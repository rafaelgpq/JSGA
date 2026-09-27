package com.rafaelgpq.jsga.util;

import java.util.Random;

/**
 * Provides utility methods for reproducible and centralized randomness.
 */
public class RandomUtils {

    private static StatefulRandom globalRandom = new StatefulRandom(System.nanoTime());

    public static void initialize(long seed) {
        globalRandom = new StatefulRandom(seed);
    }

    public static int nextInt(int bound) {
        return globalRandom.nextInt(bound);
    }

    public static double nextDouble() {
        return globalRandom.nextDouble();
    }

    public static boolean coinFlip(double probability) {
        return globalRandom.nextDouble() < probability;
    }

    public static double nextGaussian() {
        return globalRandom.nextGaussian();
    }

    public static Random getInstance() {
        return globalRandom;
    }

    public static long[] snapshot() {
        return globalRandom.snapshot();
    }

    public static void restore(long[] state) {
        globalRandom = StatefulRandom.restore(state);
    }

    private static final class StatefulRandom extends Random {
        private static final long serialVersionUID = 1L;
        private static final long INCREMENT = 0x9E3779B97F4A7C15L;

        private long state;
        private long draws;
        private boolean hasGaussian;
        private double gaussian;

        private StatefulRandom(long seed) {
            super(0L);
            setSeed(seed);
        }

        @Override
        public synchronized void setSeed(long seed) {
            state = seed;
            draws = 0L;
            hasGaussian = false;
            gaussian = 0.0;
        }

        @Override
        protected synchronized int next(int bits) {
            state += INCREMENT;
            draws++;
            long value = state;
            value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
            value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
            value ^= value >>> 31;
            if (bits == 0) return 0;
            return (int) (value >>> (Long.SIZE - bits));
        }

        @Override
        public synchronized double nextGaussian() {
            if (hasGaussian) {
                hasGaussian = false;
                return gaussian;
            }
            double first;
            double second;
            double radius;
            do {
                first = 2.0 * nextDouble() - 1.0;
                second = 2.0 * nextDouble() - 1.0;
                radius = first * first + second * second;
            } while (radius >= 1.0 || radius == 0.0);
            double multiplier = Math.sqrt(-2.0 * Math.log(radius) / radius);
            gaussian = second * multiplier;
            hasGaussian = true;
            return first * multiplier;
        }

        private synchronized long[] snapshot() {
            return new long[]{state, draws, hasGaussian ? 1L : 0L, Double.doubleToLongBits(gaussian)};
        }

        private static StatefulRandom restore(long[] state) {
            if (state == null || state.length != 4 || state[1] < 0
                    || (state[2] != 0L && state[2] != 1L)
                    || !Double.isFinite(Double.longBitsToDouble(state[3]))) {
                throw new IllegalArgumentException("Random generator state is invalid.");
            }
            StatefulRandom random = new StatefulRandom(0L);
            synchronized (random) {
                random.state = state[0];
                random.draws = state[1];
                random.hasGaussian = state[2] == 1L;
                random.gaussian = Double.longBitsToDouble(state[3]);
            }
            return random;
        }
    }
}
