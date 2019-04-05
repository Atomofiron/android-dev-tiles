package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.provider.Settings.System;

import io.atomofiron.devtiles.util.Cmd;

@SuppressLint("NewApi, DefaultLocale")
public class DontKeepService extends BaseService {
    private static final String CONTENT_TEMPLATE = "su -c content insert --uri content://settings/global --bind name:s:%s --bind value:i:%d";
    private static final String KEY = "always_finish_activities";
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    @Override
    public void onClick(boolean isActive) {
        boolean success = false;
        int value = isActive ? DISABLE : ENABLE;

        try {
            success = Settings.Global.putInt(getContentResolver(), KEY, value);

            log("success: " + success);
        } catch (SecurityException e) {
            log("exc: " + e.toString());

            success = Cmd.run(String.format(CONTENT_TEMPLATE, KEY, value));
        } catch (Exception e) {
            log("exc: " + e.toString());
        }

        if (success) updateTile(!isActive);
    }

    @Override
    public void onUpdate() {
        try {
            int state = System.getInt(getContentResolver(), KEY);
            updateTile(state == ENABLE);
            log("state: " + state);
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }
}
