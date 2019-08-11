package io.atomofiron.devtiles.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;
import android.view.Menu;
import android.view.MenuItem;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.service.AdbTcpIpService;
import io.atomofiron.devtiles.util.L;
import io.atomofiron.devtiles.util.permission.PermissionCallback;
import io.atomofiron.devtiles.util.permission.Permissions;

public class AdbSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private SharedPreferences sp;
    private Permissions permissions = new Permissions(this);
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_adb);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_shale_log) {
            shareLog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareLog() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND)
                .setType(L.FILE_TYPE)
                .putExtra(Intent.EXTRA_STREAM, L.URI);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.title_share_file_with)));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (preference.getKey().equals(getString(R.string.pref_key_auto_enable_adb))) {
            boolean checked = (boolean) value;
            boolean granted = permissions.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            onAeChecked(checked && granted);
            if (checked && !granted) {
                requestPermission();
                return false;
            }
        } else if (preference.getKey().equals(getString(R.string.pref_key_for_aps))) {
            preference.setSummary(stringValue.replace('\n', ' '));
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onAeChecked(boolean checked) {
        Preference preference = findPreference(getString(R.string.pref_key_auto_enable_adb));
        ((TwoStatePreference) preference).setChecked(checked);

        preference = findPreference(getString(R.string.pref_key_auto_disable_adb));
        preference.setEnabled(checked);
        if (!checked)
            ((TwoStatePreference) preference).setChecked(false);

        preference = findPreference(getString(R.string.pref_key_for_aps));
        preference.setEnabled(checked);

        if (!checked)
            notificationManager.cancel(AdbTcpIpService.SETTINGS_NOTIFICATION_ID);
    }

    @Override
    public boolean onIsMultiPane() {
        return false;
    }

    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    private void requestPermission() {
        permissions.check(Manifest.permission.ACCESS_COARSE_LOCATION, new PermissionCallbackImpl());
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.need_coarse_location)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void showFinalDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.grant_via_system_settings)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(Permissions.getPermissionsSettingsIntent());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private class PermissionCallbackImpl extends PermissionCallback {
        public void onGranted() {
            onAeChecked(true);
        }

        public void onDenied(String permission) {
            showPermissionDialog();
        }

        public void onForbidden(String permission) {
            showFinalDialog();
        }
    }
}
