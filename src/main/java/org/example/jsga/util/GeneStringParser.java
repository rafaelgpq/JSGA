package org.example.jsga.util;

/**
 * Parses and validates gene string inputs (e.g., for testing, debugging, or user override).
 */
public class GeneStringParser {

    /**
     * Parses a binary string like "110101" into a boolean array.
     */
    public static boolean[] parseBinaryString(String bits) {
        boolean[] result = new boolean[bits.length()];
        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            if (c == '1') result[i] = true;
            else if (c == '0') result[i] = false;
            else throw new IllegalArgumentException("Invalid gene character: " + c);
        }
        return result;
    }

    /**
     * Returns true if the input string contains only 0s and 1s.
     */
    public static boolean isValidGeneString(String input) {
        return input.matches("[01]+");
    }
}
