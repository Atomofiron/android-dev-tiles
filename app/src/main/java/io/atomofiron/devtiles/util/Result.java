package io.atomofiron.devtiles.util;

public class Result {
    public final boolean success;
    public final String output;
    public final String error;

    public Result(boolean success) {
        this.success = success;
        this.output = null;
        this.error = null;
    }

    public Result(String error) {
        this.success = false;
        this.output = null;
        this.error = fix(error);
    }

    public Result(boolean success, String output, String error) {
        this.success = success;
        this.output = fix(output);
        this.error = fix(error);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return "[success: " + success + ", output: " + output + ", error: " + error + "]";
    }

    private String fix(String message) {
        if (message != null && message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        return message;
    }
}
