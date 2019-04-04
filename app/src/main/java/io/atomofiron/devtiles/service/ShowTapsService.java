package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings.System;

import io.atomofiron.devtiles.util.Cmd;

@SuppressLint("NewApi, DefaultLocale")
public class ShowTapsService extends BaseService {
    private static final String CONTENT_TEMPLATE = "su -c content insert --uri content://settings/system --bind name:s:%s --bind value:i:%d";
    private static final String KEY = "show_touches";
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    @Override
    public void onClick(boolean isActive) {
        boolean success = false;
        int value = isActive ? DISABLE : ENABLE;

        try {
            success = System.putInt(getContentResolver(), KEY, value);
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
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }
}
