package mobi.upod.app.services.download

import de.wcht.upod.R
import mobi.upod.android.content.{RemoteActionIntent, RemoteActionReceiver}
import mobi.upod.app.AppInjection

class RemoteDownloadActionReceiver extends RemoteActionReceiver with AppInjection {

  val intentBuilder = RemoteDownloadActionIntent

  protected def createActions = Map(
    R.id.action_stop_download -> new StopDownloadAction
  )
}

object RemoteDownloadActionIntent extends RemoteActionIntent("REMOTE_DOWNLOAD_ACTION", classOf[RemoteDownloadActionReceiver])