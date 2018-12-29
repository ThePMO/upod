package mobi.upod.app.gui.preference.importing

import java.io.{File, PrintWriter, StringWriter}
import java.net.URL
import java.util.concurrent.TimeUnit

import android.content.{Context, SharedPreferences}
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import de.wcht.upod.R
import javax.xml.parsers.{DocumentBuilderFactory, ParserConfigurationException}
import mobi.upod.android.logging.Logging
import mobi.upod.android.os.{AsyncTask, Runnable}
import mobi.upod.app.AppInjection
import mobi.upod.app.data.{Episode, Podcast}
import mobi.upod.app.services.download.DownloadService
import mobi.upod.app.storage.{ImportedSubscriptionsDao, _}
import org.w3c.dom.Node

class DiagnosticsImport(pageKey: String) extends StartActionWizardPage(pageKey, R.string.wizard_migration_import_diagnostics)
  with Logging
  with AppInjection {

  override protected def doAction(context: Context): String = {
    val databaseFile = new File("/sdcard/upod")
    val preferencesFile = new File("/sdcard/mobi.upod.app_preferences.xml")
    Seq(databaseFile, preferencesFile).foreach(f => {
      if (!f.exists) {
        return context.getString(R.string.wizard_migration_import_file_not_found, f)
      }
    })

    AsyncTask.executeWithWaitDialog(context, R.string.wizard_migration_import_diagnostics_info) {
      val message = importFromDiagnostics(context, databaseFile, preferencesFile)

      getActivity.runOnUiThread(Runnable(textView.setText(message)))
    }

    context.getString(R.string.wizard_migration_import_running)
  }

  private def importFromDiagnostics(context: Context, databaseFile: File, preferencesFile: File) = {
    val startTime = System.nanoTime()
    log.info("Started import")

    val message = try {
      importSharedPreferences(context, preferencesFile)

      importDatabase(context, databaseFile)

      addPlaylistItemsToDownloadQueue()

      context.getString(R.string.wizard_migration_import_diagnostics_finished)
    } catch {
      case e@(_: Exception | _: OutOfMemoryError) => context.getString(R.string.wizard_migration_import_error, convertExceptionToString(e))
    } finally {
      log.info("Import finished in " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime) + "s")
    }
    message
  }

  private def importSharedPreferences(context: Context, preferencesFile: File) = {
    def getAttribute(node: Node, name: String): String = {
      Option(node.getAttributes)
        .flatMap(it => Option(it.getNamedItem(name)))
        .map(_.getTextContent)
        .orNull
    }

    log.info(s"Loading preferences from $preferencesFile")

    val doc = newDocumentBuilder.parse(preferencesFile)
    val childSeq = getChildrenAsSequence(doc.getFirstChild)
    val prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    childSeq.foreach(node => {
      val typeName = node.getNodeName
      val name = getAttribute(node, "name")
      val value = Option(getAttribute(node, "value")).getOrElse(node.getTextContent)
      if (name != null && value != null && isAppPreference(name)) {
        putPreference(prefEditor, typeName, name, value)
      }
    })
    prefEditor.commit()
  }

  private def putPreference(prefEditor: SharedPreferences.Editor, typeName: String, name: String, value: String) = {
    typeName match {
      case "string" => prefEditor.putString(name, value)
      case "boolean" => prefEditor.putBoolean(name, value.toBoolean)
      case "float" => prefEditor.putFloat(name, value.toFloat)
      case "int" => prefEditor.putInt(name, value.toInt)
      case "long" => prefEditor.putLong(name, value.toLong)
      case _ => throw new IllegalStateException(s"Unknown element in preferences: $typeName $name $value")
    }
  }

  // the preferences might contain some weird legacy values, only import what looks like a setting from uPod
  private def isAppPreference(name: String) = name.startsWith("pref_") || name.startsWith("subscription_settings_")

  private def importDatabase(context: Context, databaseFile: File): Unit = {
    val importDatabase = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath, null, 0)
    val importDatabaseHelper = DatabaseHelper(context, Some(importDatabase))

    importTable[Episode, EpisodeDao](importDatabaseHelper, new EpisodeDao(_))
    importTable[Podcast, PodcastDao](importDatabaseHelper, new PodcastDao(_))
    importTable[URL, ImportedSubscriptionsDao](importDatabaseHelper, new ImportedSubscriptionsDao(_))
  }

  private def addPlaylistItemsToDownloadQueue(): Unit = {
    val episodeDao = inject[EpisodeDao]
    val playlistIDs = episodeDao.findPlaylistItems.map(_.id).toSeqAndClose()
    episodeDao.inTransaction {
      playlistIDs.foreach(episodeDao.resetDownloadInfo)
      episodeDao.addToDownloadListEnd(playlistIDs)
    }
    inject[DownloadService].downloadQueue()
  }

  private def importTable[T, D<:Dao[T]](databaseHelper: DatabaseHelper, daoConstructor: DatabaseHelper => D)(implicit m: scala.reflect.Manifest[D]): Unit = {
    val newAppDao = inject[D]
    val importAppDao = daoConstructor(databaseHelper)

    log.info(s"Starting import from ${newAppDao.tableName}")
    val cursor = importAppDao.all()
    newAppDao.newTransactionWithoutTriggers {
      cursor.foreachAndClose { it =>
        newAppDao.insertOrFail(it)
      }
    }
  }

  private def convertExceptionToString(e: Throwable) = {
    val stringWriter = new StringWriter()
    e.printStackTrace(new PrintWriter(stringWriter))
    e.getClass + ": " + e.getMessage + "\n" + stringWriter
  }

  private def getChildrenAsSequence(rootElement: Node) = {
    val children = rootElement.getChildNodes
    (0 until children.getLength).map(children.item)
  }

  private def newDocumentBuilder = {
    val dbf = DocumentBuilderFactory.newInstance
    setFeatureIfExists(dbf, "http://apache.org/xml/features/disallow-doctype-decl", value = true)
    setFeatureIfExists(dbf, "http://xml.org/sax/features/external-general-entities", value = false)
    setFeatureIfExists(dbf, "http://xml.org/sax/features/external-parameter-entities", value = false)
    setFeatureIfExists(dbf, "http://apache.org/xml/features/nonvalidating/load-external-dtd", value = false)
    dbf.setExpandEntityReferences(false)
    dbf.newDocumentBuilder()
  }

  private def setFeatureIfExists(dbf: DocumentBuilderFactory, feature: String, value: Boolean): Unit = {
    try {
      dbf.setFeature(feature, value)
    } catch {
      case _: ParserConfigurationException => log.info(s"${dbf.getClass} does not accept feature $feature")
    }
  }
}