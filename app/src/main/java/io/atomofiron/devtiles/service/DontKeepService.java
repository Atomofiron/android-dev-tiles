package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.Settings;

/* does not work properly */
@SuppressLint({"NewApi", "Registered"})
public class DontKeepService extends SettingsService {
    private static final String NAME = "always_finish_activities";

    @Override
    protected Uri getUri() {
        return Settings.Global.CONTENT_URI;
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
