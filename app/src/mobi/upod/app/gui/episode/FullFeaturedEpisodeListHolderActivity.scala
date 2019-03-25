package mobi.upod.app.gui.episode

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import mobi.upod.app.data.EpisodeListItem

trait FullFeaturedEpisodeListHolderActivity extends AppCompatActivity with EpisodeListHolder {

  def enableEpisodeActions = true

  def checkClickedEpisode = false

  def checkedEpisode = None

  def openEpisode(episodeListItem: EpisodeListItem, navigationItemId: Long, viewModeId: Int, prepareIntent: Intent => Unit) {
    EpisodeDetailsActivity.start(this, episodeListItem, navigationItemId, viewModeId, prepareIntent)
  }
}
