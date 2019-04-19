package io.atomofiron.devtiles.util;

import android.os.AsyncTask;

import io.atomofiron.devtiles.I;

public class AsyncRuntime extends AsyncTask<String, Void, Result> {

    private final String shell;
    private final Callback callback;

    public AsyncRuntime(Callback callback) {
        this.shell = Cmd.SHELL;
        this.callback = callback;
    }

    public AsyncRuntime(String shell, Callback callback) {
        this.shell = shell;
        this.callback = callback;
    }

    @Override
    protected Result doInBackground(String... args) {
        return Cmd.run(shell, args);
    }

    @Override
    protected void onPostExecute(Result result) {
        callback.onResult(result);
    }


    private void log(String s) {
        if (I.LOGGING)
            I.log(getClass().getSimpleName() + "." + s);
    }
}
