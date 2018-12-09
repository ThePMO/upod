package mobi.upod.app.gui.info

import mobi.upod.android.view.cards.{CardHeader, CardHeaders, TipCardHeader}
import de.wcht.upod.R

trait FinishedEpisodesTip extends CardHeaders {

  override def createCardHeaders: Seq[CardHeader] = super.createCardHeaders :+
    TipCardHeader.textTip("finished", R.string.tip_finished_title, R.string.tip_finished_details)
}
