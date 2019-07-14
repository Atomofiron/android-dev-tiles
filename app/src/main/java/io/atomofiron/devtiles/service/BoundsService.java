package io.atomofiron.devtiles.service;

import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.Result;

public class BoundsService extends PropertiesService {

    {
        needSu = true;
        setIcons(R.drawable.ic_qs_layout_bounds, R.drawable.ic_qs_layout_bounds_unavailable);
    }

    @Override
    protected String getProp() {
        return "debug.layout";
    }

    @Override
    protected String getSetterCommandSuffix() {
        return " && service call activity 1599295570 1>/dev/null";
    }

    @Override
    protected String getValueForState(boolean activate) {
        return activate ? TRUE : FALSE;
    }

    @Override
    protected boolean getStateForValue(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (!result.success || result.output == null) {
            updateTile(State.INACTIVE);
        } else if (result.output.endsWith(TRUE)) {
            updateTile(State.ACTIVE);
        } else {
            updateTile(State.INACTIVE);
        }
    }
}
