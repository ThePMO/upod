package mobi.upod.app.gui.episode.playlist

import com.escalatesoft.subcut.inject.BindingModule
import mobi.upod.app.data.EpisodeListItem
import mobi.upod.app.gui.episode.{AsyncEpisodeAction, EpisodeAction}
import mobi.upod.app.services.playback.PlaybackService
import android.content.Context
import mobi.upod.app.services.licensing.LicenseService

private[episode] class PlayEpisodeAction(episode: => Option[EpisodeListItem])(implicit bindings: BindingModule)
  extends AbstractPlayEpisodeAction(episode) {

  override protected def isAdequatePlaybackAction(episode: EpisodeListItem): Boolean = episode.downloadInfo.complete
}
