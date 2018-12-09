package mobi.upod.app.gui.preference

import android.preference.Preference
import mobi.upod.android.app.action.BrowseAction
import mobi.upod.android.logging.{LogConfiguration, SendDiagnosticsAction}
import mobi.upod.android.preference.{PreferenceChangeListener, SimplePreferenceFragment}
import de.wcht.upod.R
import mobi.upod.app.gui.info.{ResetTipsAction, ShareUpodAction, ShowAboutDialogAction}


class SupportFragment extends SimplePreferenceFragment(R.xml.pref_support) {

  protected def prefs = None

  override protected def changeListeners = Map(
    "pref_enhanced_logging" -> PreferenceChangeListener(onEnhancedLoggingChanged)
  )

  override protected def clickActions = Map(
    "pref_about" -> new ShowAboutDialogAction,
    "pref_website" -> new BrowseAction("https://upod.wcht.de/"),
    "pref_twitter" -> new BrowseAction("https://twitter.com/uPodMobi"),

    "pref_reset_tips" -> new ResetTipsAction,
    "pref_support_site" -> new BrowseAction("https://github.com/Boris-de/upod/issues"),
    "pref_send_diagnostics" -> new SendDiagnosticsAction(),

    "pref_share_upod" -> new ShareUpodAction
  )

  private def onEnhancedLoggingChanged(preference: Preference, newValue: AnyRef): Boolean = {
    LogConfiguration.configureLogging(getActivity, newValue.asInstanceOf[Boolean])
    true
  }
}
