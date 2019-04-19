package io.atomofiron.devtiles.service;

import android.os.SystemProperties;

public abstract class PropertiesService extends BaseService {
    private final String PROP;
    private final String SET_PROP;
    private final String GET_PROP;

    protected static final String TRUE = "true";
    protected static final String FALSE = "false";

    {
        PROP = getProp();
        SET_PROP = "setprop " + PROP + " %s %s";
        GET_PROP = "getprop " + PROP;
    }

    protected abstract String getProp();

    protected String getSetterCommandSuffix() {
        return "";
    }

    protected abstract String getValueForState(boolean activate);

    protected abstract boolean getStateForValue(String value);

    @Override
    public final void onClick(boolean isActive) {
        String value = getValueForState(!isActive);

        try {
            SystemProperties.set(PROP, value);

            needSu = false;
        } catch (Exception e) {
            log("exc: " + e.toString());

            needSu = true;

            updateTile(isActive ? State.INACTIVATING : State.ACTIVATING);

            runAsSu(String.format(SET_PROP, value, getSetterCommandSuffix()), GET_PROP);
        }
    }

    @Override
    protected void onUpdate() {
        log("onUpdate()");

        try {
            String state = SystemProperties.get(PROP);
            log("state: " + state);
            updateTile(getStateForValue(state) ? State.ACTIVE : State.INACTIVE);
        } catch (Exception e) {
            log("exc: " + e.toString());

            run(GET_PROP);
        }
    }
}
