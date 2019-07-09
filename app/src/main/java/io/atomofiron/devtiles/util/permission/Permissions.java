package io.atomofiron.devtiles.util.permission;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.*;

import io.atomofiron.devtiles.BuildConfig;

@TargetApi(Build.VERSION_CODES.M)
public class Permissions {
    private static final String PACKAGE_SCHEME = "package:";

    private final Activity activity;
    private final Fragment fragment;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, PermissionCallback> map = new HashMap<>();
    private int requestCode = 0;

    public Permissions(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public Permissions(Fragment fragment) {
        this.activity = null;
        this.fragment = fragment;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    protected Permissions(Permissions permissions) {
        this.activity = permissions.activity;
        this.fragment = permissions.fragment;
    }

    private int getNextRequestCode() {
        return requestCode++ % 65536;
    }

    private Context getContext() {
        if (fragment != null)
            return fragment.getContext();
        else
            return activity;
    }

    public static boolean checkSelfPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static Intent getPermissionsSettingsIntent() {
        Uri packageUri = Uri.parse(PACKAGE_SCHEME + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public boolean checkSelfPermission(String permission) {
        return checkSelfPermission(getContext(), permission);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionCallback callback = Objects.requireNonNull(map.remove(requestCode));
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback.onGranted();
        } else if (shouldShowRequestPermissionRationale(permissions[0])) {
            callback.onDenied(permissions[0]);
        } else
            callback.onForbidden(permissions[0]);
    }

    private boolean shouldShowRequestPermissionRationale(String permission) {
        boolean should = false;
        if (activity != null) {
            should = activity.shouldShowRequestPermissionRationale(permission);
        }
        if (fragment != null) {
            should = fragment.shouldShowRequestPermissionRationale(permission);
        }
        return should;
    }

    private void requestPermission(String permission, int requestCode) {
        if (activity != null) {
            activity.requestPermissions(new String[] { permission }, requestCode);
        }
        if (fragment != null) {
            fragment.requestPermissions(new String[] { permission }, requestCode);
        }
    }

    public void check(PermissionCallback callback, String... permissions) { }

    public void check(String permission, PermissionCallback callback) {
        if (checkSelfPermission(permission)) {
            callback.onGranted();
        }  else if (shouldShowRequestPermissionRationale(permission)) {
            int requestCode = getNextRequestCode();
            map.put(requestCode, callback);
            requestPermission(permission, requestCode);
        } else
            callback.onForbidden(permission);
    }
}