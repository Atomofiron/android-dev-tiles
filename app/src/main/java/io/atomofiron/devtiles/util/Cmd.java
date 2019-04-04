package io.atomofiron.devtiles.util;

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

            byte[] buf = new byte[8];
            InputStream stream;
            if (success) {
                stream = process.getInputStream();
            } else {
                stream = process.getErrorStream();
            }
            int red;
            StringBuilder builder = new StringBuilder();
            while ((red = stream.read(buf)) > 0) {
                log("red: " + red);
                builder.append(new String(buf, 0, red));
            }
            stream.close();

            String message = builder.toString();
            log("message:\n" + message);
            return new Result(success, message);
        } catch (Exception exception) {
            log("exc: " + exception.toString());
            return new Result(exception.toString());
        }
    }

    private static void log(String s) {
        if (I.LOGGING)
            I.log("Cmd." + s);
    }
}
