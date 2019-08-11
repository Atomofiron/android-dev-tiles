package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import io.atomofiron.devtiles.BuildConfig;
import io.atomofiron.devtiles.R;
import io.atomofiron.devtiles.util.AsyncRuntime;
import io.atomofiron.devtiles.util.Callback;
import io.atomofiron.devtiles.util.Cmd;
import io.atomofiron.devtiles.util.L;
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
    // todo попробовать заменить на waitFor()
    protected static final String SU_CHECK = "su -c echo " + IS_SU + " 2>/dev/null || echo " + NO_SU;
    private static final int UNDEFINED = -1;

    private SharedPreferences sp;
    private final String KEY_SU_GRANTED = "KEY_SU_GRANTED_FOR_" + getClass().getSimpleName().toUpperCase();

    private boolean suGranted = false;
    private State state = null;

    protected boolean needSu = false;
    private int unavailableIconResId = UNDEFINED;
    private int defaultIconResId = UNDEFINED;

    @Override
    public void onCreate() {
        super.onCreate();

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        suGranted = sp.getBoolean(KEY_SU_GRANTED, false);
    }

    protected void setIcons(int defaultIconResId, int unavailableIconResId) {
        if (defaultIconResId == UNDEFINED) {
            throw new IllegalArgumentException("defaultIconResId == -1");
        }
        if (unavailableIconResId == UNDEFINED) {
            throw new IllegalArgumentException("unavailableIconResId == -1");
        }
        this.defaultIconResId = defaultIconResId;
        this.unavailableIconResId = unavailableIconResId;
    }

    @Override
    public final void onClick() {
        log("onClick");
        /* state должет быть уже инициализирован в onUpdate()
         */
        if (state != State.ACTIVE && state != State.INACTIVE) return;
        if (needSu && !suGranted) return;

        onClick(state == State.ACTIVE);
    }

    protected abstract void onClick(boolean isActive);

    protected abstract void onUpdate();

    @Override
    public final void onStartListening() {
        log("onStartListening()");

        log("onUpdate");
        onUpdate();

        if (needSu && !suGranted && !checkSuGranted()) {
            log("!checkSuGranted()");

            if (state == State.ACTIVE)
                updateTile(State.INACTIVATING);

            if (state == State.INACTIVE)
                updateTile(State.UNAVAILABLE);
        }
    }

    @Override
    public final void onStopListening() {
        log("onStopListening()");
    }

    @Override
    public final void onTileAdded() {
    }

    @Override
    public final void onTileRemoved() {
    }

    public void onResult(Result result) {
        log("onResult: success = " + result.success);

        if (!result.success) {
            String message = (result.error == null) ? getString(R.string.error) : result.error;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        if (needSu && result.output != null) {
            if (result.output.startsWith(IS_SU))
                suGranted = true;
            else if (result.output.startsWith(NO_SU))
                suGranted = false;
        } else if (needSu) {
            // unreachable
            suGranted = false;
            log("onResult: WTF needSu && result.output == null");
        }
    }

    protected final void run(String... cmd) {
        new AsyncRuntime(this).execute(cmd);
    }

    protected final void runWithSu(String... cmd) {
        String[] arr = new String[cmd.length + 1];
        arr[0] = SU_CHECK;
        System.arraycopy(cmd, 0, arr, 1, cmd.length);
        new AsyncRuntime(this).execute(arr);
    }

    protected final boolean isSuGranted() {
        log("isSuGranted: " + suGranted);
        return suGranted;
    }

    protected final boolean checkSuGranted() {
        suGranted = Cmd.run(Cmd.SU);
        log("checkSuGranted: " + suGranted);
        sp.edit().putBoolean(KEY_SU_GRANTED, suGranted).apply();
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

        if (iconResId != UNDEFINED && tile.getIcon().getResId() != iconResId) {
            tile.setIcon(Icon.createWithResource(getBaseContext(), iconResId));
        }
        //desc tile.setContentDescription(description);
        tile.setState(state.state);
        tile.setLabel(description);
        tile.updateTile();
    }

    protected final void log(String s) {
        if (BuildConfig.DEBUG)
            L.log(this, s);
    }
}
