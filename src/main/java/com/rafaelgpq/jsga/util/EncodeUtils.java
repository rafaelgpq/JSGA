package com.rafaelgpq.jsga.util;

/**
 * Utility methods for encoding and decoding genotype forms including Gray code.
 */
public class EncodeUtils {

    /**
     * Converts an integer to a binary boolean array (LSB first).
     */
    public static boolean[] intToBinary(int value, int length) {
        if (length < 0 || length > Integer.SIZE) {
            throw new IllegalArgumentException("Binary length must be between zero and 32.");
        }
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            result[i] = ((value >>> i) & 1) != 0;
        }
        return result;
    }

    /**
     * Converts binary to Gray code.
     */
    public static boolean[] binaryToGray(boolean[] binary) {
        requireBits(binary, "Binary input");
        boolean[] gray = new boolean[binary.length];
        for (int i = 0; i < binary.length; i++) {
            gray[i] = binary[i] ^ (i + 1 < binary.length && binary[i + 1]);
        }
        return gray;
    }

    /**
     * Converts Gray code to binary.
     */
    public static boolean[] grayToBinary(boolean[] gray) {
        requireBits(gray, "Gray-coded input");
        boolean[] binary = new boolean[gray.length];
        boolean decoded = false;
        for (int i = gray.length - 1; i >= 0; i--) {
            decoded ^= gray[i];
            binary[i] = decoded;
        }
        return binary;
    }

    /**
     * Packs an unpacked boolean array into a packed byte array.
     * @param unpacked boolean[] with one bit per entry
     * @param full number of full bytes
     * @param slop number of bits not filling a full byte
     */
    public static byte[] pack(boolean[] unpacked, int full, int slop) {
        requireBits(unpacked, "Unpacked input");
        if (full < 0 || slop < 0 || slop >= Byte.SIZE
                || unpacked.length != full * Byte.SIZE + slop) {
            throw new IllegalArgumentException("Full-byte and slop counts must match the unpacked bit length.");
        }
        int totalBytes = full + (slop > 0 ? 1 : 0);
        byte[] packed = new byte[totalBytes];
        int index = 0;

        for (int i = 0; i < full; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                if (unpacked[index++]) b |= (1 << j);
            }
            packed[i] = b;
        }

        if (slop > 0) {
            byte b = 0;
            for (int j = 0; j < slop; j++) {
                if (unpacked[index++]) b |= (1 << j);
            }
            packed[full] = b;
        }

        return packed;
    }

    /**
     * Converts a boolean array to an integer (LSB first).
     */
    public static int binaryToInt(boolean[] binary) {
        requireBits(binary, "Binary input");
        if (binary.length > Integer.SIZE) {
            throw new IllegalArgumentException("Binary input must not exceed 32 bits.");
        }
        int value = 0;
        for (int i = binary.length - 1; i >= 0; i--) {
            value = (value << 1) | (binary[i] ? 1 : 0);
        }
        return value;
    }

    private static void requireBits(boolean[] bits, String name) {
        if (bits == null) {
            throw new IllegalArgumentException(name + " must not be null.");
        }
    }
}
