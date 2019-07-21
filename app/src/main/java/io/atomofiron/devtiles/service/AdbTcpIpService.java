package io.atomofiron.devtiles.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;
import io.atomofiron.devtiles.util.permission.Permissions;

public class AdbTcpIpService extends BaseService {
    private static final String KEY_LAST_TRUSTED_AP = "KEY_LAST_TRUSTED_AP";
    private static final String KEY_TURNED_OFF = "KEY_TURNED_OFF";
    private static final int SETTINGS_REQUEST_CODE = 2345;
    public static final int SETTINGS_NOTIFICATION_ID = 2345;

    private static final String SET_PROP = "su -c \"setprop service.adb.tcp.port %s && stop adbd && start adbd\"";
    private static final String GET_IP_AND_PROP = "ip route show; getprop service.adb.tcp.port";
    private static final String GET_IP_WIFI = "ip route show dev `getprop wifi.interface`";

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "5555";
    private static final String DISABLE_PORT = "-1";

    private final Pattern ipPattern = Pattern.compile("(?<=src )[0-9.]+(?= ?)");

    public String port = DEFAULT_PORT;

    private WifiManager wifiManager;
    private SharedPreferences sp;

    {
        needSu = true;
        setIcons(R.drawable.ic_qs_adb_tcpip, R.drawable.ic_qs_adb_tcpip_unavailable);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onClick(boolean isActive) {
        setState(!isActive);
    }

    private void setState(boolean enabled) {
        String port = enabled ? this.port : DISABLE_PORT;

        updateTile(enabled ? State.ACTIVATING : State.INACTIVATING);
        sp.edit().putBoolean(KEY_TURNED_OFF, !enabled).apply();

        runWithSu(String.format(SET_PROP, port), GET_IP_AND_PROP);
    }

    @Override
    protected void onUpdate() {
        run(GET_IP_AND_PROP);

        checkWifi();
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (result.output == null) {
            log("onResult: WTF result.output == null");
            updateTile(State.INACTIVE);
        } else {
            int port = parsePort(result.output);
            if (port > 0) {
                String ip = parseIp(result.output);

                updateTile(isSuGranted() ? State.ACTIVE : State.INACTIVATING, ip + ":" + port);
            } else {
                updateTile(isSuGranted() ? State.INACTIVE : State.UNAVAILABLE, getString(R.string.adb_over_network));
            }
        }
    }

    private String parseIp(String output) {
        String ip = LOCALHOST;
        Matcher matcher = ipPattern.matcher(output);
        while (matcher.find()) {
            ip = matcher.group();
            log("parseIp: ip = " + ip);
        }
        return ip;
    }

    private int parsePort(String output) {
        try {
            int start = output.lastIndexOf('\n') + 1;
            int length = output.length();
            String stringPort = output.substring(start, length);
            log("parsePort: stringPort = " + stringPort);
            return Integer.parseInt(stringPort);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void checkWifi() {
        boolean autoEnable = sp.getBoolean(getString(R.string.pref_key_auto_enable_adb), false);
        String lastTrustedAp = sp.getString(KEY_LAST_TRUSTED_AP, null);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!autoEnable) {
            manager.cancel(SETTINGS_NOTIFICATION_ID);
            return;
        }

        boolean granted = Permissions.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!granted) {
            showPermissionNotification(manager);
            return;
        }
        manager.cancel(SETTINGS_NOTIFICATION_ID);

        boolean autoDisable = sp.getBoolean(getString(R.string.pref_key_auto_disable_adb), false);
        boolean turnedOff = sp.getBoolean(KEY_TURNED_OFF, false);
        List<String> aps = Arrays.asList(sp.getString(getString(R.string.pref_key_for_aps), "").split("[\n]+"));
        int state = getQsTile().getState();

        String ssid = wifiManager.getConnectionInfo().getSSID();
        ssid = ssid.substring(1, ssid.length() - 1); // remove ""

        String currentTrustedAp = (aps != null && aps.contains(ssid)) ? ssid : null;
        sp.edit().putString(KEY_LAST_TRUSTED_AP, currentTrustedAp).apply();

        // autoEnable == true
        if (state == Tile.STATE_INACTIVE && currentTrustedAp != null && !turnedOff) {
            setState(true);
        } else if (autoDisable && lastTrustedAp != null && currentTrustedAp == null) {
            if (state == Tile.STATE_ACTIVE)
                setState(false);

            sp.edit().remove(KEY_TURNED_OFF).apply();
        }
    }

    private void showPermissionNotification(NotificationManager manager) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder = new Notification.Builder(this, getString(R.string.permission_channel_id));
        else
            builder = new Notification.Builder(this);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                SETTINGS_REQUEST_CODE,
                Permissions.getPermissionsSettingsIntent(),
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(getString(R.string.need_coarse_location));
        builder.setSmallIcon(Icon.createWithResource(this, R.drawable.ic_adbtcp_small));
        Notification notification = builder.build();

        createChannelIfNeeded(manager);
        manager.notify(SETTINGS_NOTIFICATION_ID, notification);
    }

    private void createChannelIfNeeded(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.permission_channel_id);
            NotificationChannel channel = manager.getNotificationChannel(channelId);
            if (channel == null) {
                String channelName = getString(R.string.permission_channel_name);
                channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
                manager.createNotificationChannel(channel);
            }
        }
    }
}
