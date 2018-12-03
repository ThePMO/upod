package mobi.upod.app.services.sync

import android.content.{BroadcastReceiver, Context, Intent}
import mobi.upod.android.logging.Logging
import mobi.upod.app.AppInjection

class BootCompletedReceiver extends BroadcastReceiver with AppInjection with Logging {

  def onReceive(context: Context, intent: Intent) {
    log.info("boot completed")
    // we don't need to do something specific here -- it's enough that the app has started as all services will care for themselves
  }
}
