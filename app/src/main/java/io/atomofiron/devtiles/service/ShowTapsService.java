package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings;

@SuppressLint("NewApi")
public class ShowTapsService extends SettingsService {
    private static final String NAME = "show_touches";

    {
        uri = Settings.System.CONTENT_URI;
        name = NAME;
    }
}
