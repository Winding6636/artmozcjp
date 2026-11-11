package com.kazumaproject.markdownhelperkeyboard.setting_activity.ui.setting

import android.os.Bundle
import androidx.preference.Preference
import com.kazumaproject.markdownhelperkeyboard.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RomajiFlick12PreferenceFragment : AsyncPreferenceFragment() {
    override val preferencesXmlRes: Int = R.xml.pref_romaji_flick12

    override fun onPreferencesReady(savedInstanceState: Bundle?, rootKey: String?) {
        findPreference<Preference>("romaji_flick12_size_position_preference")?.setOnPreferenceClickListener {
            navigateSafely(R.id.keyboardGeometrySettingsFragment, Bundle().apply { putString("geometryKeyboard", "CUSTOM") })
            true
        }
        findPreference<Preference>("romaji_flick12_key_size_preference")?.setOnPreferenceClickListener {
            navigateSafely(R.id.flickKeyboardSizeSettingsFragment, Bundle().apply { putString("sizingSurface", "custom") })
            true
        }
        findPreference<Preference>("romaji_flick12_popup_style_preference")?.setOnPreferenceClickListener {
            navigateSafely(R.id.flickKeyboardPopupStyleListFragment)
            true
        }
        applyLegacySearchResultFilterIfNeeded()
    }
}
