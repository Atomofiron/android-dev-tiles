package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings.System;
import android.service.quicksettings.Tile;

import io.atomofiron.devtiles.util.Cmd;

@SuppressLint("NewApi, DefaultLocale")
public class ShowTapsService extends BaseService {
    private static final String CONTENT_TEMPLATE = "su -c content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:%d";
    private static final String KEY = "show_touches";
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    @Override
    public void onClick() {
        boolean success = false;
        boolean activate = getQsTile().getState() != Tile.STATE_ACTIVE;
        int value = activate ? ENABLE : DISABLE;

        try {
            success = System.putInt(getContentResolver(), KEY, value);
        } catch (SecurityException e) {
            log("exc: " + e.toString());

            success = Cmd.run(String.format(CONTENT_TEMPLATE, value));
        } catch (Exception e) {
            log("exc: " + e.toString());
        }

        if (success) updateTile(activate);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        try {
            int state = System.getInt(getContentResolver(), KEY);
            updateTile(state == ENABLE);
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }
}
