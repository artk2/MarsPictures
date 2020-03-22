package android.util;

/**
 * https://stackoverflow.com/a/46793567
 * Log class mock
 */
public class Log {
    public static int d(String tag, String msg) {
        System.out.println("DEBUG: " + msg);
        return 0;
    }

    public static int i(String tag, String msg) {
        System.out.println("INFO: " + msg);
        return 0;
    }

    public static int w(String tag, String msg) {
        System.out.println("WARN: " + msg);
        return 0;
    }

    public static int e(String tag, String msg) {
        System.out.println("ERROR: " + msg);
        return 0;
    }

    public static int v(String tag, String msg) {
        System.out.println("VERBOSE: " + msg);
        return 0;
    }

    // add other methods if required...
}