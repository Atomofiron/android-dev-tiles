package io.atomofiron.devtiles;

import android.util.Log;

public class I {
    public static final boolean LOGGING = BuildConfig.DEBUG;

    public static void log(String s) {
        Log.e("atomofiron", s);
    }

    public static void log(Object o, String s) {
        log(String.format("[%s] %s", o.getClass().getSimpleName(), s));
    }
}
