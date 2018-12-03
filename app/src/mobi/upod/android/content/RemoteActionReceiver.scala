package mobi.upod.android.content

import android.content.{Context, Intent, BroadcastReceiver}
import mobi.upod.android.app.action.ActionController

trait RemoteActionReceiver extends BroadcastReceiver with ActionController {
  val intentBuilder: RemoteActionIntent

  def onReceive(context: Context, intent: Intent) {
    if (Option(intent.getAction).contains(intentBuilder.IntentAction)) {
      intentBuilder.actionId(intent).foreach(fire(_)(context))
    }
  }
}
