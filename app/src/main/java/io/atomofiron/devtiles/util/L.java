package io.atomofiron.devtiles.util;

import android.annotation.SuppressLint;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import io.atomofiron.devtiles.BuildConfig;

@SuppressLint("SdCardPath")
public class L {
    private static final String TAG = "devtiles";
    private static final String PATH = "/data/data/" + BuildConfig.APPLICATION_ID + "/log.txt";
    private static final String PATH_OLD = "/data/data/" + BuildConfig.APPLICATION_ID + "/log_old.txt";

    private static final long MAX_FILE_LENGTH_BYTES = 1024L * 100;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US); // without YYYY-MM-dd_
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private File oldFile = new File(PATH_OLD);
    private File file = new File(PATH);

    public static final L instance = new L();

    private static boolean logExceptions = true;

    private L() {
        try {
            if (file.createNewFile())
                Log.e(TAG, "[Logger] new file " + file.getAbsolutePath());

            // todo remove this
            if (oldFile.length() > MAX_FILE_LENGTH_BYTES) {
                oldFile.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "[Logger Exception] file " + e);
            logExceptions = file.canWrite();
        }
    }

    private void dump() {
        try {
            oldFile.delete();
            if (!file.renameTo(oldFile)) file.delete();
            file.createNewFile();
        } catch (Exception e) {
            if (logExceptions)
                Log.e(TAG, "[Logger] " + e);
        }
    }

    private void log(String s) {
        try {
            String time = formatter.format(new Date(System.currentTimeMillis()));
            String text = String.format("[%s] %s\n", time, s);
            appendText(file, text);

            if (file.length() > MAX_FILE_LENGTH_BYTES) dump();
        } catch (Exception e) {
            if (logExceptions)
                Log.e(TAG, "[Logger] " + e);
        }
    }

    public static void log(Object context, String s) {
        log(context.getClass().getSimpleName(), s);
    }

    public static void log(String label, String s) {
        String text = String.format("[%s] %s", label, s);
        Log.e(TAG, text);
        L.instance.log(text);
    }

    public static void appendText(File file, String text) throws IOException {
        appendBytes(file, text.getBytes(UTF_8));
    }

    public static void appendBytes(File file, byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        fos.write(bytes);
        fos.close();
    }

    public static String readText(File file) throws IOException {
        return new String(readBytes(file), UTF_8);
    }

    public static byte[] readBytes(File file) throws IOException {
        InputStream fis = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new OutOfMemoryError(String.format(
                    "File %s is too big (%d%n bytes) to fit in memory.",
                    file.getAbsolutePath(),
                    length
            ));
        }
        int remaining = (int) length;
        byte[] result = new byte[remaining];
        int offset = 0;
        while (remaining > 0) {
            int read = fis.read(result, offset, remaining);
            if (read < 0) break;
            remaining -= read;
            offset += read;
        }
        if (remaining == 0)
            return result;
        else
            return Arrays.copyOf(result, offset);
    }
}