package io.atomofiron.devtiles.service;

import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

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
        unavailableIconResId = R.drawable.ic_qs_adb_tcpip_unavailable;
    }

    @Override
    public void onClick(boolean isActive) {
        String port = isActive ? DISABLE_PORT : this.port;

        updateTile(isActive ? State.INACTIVATING : State.ACTIVATING);

        run(String.format(SET_PROP, port), GET_IP_AND_PROP);
    }

    @Override
    protected void onUpdate() {
        run(GET_IP_AND_PROP);
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
            updateTile(State.ACTIVE, ip + ":" + port);
        } else {
            //desc updateTile(Tile.STATE_INACTIVE, "");
            updateTile(State.INACTIVE, getString(R.string.adb_over_network));
        }
    }
}
