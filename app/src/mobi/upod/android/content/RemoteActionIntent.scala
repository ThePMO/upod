package mobi.upod.android.content

import android.app.PendingIntent
import android.content.{Context, Intent}
import android.net.Uri
import mobi.upod.app

class RemoteActionIntent(intentActionName: String, receiver: Class[_]) {
  val DataUriBase = s"upod://remote-action.upod.mobi/${intentActionName.toLowerCase}"
  val DataUriPattern = s"$DataUriBase/([\\-0-9]+)".r
  val IntentAction: String = app.IntentAction(intentActionName)

  def apply(context: Context, actionId: Int): PendingIntent = {
    val dataUri = Uri.parse(s"$DataUriBase/$actionId")
    val intent = new Intent(IntentAction, dataUri, context, receiver)
    PendingIntent.getBroadcast(context, 0, intent, 0)
  }

  def actionId(intent: Intent): Option[Int] = intent.getData.toString match {
    case DataUriPattern(id) => Some(id.toInt)
    case _ => None
  }
}
