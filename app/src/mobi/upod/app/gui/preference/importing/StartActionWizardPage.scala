package mobi.upod.app.gui.preference.importing

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import mobi.upod.android.view.wizard.WizardPage


abstract class StartActionWizardPage(key: String, headerId: Int) extends WizardPage(key, headerId) {
  protected var textView: TextView = _

  override protected final def createContentView(context: Context, container: ViewGroup, inflater: LayoutInflater): View = {
    val message = doAction(context)
    textView = new TextView(context)
    textView.setText(message)
    container.addView(textView)
    textView
  }

  protected def doAction(context: Context): String
}