package com.samwhited.checkin;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Show the current server pref as the summary text.
		final EditTextPreference serverPref = (EditTextPreference) findPreference("pref_server");
		if (serverPref != null) {
			serverPref.setSummary(serverPref.getText());
			serverPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				/**
				 * Called when a Preference has been changed by the user. This is
				 * called before the state of the Preference is about to be updated and
				 * before the state is persisted.
				 *
				 * @param preference The changed Preference.
				 * @param newValue   The new value of the Preference.
				 * @return True to update the state of the Preference with the new value.
				 */
				@Override
				public boolean onPreferenceChange(final Preference preference,
												  final Object newValue) {
					final String nv = (String)newValue;
					preference.setSummary(nv);
					return true;
				}
			});
		}
	}
}