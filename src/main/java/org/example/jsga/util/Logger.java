package org.example.jsga.util;

/**
 * Simple logger for trace/debug output.
 */
public class Logger {

    private static boolean enabled = false;

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static void trace(String message) {
        if (enabled) {
            System.out.println("[TRACE] " + message);
        }
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}
