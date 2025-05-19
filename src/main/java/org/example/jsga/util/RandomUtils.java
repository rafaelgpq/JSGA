package org.example.jsga.util;

import java.util.Random;

/**
 * Provides utility methods for reproducible and centralized randomness.
 */
public class RandomUtils {

    private static Random globalRandom = new Random();

    // Called by MainSimulator to seed the RNG
    public static void initialize(long seed) {
        globalRandom = new Random(seed);
    }

    public static int randint(int bound) {
        return globalRandom.nextInt(bound);
    }

    public static double rand() {
        return globalRandom.nextDouble();
    }

    public static boolean coinFlip(double probability) {
        return globalRandom.nextDouble() < probability;
    }

    public static Random getInstance() {
        return globalRandom;
    }
}
