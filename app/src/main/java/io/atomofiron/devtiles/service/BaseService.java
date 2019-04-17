package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import io.atomofiron.devtiles.util.AsyncRuntime;
import io.atomofiron.devtiles.util.Callback;
import io.atomofiron.devtiles.I;
import io.atomofiron.devtiles.util.Cmd;
import io.atomofiron.devtiles.util.Result;

@SuppressLint("NewApi")
public abstract class BaseService extends TileService implements Callback {
    protected enum State {
        ACTIVE(Tile.STATE_ACTIVE),
        ACTIVATING(Tile.STATE_UNAVAILABLE),
        INACTIVE(Tile.STATE_INACTIVE),
        INACTIVATING(Tile.STATE_ACTIVE),
        UNAVAILABLE(Tile.STATE_UNAVAILABLE);

        final int state;

        State(int state) {
            this.state = state;
        }
    }

    private static final String IS_SU = "IS_SU";
    private static final String NO_SU = "NO_SU";
    protected static final String SU_CHECK = "su -c echo " + IS_SU + " || echo " + NO_SU;
    private static final int UNDEFINED = -1;

    private SharedPreferences sp;
    private final String KEY_SU_GRANTED = "KEY_SU_GRANTED_FOR_" + getName().toUpperCase();

    private boolean suGranted = false;
    private State state = null;

    protected boolean needSu = false;
    protected int unavailableIconResId = UNDEFINED;
    protected int defaultIconResId = UNDEFINED;

    @Override
    public void onCreate() {
        super.onCreate();

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        suGranted = sp.getBoolean(KEY_SU_GRANTED, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sp.edit().putBoolean(KEY_SU_GRANTED, suGranted).apply();
    }

    @Override
    public final void onClick() {
        log("onClick");
        if (state != State.ACTIVE & state != State.INACTIVE) return;
        if (needSu && !isSuGranted()) return;

        onClick(state == State.ACTIVE);
    }

    protected abstract void onClick(boolean isActive);

    protected abstract void onUpdate();

    @Override
    public final void onStartListening() {
        log("onStartListening()");

        if (state == null) {
            state = (getQsTile().getState() == Tile.STATE_ACTIVE) ? State.ACTIVE : State.INACTIVE;
        }

        int iconResId = getQsTile().getIcon().getResId();
        if (defaultIconResId == UNDEFINED && iconResId != unavailableIconResId) {
            defaultIconResId = iconResId;
        }

        log("onUpdate");
        onUpdate();

        if (needSu && !suGranted && !checkSuGranted()) {
            log("!checkSuGranted()");
            updateTile((state == State.ACTIVE) ? State.INACTIVATING : State.UNAVAILABLE);
        }
    }

    @Override
    public final void onStopListening() {
    }

    @Override
    public final void onTileAdded() {
    }

    @Override
    public final void onTileRemoved() {
    }

    public void onResult(Result result) {
        log("onResult: " + result.success);

        if (needSu && result.message != null) {
            if (result.message.startsWith(IS_SU))
                suGranted = true;
            else if (result.message.startsWith(NO_SU))
                suGranted = false;
        }
    }

    protected final void run(String... cmd) {
        new AsyncRuntime(this).execute(cmd);
    }

    protected final boolean isSuGranted() {
        log("isSuGranted: " + suGranted);
        return suGranted;
    }

    protected final boolean checkSuGranted() {
        suGranted = Cmd.run(Cmd.SU);
        log("checkSuGranted: " + suGranted);
        return suGranted;
    }

    protected final void updateTile(boolean activate) {
        updateTile(activate ? State.ACTIVE : State.INACTIVE);
    }

    protected final void updateTile(State state) {
        updateTile(state, getQsTile().getLabel());
    }

    protected final void updateTile(State state, CharSequence description) {
        this.state = state;

        Tile tile = getQsTile();
        int iconResId = (state == State.INACTIVATING) ? unavailableIconResId : defaultIconResId;

        if (tile.getIcon().getResId() != iconResId)
            tile.setIcon(Icon.createWithResource(getBaseContext(), iconResId));
        //desc tile.setContentDescription(description);
        tile.setState(state.state);
        tile.setLabel(description);
        tile.updateTile();
    }

    private String getName() {
        return getClass().getSimpleName();
    }

    protected final void log(String s) {
        if (I.LOGGING)
            I.log(getName() + "." + s);
    }
}
