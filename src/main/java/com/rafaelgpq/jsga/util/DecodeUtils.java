package com.rafaelgpq.jsga.util;

/**
 * Utility methods for decoding packed gene arrays into usable forms.
 */
public class DecodeUtils {

    /**
     * Converts packed, least-significant-bit-first bytes into an unpacked bit array.
     *
     * @param fullBits number of bits in complete bytes; must be a multiple of eight
     * @param slopBits number of bits in the final partial byte
     */
    public static boolean[] unpack(byte[] packed, int fullBits, int slopBits) {
        if (packed == null) {
            throw new IllegalArgumentException("Packed gene must not be null.");
        }
        if (fullBits < 0 || fullBits % Byte.SIZE != 0) {
            throw new IllegalArgumentException("Full bit count must be a non-negative multiple of eight.");
        }
        if (slopBits < 0 || slopBits >= Byte.SIZE) {
            throw new IllegalArgumentException("Slop bit count must be between zero and seven.");
        }
        int expectedBytes = fullBits / Byte.SIZE + (slopBits == 0 ? 0 : 1);
        if (packed.length != expectedBytes) {
            throw new IllegalArgumentException("Packed gene must contain exactly " + expectedBytes + " bytes.");
        }

        boolean[] unpacked = new boolean[fullBits + slopBits];
        int index = 0;

        for (int i = 0; i < fullBits / Byte.SIZE; i++) {
            for (int j = 0; j < Byte.SIZE; j++) {
                unpacked[index++] = (packed[i] & (1 << j)) != 0;
            }
        }

        if (slopBits > 0) {
            byte lastByte = packed[fullBits / Byte.SIZE];
            for (int j = 0; j < slopBits; j++) {
                unpacked[index++] = (lastByte & (1 << j)) != 0;
            }
        }

        return unpacked;
    }

    public static boolean[] unpack(byte[] packed, int geneLength) {
        if (geneLength < 0) {
            throw new IllegalArgumentException("Gene length must not be negative.");
        }
        int fullBits = geneLength - geneLength % Byte.SIZE;
        return unpack(packed, fullBits, geneLength % Byte.SIZE);
    }

    /**
     * Converts Gray-coded input to binary output.
     */
    public static boolean[] degray(boolean[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Gray-coded input must not be null.");
        }
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
        if (bits == null) {
            throw new IllegalArgumentException("Binary input must not be null.");
        }
        double value = 0.0;
        for (boolean bit : bits) {
            value = value * 2 + (bit ? 1 : 0);
        }
        if (aliasFlag) {
            value += RandomUtils.nextDouble();
        }
        return value;
    }
}
