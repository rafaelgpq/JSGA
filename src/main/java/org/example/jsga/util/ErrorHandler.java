package org.example.jsga.util;

/**
 * Centralized error reporting and termination utility.
 */
public class ErrorHandler {

    /**
     * Throws a runtime exception with the given error message.
     */
    public static void fatal(String message) {
        throw new RuntimeException("[FATAL] " + message);
    }

    /**
     * Logs a warning to the console.
     */
    public static void warn(String message) {
        System.err.println("[WARNING] " + message);
    }

    /**
     * Logs an error to the console.
     */
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    /**
     * Logs info to stdout.
     */
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
}

