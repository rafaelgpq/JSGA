package org.example.jsga.util;

/**
 * Utility methods for decoding packed gene arrays into usable forms.
 */
public class DecodeUtils {

    /**
     * Converts packed byte[] into an unpacked boolean[] bit array.
     */
    public static boolean[] unpack(byte[] packed, int fullBits, int slopBits) {
        boolean[] unpacked = new boolean[fullBits + slopBits];
        int index = 0;

        for (int i = 0; i < fullBits / 8; i++) {
            for (int j = 0; j < 8; j++) {
                unpacked[index++] = (packed[i] & (1 << j)) != 0;
            }
        }

        if (slopBits > 0 && fullBits / 8 < packed.length) {
            byte lastByte = packed[fullBits / 8];
            for (int j = 0; j < slopBits; j++) {
                unpacked[index++] = (lastByte & (1 << j)) != 0;
            }
        }

        return unpacked;
    }

    /**
     * Converts Gray-coded input to binary output.
     */
    public static boolean[] degray(boolean[] input) {
        boolean[] output = new boolean[input.length];
        boolean last = false;

        for (int i = 0; i < input.length; i++) {
            output[i] = input[i] ? !last : last;
            last = output[i];
        }

        return output;
    }

    /**
     * Converts binary boolean[] to a double value interpreted as an integer.
     */
    public static double binaryToDouble(boolean[] bits, boolean aliasFlag) {
        double value = 0.0;
        for (boolean bit : bits) {
            value = value * 2 + (bit ? 1 : 0);
        }
        if (aliasFlag) {
            value += Math.random(); // For random offset when aliasing
        }
        return value;
    }
}
