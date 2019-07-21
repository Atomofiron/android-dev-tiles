package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.Settings;

import io.atomofiron.devtiles.util.Result;

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

    private void assertFields() {
        if (uri == null)
            throw new NullPointerException("SettingsService.uri == null!");

        if (name == null)
            throw new NullPointerException("SettingsService.name == null!");
    }

    private int get() {
        assertFields();
        String uri = this.uri.toString();

        if (uri.endsWith(SYSTEM))
            return Settings.System.getInt(getContentResolver(), name, DISABLE);
        else if (uri.endsWith(SECURE))
            return Settings.Secure.getInt(getContentResolver(), name, DISABLE);
        else if (uri.endsWith(GLOBAL))
            return Settings.Global.getInt(getContentResolver(), name, DISABLE);
        else
            throw new IllegalArgumentException("uri = " + uri);
    }

    private boolean set(int value) {
        assertFields();
        String uri = this.uri.toString();

        if (uri.endsWith(SYSTEM))
            return Settings.System.putInt(getContentResolver(), name, value);
        else if (uri.endsWith(SECURE))
            return Settings.Secure.putInt(getContentResolver(), name, value);
        else if (uri.endsWith(GLOBAL))
            return Settings.Global.putInt(getContentResolver(), name, value);
        else
            throw new IllegalArgumentException("uri = " + uri);
    }

    @Override
    public final void onClick(boolean isActive) {
        int value = isActive ? DISABLE : ENABLE;

        try {
            needSu = false;

            if (set(value))
                updateTile(!isActive);
        } catch (SecurityException e) {
            log("onClick: exc = " + e.toString());

            needSu = true;

            runWithSu(String.format(CONTENT_TEMPLATE, uri, name, value));
        }
    }

    @Override
    public final void onUpdate() {
        try {
            int state = get();
            updateTile(state == ENABLE);
            log("onUpdate: state = " + state);
        } catch (Exception e) {
            log("onUpdate: exc = " + e.toString());
        }
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        onUpdate();
    }
}
