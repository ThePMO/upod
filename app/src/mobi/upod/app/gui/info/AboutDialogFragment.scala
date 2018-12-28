package mobi.upod.app.gui.info

import android.app.{Activity, Dialog, DialogFragment}
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AlertDialog
import android.text.method.LinkMovementMethod
import android.text.style.{ClickableSpan, URLSpan}
import android.text.{Spannable, SpannableStringBuilder}
import android.view.{Gravity, View}
import android.widget.{ScrollView, TextView}
import de.wcht.upod.R
import mobi.upod.android.app.SimpleDialogFragment
import mobi.upod.android.content.Theme._
import mobi.upod.android.util.HtmlCompat

class AboutDialogFragment extends DialogFragment {

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val context = getActivity

    val builder = new AlertDialog.Builder(context)
    builder.setIcon(R.drawable.ic_launcher)
    builder.setTitle(R.string.about)
    builder.setView(createContentView)
    builder.setPositiveButton(R.string.close, null)
    builder.create
  }

  private def createContentView: View = {
    val scrollView = new ScrollView(getActivity)
    scrollView.addView(createTextView)
    scrollView
  }

  private def createTextView: View = {
    val context = getActivity
    val content = new TextView(context)
    val padding = getResources.getDimensionPixelSize(R.dimen.space_medium)
    content.setPadding(padding, padding, padding, padding)
    TextViewCompat.setTextAppearance(content, context.getThemeResource(R.attr.textAppearance))
    content.setGravity(Gravity.CENTER_HORIZONTAL)
    content.setLinksClickable(true)
    content.setMovementMethod(LinkMovementMethod.getInstance())
    content.setText(createContentText)
    content
  }
  
  private def createContentText: Spannable = {

    def makeLinkClickable(spannable: SpannableStringBuilder, span: URLSpan): Unit = {
      val start = spannable.getSpanStart(span)
      val end = spannable.getSpanEnd(span)
      val flags = spannable.getSpanFlags(span)
      val clickableSpan = new ClickableSpan {
        override def onClick(widget: View): Unit =
          showLicenseDialog(span.getURL)
      }
      spannable.setSpan(clickableSpan, start, end, flags)
      spannable.removeSpan(span)
    }

    val context = getActivity
    val version = context.getPackageManager.getPackageInfo(context.getPackageName, 0).versionName
    val text = HtmlCompat.fromHtml(context.getString(R.string.about_details, version))
    val spannable = new SpannableStringBuilder(text)
    val linkSpans = spannable.getSpans(0, text.length(), classOf[URLSpan])
    linkSpans.foreach(makeLinkClickable(spannable, _))
    spannable
  }

  private def showLicenseDialog(url: String): Unit = {
    url match {
      case "#license" => ShowLicensesDialog.show(getActivity, "file:///android_asset/license.txt")
      case "#original" => ShowLicensesDialog.show(getActivity, "file:///android_asset/original_license.txt")
      case _ => ShowLicensesDialog.show(getActivity, "file:///android_asset/licenses.html")
    }
  }
}

object AboutDialogFragment {

  def show(activity: Activity): Unit = {
    val fragment = new AboutDialogFragment
    fragment.show(activity.getFragmentManager, SimpleDialogFragment.defaultTag)
  }
}
