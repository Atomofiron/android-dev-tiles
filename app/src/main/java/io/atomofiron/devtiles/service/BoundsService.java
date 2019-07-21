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
    protected String getValueForState(boolean active) {
        return active ? TRUE : FALSE;
    }

    @Override
    protected boolean getStateForValue(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public void onResult(Result result) {
        super.onResult(result);

        if (result.output == null) {
            // unreachable
            updateTile(State.INACTIVE);
        } else if (result.output.endsWith(TRUE)) {
            if (needSu && !isSuGranted())
                updateTile(State.INACTIVATING);
            else
                updateTile(State.ACTIVE);
        } else if (result.output.endsWith(FALSE)) {
            if (needSu && !isSuGranted())
                updateTile(State.UNAVAILABLE);
            else
                updateTile(State.INACTIVE);
        } else {
            log("onResult: WTF not true not false");
            updateTile(State.UNAVAILABLE);
        }
    }
}
