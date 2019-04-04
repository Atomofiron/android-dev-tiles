package io.atomofiron.devtiles.service;

import android.annotation.SuppressLint;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import io.atomofiron.devtiles.util.AsyncRuntime;
import io.atomofiron.devtiles.util.Callback;
import io.atomofiron.devtiles.I;
import io.atomofiron.devtiles.util.Cmd;
import io.atomofiron.devtiles.util.Result;

@SuppressLint("NewApi")
public abstract class BaseService extends TileService implements Callback {
    protected boolean needSu = false;
    private boolean suGranted = false;

    @Override
    public void onStartListening() {
        super.onStartListening();
        log("onStartListening()");

        if (needSu && !checkSuGranted()) {
            updateTile(Tile.STATE_UNAVAILABLE);
        }
    }

    public void run(String... cmd) {
        log("run: " + cmd[0]);
        new AsyncRuntime(this).execute(cmd);
    }

    public void onResult(Result result) {
        log("onResult: " + result.success);
    }

    protected final boolean isSuGranted() {
        log("isSuGranted: " + suGranted);
        return suGranted;
    }

    private boolean checkSuGranted() {
        suGranted = Cmd.run(Cmd.SU);
        log("checkSuGranted: " + suGranted);
        return suGranted;
    }

    protected void updateTile(boolean activate) {
        updateTile(activate ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
    }

    protected void updateTile(int state) {
        //desc updateTile(state, getQsTile().getContentDescription());
        updateTile(state, getQsTile().getLabel());
    }

    protected void updateTile(CharSequence description) {
        updateTile(getQsTile().getState(), description);
    }

    protected void updateTile(int state, CharSequence description) {
        Tile tile = getQsTile();
        tile.setState(state);
        //desc tile.setContentDescription(description);
        tile.setLabel(description);
        tile.updateTile();
    }

    protected void log(String s) {
        if (I.LOGGING)
            I.log(getClass().getSimpleName() + "." + s);
    }
}
