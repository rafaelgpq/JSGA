package com.rafaelgpq.jsga.optimization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class OptimizationUtils {

    private OptimizationUtils() {
    }

    public static List<Boolean> randomBits(int length, Random random) {
        List<Boolean> bits = new ArrayList<>(length);
        for (int i = 0; i < length; i++) bits.add(random.nextBoolean());
        return List.copyOf(bits);
    }

    public static List<Boolean> uniformBits(List<Boolean> first, List<Boolean> second, Random random) {
        requireSameLength(first, second);
        List<Boolean> child = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) child.add(random.nextBoolean() ? first.get(i) : second.get(i));
        return List.copyOf(child);
    }

    public static List<Boolean> mutateBits(List<Boolean> bits, double rate, Random random) {
        List<Boolean> mutant = new ArrayList<>(bits);
        for (int i = 0; i < mutant.size(); i++) {
            if (random.nextDouble() < rate) mutant.set(i, !mutant.get(i));
        }
        return List.copyOf(mutant);
    }

    public static List<Integer> randomPermutation(int length, Random random) {
        List<Integer> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) values.add(i);
        Collections.shuffle(values, random);
        return List.copyOf(values);
    }

    public static List<Integer> orderedCrossover(List<Integer> first, List<Integer> second, Random random) {
        requireSameLength(first, second);
        int length = first.size();
        if (length < 2) return List.copyOf(first);
        int start = random.nextInt(length);
        int end = random.nextInt(length);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        List<Integer> child = new ArrayList<>(Collections.nCopies(length, null));
        Set<Integer> used = new HashSet<>();
        for (int i = start; i <= end; i++) {
            child.set(i, first.get(i));
            used.add(first.get(i));
        }
        int target = (end + 1) % length;
        for (int offset = 0; offset < length; offset++) {
            int value = second.get((end + 1 + offset) % length);
            if (!used.add(value)) continue;
            child.set(target, value);
            target = (target + 1) % length;
        }
        return List.copyOf(child);
    }

    public static List<Integer> swapMutation(List<Integer> values, double rate, Random random) {
        List<Integer> mutant = new ArrayList<>(values);
        for (int i = 0; i < mutant.size(); i++) {
            if (mutant.size() > 1 && random.nextDouble() < rate) {
                int other = random.nextInt(mutant.size());
                Collections.swap(mutant, i, other);
            }
        }
        return List.copyOf(mutant);
    }

    public static List<Double> randomValues(int length, double minimum, double maximum, Random random) {
        List<Double> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(minimum + random.nextDouble() * (maximum - minimum));
        }
        return List.copyOf(values);
    }

    public static List<Double> blendAndMutate(List<Double> first, List<Double> second, double rate,
                                               double minimum, double maximum, Random random) {
        requireSameLength(first, second);
        List<Double> child = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            double alpha = random.nextDouble();
            double value = alpha * first.get(i) + (1.0 - alpha) * second.get(i);
            if (random.nextDouble() < rate) value += random.nextGaussian() * (maximum - minimum) * 0.1;
            child.add(Math.max(minimum, Math.min(maximum, value)));
        }
        return List.copyOf(child);
    }

    private static void requireSameLength(List<?> first, List<?> second) {
        if (first == null || second == null || first.size() != second.size()) {
            throw new IllegalArgumentException("Parent solutions must be non-null and have equal lengths.");
        }
    }
}
