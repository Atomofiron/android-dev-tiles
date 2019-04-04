package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

@SuppressLint("NewApi")
public class BoundsService extends BaseService {
    private static final String SET_PROP = "su -c setprop debug.layout %1$s && sleep 3; service call activity 1599295570 1>/dev/null";
    private static final String GET_PROP = "getprop debug.layout";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    {
        needSu = true;
        unavailableIconResId = R.drawable.ic_qs_layout_bounds_unavailable;
    }

    @Override
    public void onClick(boolean isActive) {
        String enable = isActive ? FALSE : TRUE;

        updateTile(isActive ? State.INACTIVATING : State.ACTIVATING);

        run(String.format(SET_PROP, enable), GET_PROP);
    }

    @Override
    protected void onUpdate() {
        run(GET_PROP);
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.message == null) {
            updateTile(State.INACTIVE);
        } else if (result.message.equals(TRUE)) {
            updateTile(State.ACTIVE);
        } else {
            updateTile(State.INACTIVE);
        }
    }
}
