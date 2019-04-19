package io.atomofiron.devtiles.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.atomofiron.devtiles.I;

public class Cmd {
    public static final String SHELL = "sh";
    public static final String SU = "su";
    public static final int SUCCESS_CODE = 0;

    public static boolean run(String cmd) {
        try {
            log("exec()");
            Process process = Runtime.getRuntime().exec(cmd);
            log("close()");
            process.getOutputStream().close();
            log("waitFor()");
            return process.waitFor() == SUCCESS_CODE;
        } catch (Exception e) {
            log("exc: " + e.toString());
            return false;
        }
    }

    public static Result run(String shell, String... args) {
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");

            sb.append(args[i]);
        }
        log("run (" + shell + "): " + sb.append(" ]").toString());

        boolean success;
        Process process;

        try {
            log("exec()");
            process = Runtime.getRuntime().exec(shell);
            OutputStream os = process.getOutputStream();
            for (String arg : args) {
                log("write()");
                os.write((arg + "\n").getBytes());
            }
            log("flush()");
            os.flush();
            log("close()");
            os.close();
            log("waitFor()");
            success = process.waitFor() == SUCCESS_CODE;
            log("success: " + success);

            String input = readStream(process.getInputStream());
            log("input:\n" + input);
            String error = readStream(process.getErrorStream());
            log("error:\n" + error);

            return new Result(success, success ? input : error);
        } catch (Exception exception) {
            log("exc: " + exception.toString());
            return new Result(exception.toString());
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        byte[] buf = new byte[8];
        int red;
        StringBuilder builder = new StringBuilder();
        while ((red = stream.read(buf)) > 0) {
            log("red: " + red);
            builder.append(new String(buf, 0, red));
        }
        stream.close();

        return builder.toString();
    }

    private static void log(String s) {
        if (I.LOGGING)
            I.log("Cmd." + s);
    }
}
