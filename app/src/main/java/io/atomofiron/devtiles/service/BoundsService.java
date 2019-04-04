package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.service.quicksettings.Tile;

import io.atomofiron.devtiles.util.Result;

@SuppressLint("NewApi")
public class BoundsService extends BaseService {
    private static final String SET_PROP = "su -c setprop debug.layout %1$s && service call activity 1599295570 1>/dev/null";
    private static final String GET_PROP = "getprop debug.layout";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    {
        needSu = true;
    }

    @Override
    public void onClick() {
        log("onClick");

        if (!isSuGranted()) return;

        String enable;
        switch (getQsTile().getState()) {
            case Tile.STATE_INACTIVE:
                enable = TRUE;
                break;
            case Tile.STATE_ACTIVE:
                enable = FALSE;
                break;
            default:
                return;
        }

        updateTile(Tile.STATE_UNAVAILABLE);

        run(String.format(SET_PROP, enable), GET_PROP);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if (isSuGranted())
            run(GET_PROP);
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.message == null) {
            updateTile(Tile.STATE_INACTIVE);
        } else if (result.message.equals(TRUE)) {
            updateTile(Tile.STATE_ACTIVE);
        } else {
            updateTile(Tile.STATE_INACTIVE);
        }
    }
}
