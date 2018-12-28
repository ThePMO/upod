package mobi.upod.app.gui.preference.importing

import java.io.File

import android.content.{Context, Intent}
import android.net.Uri
import de.wcht.upod.R
import mobi.upod.app.gui.opml.OpmlImportActivity


class OPMLImport(key: String) extends StartActionWizardPage(key, R.string.wizard_migration_import_opml) {
  override protected def doAction(context: Context): String = {
    val opmlFile = new File("/sdcard/subscriptions.opml")
    if (opmlFile.exists) {
      val intent = new Intent(context, classOf[OpmlImportActivity])
      intent.setData(Uri.fromFile(opmlFile))
      startActivity(intent)
      context.getString(R.string.wizard_migration_import_opml_finished)
    } else {
      context.getString(R.string.wizard_migration_import_file_not_found, opmlFile)
    }
  }
}