package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.Settings;

import io.atomofiron.devtiles.util.Cmd;

@SuppressLint("DefaultLocale")
public abstract class SettingsService extends BaseService {
    private static final String CONTENT_TEMPLATE = "su -c content insert --uri %s --bind name:s:%s --bind value:i:%d";
    private static final int ENABLE = 1;
    private static final int DISABLE = 0;

    private static final String SYSTEM = "system";
    private static final String SECURE = "secure";
    private static final String GLOBAL = "global";

    protected Uri uri = null;
    protected String name = null;

    private int get() {
        String uri = this.uri.toString();

        if (uri.endsWith(SYSTEM))
            return Settings.System.getInt(getContentResolver(), name, DISABLE);
        else if (uri.endsWith(SECURE))
            return Settings.Secure.getInt(getContentResolver(), name, DISABLE);
        else if (uri.endsWith(GLOBAL))
            return Settings.Global.getInt(getContentResolver(), name, DISABLE);
        else
            return DISABLE;
    }

    private boolean set(int value) {
        String uri = this.uri.toString();

        if (uri.endsWith(SYSTEM))
            return Settings.System.putInt(getContentResolver(), name, value);
        else if (uri.endsWith(SECURE))
            return Settings.Secure.putInt(getContentResolver(), name, value);
        else if (uri.endsWith(GLOBAL))
            return Settings.Global.putInt(getContentResolver(), name, value);
        else
            return false;
    }

    @Override
    public final void onClick(boolean isActive) {
        boolean success = false;
        int value = isActive ? DISABLE : ENABLE;

        try {
            success = set(value);

        } catch (SecurityException e) {
            log("exc: " + e.toString());

            success = Cmd.run(String.format(CONTENT_TEMPLATE, uri, name, value));
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
        log("success: " + success);

        if (success) updateTile(!isActive);
    }

    @Override
    public final void onUpdate() {
        if (uri == null)
            throw new NullPointerException("SettingsService.uri == null!");

        if (name == null)
            throw new NullPointerException("SettingsService.name == null!");

        try {
            int state = get();
            updateTile(state == ENABLE);
            log("state: " + state);
        } catch (Exception e) {
            log("exc: " + e.toString());
        }
    }
}
