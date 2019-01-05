package mobi.upod.app.storage

import android.app.Application
import mobi.upod.android.content.preferences._
import org.joda.time.DateTime
import mobi.upod.app.AppUpgradeListener

class InternalSyncPreferences(app: Application) extends DevicePreferences(app) with AppUpgradeListener {

  lazy val lastPushSyncTimestamp = new DateTimePreference("lastPushSyncTimestamp") with Setter[DateTime]
  lazy val lastFullSyncTimestamp = new DateTimePreference("lastFullSyncTimestamp") with Setter[DateTime]
  lazy val fullSyncRequired = new BooleanPreference("fullSyncRequired") with Setter[Boolean]
  lazy val playlistUpdated = new BooleanPreference("playlistUpdated") with Setter[Boolean]
  lazy val identitySettingsUpdated = new BooleanPreference("identitySettingsUpdated") with Setter[Boolean]
  lazy val nextSyncTimestamp = new DateTimePreference("nextSyncTimestamp") with OptionSetter[DateTime]

  lazy val isUpgradeSync = new BooleanPreference("isUpgradeSync", false) with Setter[Boolean]
  lazy val episodeUriState = new EnumerationPreference(EpisodeUriState)("episode_uri_state", EpisodeUriState.UpToDate) with Setter[EpisodeUriState.Type]
  lazy val lastAnnouncementETag = new StringPreference("lastAnnouncementETag", "") with Setter[String]

  def preferences = Seq(
    lastPushSyncTimestamp,
    lastFullSyncTimestamp,
    fullSyncRequired,
    playlistUpdated,
    lastAnnouncementETag
  )

  def reset() {
    fullSyncRequired := false
    playlistUpdated := false
    lastAnnouncementETag := ""
  }

  def onAppUpgrade(oldVersion: Int, newVersion: Int): Unit = {
    if (oldVersion < 31) {
      identitySettingsUpdated := true
    }
    if (oldVersion < 400) {
      isUpgradeSync := true
    }
    if (oldVersion < 4400) {
      episodeUriState := EpisodeUriState.LocalUriUpdateRequired
    }
  }
}
