package mobi.upod.app.gui.preference

import mobi.upod.android.preference.SimplePreferenceFragment
import de.wcht.upod.R
import mobi.upod.app.storage.PlaybackPreferences
import mobi.upod.android.util.ApiLevel


class PlaybackPreferenceFragment extends SimplePreferenceFragment(R.xml.pref_playback) {
  protected def prefs = Some(inject[PlaybackPreferences])
}
