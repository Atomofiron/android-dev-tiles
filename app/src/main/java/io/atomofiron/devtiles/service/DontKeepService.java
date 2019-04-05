package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.provider.Settings;

@SuppressLint("NewApi")
public class DontKeepService extends SettingsService {
    private static final String NAME = "always_finish_activities";

    {
        uri = Settings.Global.CONTENT_URI;
        name = NAME;
    }
}
