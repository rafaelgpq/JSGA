package org.example.jsga.util;

/**
 * Centralized logging and error handling utility.
 */
public class LogManager {

    private static boolean traceEnabled = false;
    private static boolean quietMode = false;

    public static void enableTrace() {
        traceEnabled = true;
    }

    public static void disableTrace() {
        traceEnabled = false;
    }

    public static void setQuietMode(boolean quiet) {
        quietMode = quiet;
    }

    public static void fatal(String message) {
        throw new RuntimeException("[FATAL] " + message);
    }

    public static void warn(String message) {
        if (!quietMode) System.err.println("[WARNING] " + message);
    }

    public static void error(String message) {
        if (!quietMode) System.err.println("[ERROR] " + message);
    }

    public static void info(String message) {
        if (!quietMode) System.out.println("[INFO] " + message);
    }

    public static void trace(String message) {
        if (traceEnabled && !quietMode) {
            System.out.println("[TRACE] " + message);
        }
    }

    public static void configureFromProperties(java.util.Properties props) {
        traceEnabled = Boolean.parseBoolean(props.getProperty("trace", "false"));
        quietMode = Boolean.parseBoolean(props.getProperty("quiet", "false"));
    }
}
