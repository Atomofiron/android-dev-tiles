package io.atomofiron.devtiles.util.permission;

public abstract class PermissionCallback {
    public void onGranted() { }
    public void onDenied(String permission) { }
    public void onForbidden(String permission) { }
}
