package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.Settings;

@SuppressLint("NewApi")
public class PointerService extends SettingsService {
    private static final String NAME = "pointer_location";

    @Override
    protected Uri getUri() {
        return Settings.System.CONTENT_URI;
    }

    @Override
    protected String getName() {
        return NAME;
    }
}
