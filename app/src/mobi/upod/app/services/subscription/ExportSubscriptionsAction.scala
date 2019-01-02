package mobi.upod.app.services.subscription

import java.io.File

import android.content.{Context, Intent}
import android.net.Uri
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import mobi.upod.android.app.action.{ActionWaitDialog, AsyncAction}
import tech.olbrich.upod.R
import mobi.upod.app.storage.PodcastDao
import mobi.upod.io.WorldReadables

class ExportSubscriptionsAction(implicit val bindingModule: BindingModule)
  extends AsyncAction[Unit, Uri]
  with ActionWaitDialog
  with Injectable {

  override protected def getData(context: Context): Unit = ()

  override protected def processData(context: Context, data: Unit): Uri =
    writeOpml(context)

  override protected def postProcessData(context: Context, result: Uri): Unit =
    sendOpml(context, result)

  private def writeOpml(context: Context): Uri = {
    val file = new File(context.getCacheDir, "subscriptions.opml")
    val dao = inject[PodcastDao]
    dao.findSubscriptionListItems doAndClose { subscriptions =>
      OpmlWriter.write(file, subscriptions.map(s => s.title -> s.url))
    }
    WorldReadables.worldReadable(context, file)
  }

  private def sendOpml(context: Context, uri: Uri): Unit = {
    val intent = new Intent(Intent.ACTION_SEND)
    intent.setType("text/x-opml")
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_export_subscriptions)))
  }
}
