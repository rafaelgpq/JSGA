package org.example.jsga.util;

/**
 * Utility methods for encoding and decoding genotype forms including Gray code.
 */
public class EncodeUtils {

    /**
     * Converts an integer to a binary boolean array (LSB first).
     */
    public static boolean[] intToBinary(int value, int length) {
        boolean[] result = new boolean[length];
        for (int i = length - 1; i >= 0; i--) {
            result[i] = (value & 1) != 0;
            value >>= 1;
        }
        return result;
    }

    /**
     * Converts binary to Gray code.
     */
    public static boolean[] binaryToGray(boolean[] binary) {
        boolean[] gray = new boolean[binary.length];
        gray[0] = binary[0];
        for (int i = 1; i < binary.length; i++) {
            gray[i] = binary[i - 1] ^ binary[i];
        }
        return gray;
    }

    /**
     * Converts Gray code to binary.
     */
    public static boolean[] grayToBinary(boolean[] gray) {
        boolean[] binary = new boolean[gray.length];
        binary[0] = gray[0];
        for (int i = 1; i < gray.length; i++) {
            binary[i] = binary[i - 1] ^ gray[i];
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
        int value = 0;
        for (int i = binary.length - 1; i >= 0; i--) {
            value = (value << 1) | (binary[i] ? 1 : 0);
        }
        return value;
    }
}
