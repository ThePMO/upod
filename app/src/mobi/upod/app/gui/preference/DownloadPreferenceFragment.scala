package mobi.upod.app.gui.preference

import mobi.upod.android.preference.SimplePreferenceFragment
import de.wcht.upod.R
import mobi.upod.app.storage.DownloadPreferences


class DownloadPreferenceFragment extends SimplePreferenceFragment(R.xml.pref_download) {
  protected def prefs = Some(inject[DownloadPreferences])
}
