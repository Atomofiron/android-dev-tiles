package io.atomofiron.devtiles.service;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

public class AdbTcpIpService extends BaseService {
    private static final String SET_PROP = "setprop service.adb.tcp.port %s && stop adbd && start adbd";
    private static final String GET_IP_AND_PROP = "ip route show; getprop service.adb.tcp.port";
    private static final String GET_IP_WIFI = "ip route show dev `getprop wifi.interface`";

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "5555";
    private static final String DISABLE_PORT = "-1";

    private final Pattern ipPattern = Pattern.compile("(?<=src )[0-9.]+(?= ?)");

    public String port = DEFAULT_PORT;

    private WifiManager wifiManager;
    private SharedPreferences sp;

    private String currentTrustedAp = null;

    {
        needSu = true;
        unavailableIconResId = R.drawable.ic_qs_adb_tcpip_unavailable;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onClick(boolean isActive) {
        enable(!isActive);
    }

    private void enable(boolean enable) {
        String port = enable ? this.port : DISABLE_PORT;

        updateTile(enable ? State.ACTIVATING : State.INACTIVATING);

        runAsSu(SU_CHECK, String.format(SET_PROP, port), GET_IP_AND_PROP);
    }

    @Override
    protected void onUpdate() {
        run(GET_IP_AND_PROP);

        checkWifi();
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.message == null) {
            updateTile(State.INACTIVE);

            String message = (result.message == null) ? getString(R.string.error) : result.message;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }

        String ip = LOCALHOST;
        Matcher matcher = ipPattern.matcher(result.message);
        while (matcher.find()) {
            ip = matcher.group();
        }

        if (result.message.endsWith(port)) {
            updateTile(isSuGranted() ? State.ACTIVE : State.INACTIVATING, ip + ":" + port);
        } else {
            //desc updateTile(Tile.STATE_INACTIVE, "");
            updateTile(State.INACTIVE, getString(R.string.adb_over_network));
        }
    }

    private void checkWifi() {
        boolean granted = true; // todo

        if (!granted) return;

        boolean autoEnable = sp.getBoolean(getString(R.string.pref_key_auto_enable_adb), false);
        boolean autoDisable = sp.getBoolean(getString(R.string.pref_key_auto_disable_adb), false);
        String aps = sp.getString(getString(R.string.pref_key_for_aps), "");
        int state = getQsTile().getState();

        String ssid = wifiManager.getConnectionInfo().getSSID();
        ssid = ssid.substring(1, ssid.length() - 2); // remove ""

        String lastTrustedAp = currentTrustedAp;
        currentTrustedAp = (aps != null && aps.contains(ssid)) ? ssid : null;

        if (autoEnable && state == Tile.STATE_INACTIVE && currentTrustedAp != null) {
            enable(true);
        } else if (autoDisable && state == Tile.STATE_ACTIVE &&
                lastTrustedAp != null && currentTrustedAp == null) {
            enable(false);
        }
    }
}
