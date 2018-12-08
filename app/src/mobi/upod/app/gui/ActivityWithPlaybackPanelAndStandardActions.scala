package mobi.upod.app.gui

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import mobi.upod.android.app.action.ActivityActions
import mobi.upod.android.view.ChildViewsActivity
import mobi.upod.app.R
import mobi.upod.app.gui.playback.PlaybackPanel

trait ActivityWithPlaybackPanelAndStandardActions
  extends AppCompatActivity
  with ChildViewsActivity
  with PlaybackPanel
  with ActivityActions {

  protected val optionsMenuResourceId = R.menu.standard_activity_actions

  def getActivity: Activity = this

  override def onStart(): Unit = {
    super.onStart()
    getSupportActionBar.setElevation(0f)
    onActivityStart()
  }

  override def onStop(): Unit = {
    onActivityStop()
    super.onStop()
  }

  override protected def showControls(show: Boolean): Unit = {
    super.showControls(show)
    playbackPanel.show(show)
  }
}
