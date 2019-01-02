package mobi.upod.android.widget.card

import android.content.Context
import android.widget.TextView
import tech.olbrich.upod.R

abstract class StandardCardView(context: Context, title: String, buttons: CardButton*) extends CardView(context) {

   override protected def onInitCard(): Unit = {
     super.onInitCard()
     setTitle(title)
     createContent()
     addButtons(buttons: _*)
   }

  protected def createContent(): Unit
}
