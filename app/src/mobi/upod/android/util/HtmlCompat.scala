package mobi.upod.android.util

import android.text.{Html, Spanned}

object HtmlCompat {
  def fromHtml(source: String): Spanned =
    if (ApiLevel >= ApiLevel.Nougat) {
      Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    } else {
      //noinspection ScalaDeprecation
      Html.fromHtml(source)
    }
}
