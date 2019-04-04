package io.atomofiron.devtiles.util;

public class Result {
    public final boolean success;
    public final String message;

    public Result(boolean success) {
        this.success = success;
        this.message = null;
    }

    public Result(String message) {
        this.success = false;
        this.message = fix(message);
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = fix(message);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return "[success: " + success + ", message: " + message + "]";
    }

    private String fix(String message) {
        if (message != null && message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        return message;
    }
}
