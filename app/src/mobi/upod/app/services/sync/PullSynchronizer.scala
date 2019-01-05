package mobi.upod.app.services.sync

import java.net.{URI, URL}

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.google.api.client.http.HttpResponseException
import de.wcht.upod.R
import mobi.upod.android.logging.Logging
import mobi.upod.app.App
import mobi.upod.app.data._
import mobi.upod.app.services.{AnnouncementWebService, AnnouncementService, EpisodeService}
import mobi.upod.app.services.download.DownloadService
import mobi.upod.app.services.playback.PlaybackService
import mobi.upod.app.storage._
import org.joda.time.DateTime

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

private[sync] class PullSynchronizer(syncWebService: Syncer)(implicit val bindingModule: BindingModule) extends Logging with Injectable {
  private val context = inject[App]
  private val database = inject[Database]

  private val podcastFetchService = inject[PodcastFetchService]
  private val episodeService = inject[EpisodeService]
  private val playbackService = inject[PlaybackService]

  private val podcastDao = inject[PodcastDao]
  private val episodeDao = inject[EpisodeDao]
  private val importedSubscriptionsDao = inject[ImportedSubscriptionsDao]

  private val uiPreferences = inject[UiPreferences]
  private val internalSyncPreferences = inject[InternalSyncPreferences]

  def syncAllPodcasts(progressIndicator: SyncProgressIndicator): Unit =
    syncAllPodcasts(progressIndicator, false)

  def syncAllPodcastsWithNewerServerStatus(progressIndicator: SyncProgressIndicator): Unit =
    syncAllPodcasts(progressIndicator, true)

  def syncPodcast(podcast: URI, progressIndicator: SyncProgressIndicator): Unit = {
    val maxProgress = 1 // fetch one podcast

    podcastDao.findFetchInfoFor(podcast) foreach { fetchInfo =>
      progressIndicator.initProgress(
        maxProgress,
        context.getString(R.string.sync_notification_pull_podcast, fetchInfo.title.getOrElse(fetchInfo.url)))
      sync(progressIndicator, fetchPodcast(fetchInfo, false))
    }
  }

  private def syncAllPodcasts(progressIndicator: SyncProgressIndicator, syncOnlyPodcastsWithNewerServerStatus: Boolean): Unit = {
    val maxProgress = 1 + // deleting downloads
        1 // updating coverart

    progressIndicator.initProgress(maxProgress, "")
    pullAnnouncements()
    sync(progressIndicator, doSyncAllPodcasts(syncOnlyPodcastsWithNewerServerStatus, progressIndicator))
    internalSyncPreferences.isUpgradeSync := false

    progressIndicator.increaseProgress(context.getString(R.string.sync_notification_delete_downloads))
    episodeDao.inTransaction(episodeDao.resetDownloadInfoForOldFinishedEpisodes())

    progressIndicator.increaseProgress(context.getString(R.string.sync_notification_updating_coverart))
    syncCoverart()
    syncPodcastColors()
  }

  def sync(progressIndicator: SyncProgressIndicator, syncImplementation: => Unit) {
    val timestamp = DateTime.now
    val newEpisodesHash = episodeDao.calculateNewEpisodesHash

    val playlistChangeDao = new PlaylistChangeDao(inject[DatabaseHelper])
    playlistChangeDao.create()
    if (internalSyncPreferences.isUpgradeSync) {
      episodeDao.inTransaction(episodeDao.rememberUnfinished())
    }
    try {
      Thread.currentThread().setContextClassLoader(getClass.getClassLoader) // initialize properties loading for ROME RSS
      episodeDao.inTransaction(episodeDao.deleteEpisodesWithoutPodcast())
      syncImplementation
      episodeDao.inTransaction(episodeDao.deleteEpisodesWithoutPodcast())
    } finally {
      playlistChangeDao.drop()
    }
    if (internalSyncPreferences.isUpgradeSync) {
      episodeDao.inTransaction(episodeDao.markNotRemberedUnfinished())
    }
    cleanupDownloadList()
    updateSyncPreferences(timestamp)
    inject[DownloadService].applyAutoAddStrategy()

    episodeService.fireEpisodeCountChanged()
    playbackService.playlistChanged()
    notifyNewEpisodesIfApplicable(newEpisodesHash)
  }

  private def updateSyncPreferences(timestamp: DateTime): Unit = {
    internalSyncPreferences.lastFullSyncTimestamp := timestamp
    if (internalSyncPreferences.episodeUriState.get == EpisodeUriState.LocalUriUpdateRequired)
      internalSyncPreferences.episodeUriState := EpisodeUriState.UpToDate
  }

  private def pullAnnouncements(): Unit = {
    log.info("pulling new announcements")
    val announcementWebService = inject[AnnouncementWebService]
    val lastETag = internalSyncPreferences.lastAnnouncementETag.option
    val (newETag, announcementsCursor) = announcementWebService.getAnnouncements(lastETag)
    val announcements = announcementsCursor.toSeqAndClose()
    database.inTransaction {
      val dao = inject[AnnouncementDao]
      announcements.foreach(dao.insertOrUpdate)
      dao.deleteOldAnnouncements()
    }
    if (announcements.nonEmpty) {
      inject[AnnouncementService].onNewAnnouncement()
    }
  }

  /** Fetches subscriptions and their settings and all required podcasts from the server, updates them in the database
    * and returns new (unknown) podcasts
    *
    * @return new (unknown) subscriptions and podcasts not in the local database
    */
  private def fetchAndUpdatePodcasts(progressIndicator: SyncProgressIndicator): (Seq[Subscription], Seq[URL]) = {
    val importedSubscriptions = importedSubscriptionsDao.list.toSetAndClose().map(Subscription.apply)

    (importedSubscriptions.toSeq, Seq())
  }

  private def findKnownFetchInfos(syncOnlyPodcastsWithNewerServerStatus: Boolean): List[PodcastFetchInfo] = {
    val allKnownFetchInfos = {
      podcastDao.listFetchInfos.toListAndClose()
    }
    if (syncOnlyPodcastsWithNewerServerStatus) {
      val updatedPodcasts = syncWebService.getEpisodePodcasts(internalSyncPreferences.lastFullSyncTimestamp.option).toSetAndClose()
      allKnownFetchInfos.filter(fi => updatedPodcasts.contains(fi.url))
    } else {
      allKnownFetchInfos
    }
  }

  private def doSyncAllPodcasts(syncOnlyPodcastsWithNewerServerStatus: Boolean, progressIndicator: SyncProgressIndicator): Unit = {
    val podcastUrlToUri = mutable.Map[URL, URI]()
    val (newSubscriptions, newPodcasts) = fetchAndUpdatePodcasts(progressIndicator)
    val relevantFetchInfos = findKnownFetchInfos(syncOnlyPodcastsWithNewerServerStatus)

    def fetchNextPodcast(remainingFetchInfos: List[PodcastFetchInfo], newSubscription: Boolean, progress: Int): Unit = if (remainingFetchInfos.nonEmpty) {
      val fetchInfo = remainingFetchInfos.head
      progressIndicator.increaseProgress(
        context.getString(R.string.sync_notification_pull_podcast, fetchInfo.title.getOrElse(fetchInfo.url)))
      try {
        fetchPodcast(fetchInfo, newSubscription, syncOnlyPodcastsWithNewerServerStatus) foreach { uri =>
          podcastUrlToUri += fetchInfo.url -> uri
        }
      } catch {
        case ex: HttpResponseException =>
          log.warn(s"Failed to fetch podcast ${fetchInfo.title.getOrElse("")} from ${fetchInfo.url}", ex)
      }
      fetchNextPodcast(remainingFetchInfos.tail, newSubscription, progress + 1)
    }

    def fetchAndUpdateNewPodcasts(): Unit = {
      val fetchInfos = newPodcasts.map(PodcastFetchInfo.apply).toList
      fetchNextPodcast(fetchInfos, false, 0)
    }

    def fetchAndUpdateNewSubscriptions(): Unit = {
      val fetchInfos = newSubscriptions.map(PodcastFetchInfo.apply).toList
      fetchNextPodcast(fetchInfos, true, newPodcasts.length)
    }

    def fetchAndUpdateKnownPodcasts(): Unit =
      fetchNextPodcast(relevantFetchInfos, false, newSubscriptions.length)

    progressIndicator.addMaxProgress(relevantFetchInfos.length + newSubscriptions.length + newPodcasts.length)

    fetchAndUpdateNewPodcasts()
    fetchAndUpdateNewSubscriptions()
    fetchAndUpdateKnownPodcasts()
    importedSubscriptionsDao.inTransaction(importedSubscriptionsDao.removeAll())
  }

  private def fetchPodcast(fetchInfo: PodcastFetchInfo, newSubscription: Boolean, onlyIfNewerServerStatus: Boolean = false): Try[URI] = {
    def fetch: Option[PodcastWithEpisodes] = {
      if (internalSyncPreferences.episodeUriState.get != EpisodeUriState.UpToDate)
        Some(podcastFetchService.fetchPodcast(fetchInfo.url))
      else
        podcastFetchService.fetchPodcastIfNewOrUpdated(fetchInfo.url, fetchInfo.eTag, fetchInfo.modified)
    }

    def updatePodcastAndEpisodes(podcastUri: URI, podcast: PodcastSyncInfo, episodes: Seq[EpisodeSyncInfo]): Unit = {
      podcastDao.update(podcastUri, podcast)
      updateOrInsertEpisodes(podcast, episodes)
    }

    def insertPodcastAndEpisodes(podcast: PodcastSyncInfo, episodes: Seq[EpisodeSyncInfo]): Unit = {
      val p = if (newSubscription) fetchInfo.settings.fold(podcast.toPodcast)(podcast.toPodcast) else podcast.toPodcast
      podcastDao.save(p)
      updateOrInsertEpisodes(podcast, episodes)
    }

    def updateOrInsertEpisodes(podcast: PodcastSyncInfo, episodes: Seq[EpisodeSyncInfo]): Unit = {
      val podcastInfo = podcast.toEpisodePodcastInfo
      episodeDao.markAllUnlisted(podcast.uri)
      episodes foreach { episode =>
        //noinspection ScalaDeprecation
        if (internalSyncPreferences.episodeUriState.get != EpisodeUriState.UpToDate)
          episodeDao.updateOrInsertListedWithPotentialNewUri(episode, episode.toEpisode(podcastInfo))
        else
          episodeDao.updateOrInsertListed(episode, episode.toEpisode(podcastInfo))
      }
    }

    def update(podcastWithEpisodes: Option[PodcastWithEpisodes]): URI = {
      (podcastWithEpisodes, fetchInfo.uri) match {
        case (Some(pe), Some(uri)) =>
          updatePodcastAndEpisodes(uri, pe.podcast, pe.episodes)
          uri
        case (Some(pe), None) =>
          insertPodcastAndEpisodes(pe.podcast, pe.episodes)
          pe.podcast.uri
        case (None, Some(uri)) =>
          uri
        case (None, None) => throw new IllegalStateException("No content for unknown (new) podcast")
      }
    }

    def fetchEpisodeStatus: Seq[EpisodeStatusSyncInfo] = {
        Seq()
    }

    def updateEpisodeStatus(podcast: URI, remoteEpisodeStatus: Seq[EpisodeStatusSyncInfo]): Unit = if (false) {
      database.withoutTriggers {
        val episodeStatus = remoteEpisodeStatus.map(es => (EpisodeId(podcast, es.uri), es.status, es.playbackInfo))
        episodeDao.updateEpisodeStatus(episodeStatus)
      }
    }

    def addUnknownEpisodesToPlaylistIfApplicable(podcast: URI): Unit = {
      if (uiPreferences.autoAddToPlaylist || fetchInfo.settings.exists(_.autoAddToPlaylist)) {
        episodeDao.addUnknownEpisodesToPlaylist(podcast)
      }
    }

    def addUnknownEpisodesToLibraryIfApplicable(podcast: URI): Unit = {
      if (uiPreferences.skipNew || fetchInfo.settings.exists(_.autoAddEpisodes)) {
        episodeDao.addUnknownEpisodesToLibrary(podcast)
      }
    }

    def processUnknownEpisodes(podcast: URI, newestNotNewPublishedTimestamp: Option[DateTime]): Unit = if (fetchInfo.subscribed) {
      addUnknownEpisodesToPlaylistIfApplicable(podcast)
      addUnknownEpisodesToLibraryIfApplicable(podcast)
      episodeDao.markOldUnknownEpisodesFinished(podcast, newestNotNewPublishedTimestamp)
      episodeDao.markUnknownEpisodesNew(podcast)
    }

    def limitPodcastEpisodes(podcast: URI): Unit = fetchInfo.settings.flatMap(_.maxKeptEpisodes) foreach { maxKeptEpisodes =>
      episodeDao.limitPodcastLibraryEpisodeCount(podcast, maxKeptEpisodes)
    }

    try {
      val remoteEpisodeStatus = fetchEpisodeStatus
      val podcastWithEpisodes = fetch
      val podcast = database.newTransaction {
        val newestNotNewPublishedTimestamp = episodeDao.findNewestNotNewPublishedTimestamp(fetchInfo.url)
        val podcast = update(podcastWithEpisodes)
        podcastDao.resetSyncError(fetchInfo.url)
        updateEpisodeStatus(podcast, remoteEpisodeStatus)
        episodeDao.updatePodcastProperties(podcast)
        episodeDao.deleteUnlistedUnreferenced(podcast)
        processUnknownEpisodes(podcast, newestNotNewPublishedTimestamp)
        limitPodcastEpisodes(podcast)
        podcast
      }
      episodeService.fireEpisodeCountChanged()
      Success(podcast)
    } catch {
      case error: Throwable =>
        log.error(s"fetching and updating podcast ${fetchInfo.url} failed", error)
        podcastDao.inTransaction(podcastDao.setSyncError(fetchInfo.url, error.getLocalizedMessage))
        Failure(error)
    }
  }

  private def cleanupDownloadList(): Unit =
    episodeDao.inTransaction(episodeDao.removeNonLibraryEpisodesFromDownloadList())

  private def syncCoverart(): Unit =
    new ImageFetcher().sync()

  private def syncPodcastColors(): Unit =
    new PodcastColorExtractor().extractMissingColors()

  private def notifyNewEpisodesIfApplicable(oldNewEpisodesHash: EpisodeListHash): Unit = {
    if (inject[UiPreferences].notifyNewEpisodes) {
      val context = inject[mobi.upod.app.App]
      val currentNewEpisodesHash = episodeDao.calculateNewEpisodesHash
      if (currentNewEpisodesHash != oldNewEpisodesHash && currentNewEpisodesHash.count > 0)
        NewEpisodesNotification.show(context, currentNewEpisodesHash.count)
      else if (currentNewEpisodesHash.count == 0)
        NewEpisodesNotification.cancel(context)
    }
  }
}
