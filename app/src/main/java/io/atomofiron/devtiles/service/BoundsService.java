package io.atomofiron.devtiles.service;

import android.os.SystemProperties;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

public class BoundsService extends BaseService {
    private static final String PROP = "debug.layout";
    private static final String SET_PROP = "su -c setprop " + PROP + " %1$s && service call activity 1599295570 1>/dev/null";
    private static final String GET_PROP = "getprop " + PROP;

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    {
        needSu = true;
        unavailableIconResId = R.drawable.ic_qs_layout_bounds_unavailable;
    }

    @Override
    public void onClick(boolean isActive) {
        String enable = isActive ? FALSE : TRUE;

        try {
            SystemProperties.set(PROP, enable);
        } catch (Exception e) {
            log("exc: " + e.toString());

            updateTile(isActive ? State.INACTIVATING : State.ACTIVATING);

            run(SU_CHECK, String.format(SET_PROP, enable), GET_PROP);
        }
    }

    @Override
    protected void onUpdate() {
        try {
            String state = SystemProperties.get(PROP);
            log("state: " + state);
            updateTile(Boolean.parseBoolean(state) ? State.ACTIVE : State.INACTIVE);
        } catch (Exception e) {
            log("exc: " + e.toString());

            run(GET_PROP);
        }
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.message == null) {
            updateTile(State.INACTIVE);
        } else if (result.message.endsWith(TRUE)) {
            updateTile(State.ACTIVE);
        } else {
            updateTile(State.INACTIVE);
        }
    }
}
