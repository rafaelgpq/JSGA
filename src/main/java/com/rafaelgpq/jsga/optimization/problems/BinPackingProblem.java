package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One-dimensional bin packing using item-to-bin assignment genes and overflow penalties. */
public final class BinPackingProblem implements OptimizationProblem<List<Integer>> {

    private final int[] weights;
    private final int capacity;
    private final int lowerBound;

    public BinPackingProblem(int[] weights, int capacity) {
        if (weights == null || weights.length == 0 || capacity <= 0) {
            throw new IllegalArgumentException("Bin-packing items and capacity are invalid.");
        }
        this.weights = weights.clone();
        this.capacity = capacity;
        long totalWeight = 0;
        for (int weight : weights) {
            if (weight <= 0 || weight > capacity) {
                throw new IllegalArgumentException("Every item weight must fit a bin and be positive.");
            }
            totalWeight += weight;
        }
        this.lowerBound = (int) ((totalWeight + capacity - 1L) / capacity);
    }

    @Override public String name() { return "Bin Packing"; }

    @Override
    public List<Integer> randomSolution(Random random) {
        List<Integer> bins = new ArrayList<>(weights.length);
        for (int i = 0; i < weights.length; i++) bins.add(random.nextInt(weights.length));
        return List.copyOf(bins);
    }

    @Override
    public double evaluate(List<Integer> assignment) {
        validate(assignment);
        long[] loads = new long[weights.length];
        boolean[] used = new boolean[weights.length];
        int binsUsed = 0;
        long overflow = 0;
        for (int i = 0; i < weights.length; i++) {
            int bin = assignment.get(i);
            if (!used[bin]) {
                used[bin] = true;
                binsUsed++;
            }
            loads[bin] += weights[i];
        }
        for (long load : loads) overflow += Math.max(0L, load - capacity);
        return binsUsed + (weights.length + 1.0) * overflow;
    }

    @Override
    public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validate(first);
        validate(second);
        List<Integer> child = new ArrayList<>(weights.length);
        for (int i = 0; i < weights.length; i++) child.add(random.nextBoolean() ? first.get(i) : second.get(i));
        return List.copyOf(child);
    }

    @Override
    public List<Integer> mutate(List<Integer> assignment, double rate, Random random) {
        validate(assignment);
        List<Integer> child = new ArrayList<>(assignment);
        for (int i = 0; i < child.size(); i++) {
            if (random.nextDouble() < rate) child.set(i, random.nextInt(weights.length));
        }
        return List.copyOf(child);
    }

    @Override public boolean isSolved(double fitness) { return fitness == lowerBound; }

    private void validate(List<Integer> assignment) {
        if (assignment == null || assignment.size() != weights.length
                || assignment.stream().anyMatch(bin -> bin == null || bin < 0 || bin >= weights.length)) {
            throw new IllegalArgumentException("Bin assignment must select a valid bin for every item.");
        }
    }
}
