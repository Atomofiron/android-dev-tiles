package io.atomofiron.devtiles;

import android.util.Log;

public class I {
    public static final boolean LOGGING = BuildConfig.DEBUG;

    public static void log(String s) {
        Log.e("atomofiron", s);
    }
}
