package com.artk.gallery.app;

import com.artk.gallery.BuildConfig;

public class Log {

    private static final String TAG = "artk2";

    public static void e(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(TAG, getCaller() + ": " + msg);
        }
    }

    public static void e(String msg, Throwable t) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(TAG, getCaller() + ": " + msg, t);
        }
    }

    public static void e(Throwable t) {
        if (BuildConfig.DEBUG) {
            android.util.Log.e(TAG, getCaller() + ": " + t.getLocalizedMessage(), t);
        }
    }

    public static void w(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, getCaller() + ": " + msg);
        }
    }

    public static void i(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, getCaller() + ": " + msg);
        }
    }

    public static void d(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, getCaller() + ": " + msg);
        }
    }

    public static void v(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, getCaller() + ": " + msg);
        }
    }

    public static void wtf(String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.wtf(TAG, getCaller() + ": " + msg);
        }
    }

    private static String getCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length < 5) return "";

        // 0: dalvik.system.VMStack.getThreadStackTrace
        // 1: java.lang.Thread.getStackTrace
        // 2: Log.getCaller (this method)
        // 3: Log.v (or other log method of this class)
        // 4: the actual caller

        String className = stackTraceElements[4].getClassName();
        String method = stackTraceElements[4].getMethodName();

        String classTrimmed = className.replace(BuildConfig.APPLICATION_ID + ".", "");
        String methodTrimmed = method.replace("lambda$", "");
        int s = methodTrimmed.indexOf('$');
        if (s >= 0) methodTrimmed = methodTrimmed.substring(0, s);

        return classTrimmed + "." + methodTrimmed;
    }

}
