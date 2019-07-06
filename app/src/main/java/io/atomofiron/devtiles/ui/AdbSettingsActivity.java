package io.atomofiron.devtiles.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import io.atomofiron.devtiles.R;

public class AdbSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_adb);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        Preference preference = findPreference(getString(R.string.pref_key_for_aps));
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, sp.getString(preference.getKey(), ""));
        preference = findPreference(getString(R.string.pref_key_auto_enable_adb));
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, sp.getBoolean(preference.getKey(), false));
        preference = findPreference(getString(R.string.pref_key_auto_disable_adb));
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference, sp.getBoolean(preference.getKey(), false));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (preference.getKey().equals(getString(R.string.pref_key_auto_enable_adb))) {
            findPreference(getString(R.string.pref_key_auto_disable_adb)).setEnabled((boolean) value);
            findPreference(getString(R.string.pref_key_for_aps)).setEnabled((boolean) value);
        } else if (preference.getKey().equals(getString(R.string.pref_key_for_aps))) {
            preference.setSummary(stringValue.replace('\n', ' '));
        }
        return true;
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }

    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

}
