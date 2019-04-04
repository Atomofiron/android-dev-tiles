package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.service.quicksettings.Tile;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

@SuppressLint("NewApi")
public class AdbTcpIpService extends BaseService {
    private static final String SET_PROP = "su -c setprop service.adb.tcp.port %s && stop adbd && start adbd";
    private static final String GET_IP_AND_PROP = "ip route show; getprop service.adb.tcp.port";
    private static final String GET_IP_WIFI = "ip route show dev `getprop wifi.interface`";

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "5555";
    private static final String DISABLE_PORT = "-1";

    private final Pattern ipPattern = Pattern.compile("(?<=src )[0-9.]+(?= ?)");

    public String port = DEFAULT_PORT;

    {
        needSu = true;
    }

    @Override
    public void onClick() {
        super.onClick();

        if (!isSuGranted()) return;

        String port;
        switch (getQsTile().getState()) {
            case Tile.STATE_INACTIVE:
                port = this.port;
                break;
            case Tile.STATE_ACTIVE:
                port = DISABLE_PORT;
                break;
            default:
                return;
        }

        updateTile(Tile.STATE_UNAVAILABLE);

        run(String.format(SET_PROP, port), GET_IP_AND_PROP);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if (isSuGranted())
            run(GET_IP_AND_PROP);
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.message == null) {
            updateTile(Tile.STATE_INACTIVE, "");

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
            updateTile(Tile.STATE_ACTIVE, ip + ":" + port);
        } else {
            //desc updateTile(Tile.STATE_INACTIVE, "");
            updateTile(Tile.STATE_INACTIVE, getString(R.string.adb_over_network));
        }
    }
}
