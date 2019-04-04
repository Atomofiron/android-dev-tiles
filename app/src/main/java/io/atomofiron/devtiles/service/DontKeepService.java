package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings;
import android.provider.Settings.System;
import android.service.quicksettings.Tile;

@SuppressLint("NewApi")
public class DontKeepService extends BaseService {
    private static final String KEY = "always_finish_activities";
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    @Override
    public void onClick() {
        log("onClick");

        try {
            boolean activate = getQsTile().getState() != Tile.STATE_ACTIVE;
            boolean success = Settings.Global.putInt(getContentResolver(), KEY, activate ? ENABLE : DISABLE);

            if (success) updateTile(activate);
            log("success: " + success);
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        try {
            int state = System.getInt(getContentResolver(), KEY);
            updateTile(state == ENABLE);
            log("state: " + state);
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }
}
