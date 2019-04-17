package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings;

@SuppressLint("NewApi")
public class PointerService extends SettingsService {
    private static final String NAME = "pointer_location";

    {
        uri = Settings.System.CONTENT_URI;
        name = NAME;
    }
}
