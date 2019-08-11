package io.atomofiron.devtiles.util;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.content.ContentValues;
import android.content.ContentProvider;
import android.database.Cursor;
import java.io.File;
import java.io.FileNotFoundException;

public class FileContentProvider extends ContentProvider {

    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File file = new File(getContext().getDataDir(), uri.getPath());

        if (!file.exists())
            throw new FileNotFoundException(file.getAbsolutePath());

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    }

    public String getType(Uri uri) {
        return L.FILE_TYPE;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}