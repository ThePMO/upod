package mobi.upod.app.gui.info

import android.content.Context
import com.escalatesoft.subcut.inject.Injectable
import mobi.upod.android.os.AsyncTask
import mobi.upod.android.view.cards.{CardHeader, CardHeaders, TipCardHeader}
import mobi.upod.android.widget.card.{CardButton, CardView, TextCardView}
import de.wcht.upod.R
import mobi.upod.app.services.download.DownloadService
import mobi.upod.app.storage.{AutoAddDownloadStrategy, DownloadPreferences}

trait DownloadQueueTips extends CardHeaders with Injectable {

  override def createCardHeaders: Seq[CardHeader] = super.createCardHeaders :+
    createAutomaticDownloadsQueueChoiceTip

  private def createAutomaticDownloadsQueueChoiceTip: CardHeader = {
    val strategies = Array(
      AutoAddDownloadStrategy.Manual,
      AutoAddDownloadStrategy.Playlist,
      AutoAddDownloadStrategy.Library,
      AutoAddDownloadStrategy.NewAndLibrary
    )

    val prefs = inject[DownloadPreferences]
    val currentChoiceFromPreferences: Int = strategies.indexOf(prefs.autoAddDownloadStrategy.get)

    def updatePreferencesForChoice(choice: Int): Unit = {
      prefs.autoAddDownloadStrategy := strategies(choice)
      AsyncTask.execute(inject[DownloadService].applyAutoAddStrategy())
    }

    TipCardHeader.singleChoiceTip(
      "download_queue",
      R.string.tip_automatic_download_queue_title,
      R.string.tip_automatic_download_queue_details,
      currentChoiceFromPreferences,
      updatePreferencesForChoice,
      R.string.tip_automatic_download_queue_option_manual,
      R.string.tip_automatic_download_queue_option_playlist,
      R.string.tip_automatic_download_queue_option_later,
      R.string.tip_automatic_download_queue_option_new_and_later)
  }

  private def createAutomaticDownloadStartCard(context: Context): CardView = new TextCardView(
    context,
    R.string.tip_automatic_download_start_title,
    R.string.tip_automatic_download_start_details,
    CardButton(context.getString(R.string.not_now))
  )
}
