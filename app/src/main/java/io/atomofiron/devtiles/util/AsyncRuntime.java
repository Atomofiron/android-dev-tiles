package io.atomofiron.devtiles.util;

import android.os.AsyncTask;

import io.atomofiron.devtiles.I;

public class AsyncRuntime extends AsyncTask<String, Void, Result> {

    private Callback callback;

    public AsyncRuntime(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Result doInBackground(String... args) {
        return Cmd.run(Cmd.SHELL, args);
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
