package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** 0/1 Knapsack with a penalty that makes every feasible solution preferable to an infeasible one. */
public final class KnapsackProblem implements OptimizationProblem<List<Boolean>> {

    private final int[] weights;
    private final double[] values;
    private final int capacity;
    private final double infeasibilityPenalty;

    public KnapsackProblem(int[] weights, double[] values, int capacity) {
        if (weights == null || values == null || weights.length == 0 || weights.length != values.length
                || capacity <= 0) {
            throw new IllegalArgumentException("Knapsack weights, values, and capacity are invalid.");
        }
        this.weights = weights.clone();
        this.values = values.clone();
        this.capacity = capacity;
        double totalValue = 0.0;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] <= 0 || !Double.isFinite(values[i]) || values[i] <= 0.0) {
                throw new IllegalArgumentException("Item weights and values must be positive and finite.");
            }
            totalValue += values[i];
        }
        if (!Double.isFinite(totalValue)) {
            throw new IllegalArgumentException("Total knapsack value must be finite.");
        }
        this.infeasibilityPenalty = totalValue + 1.0;
    }

    @Override public String name() { return "0/1 Knapsack"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(weights.length, random);
    }

    @Override
    public double evaluate(List<Boolean> selected) {
        OneMaxProblem.requireLength(selected, weights.length);
        long totalWeight = 0L;
        double totalValue = 0.0;
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i)) {
                totalWeight += weights[i];
                totalValue += values[i];
            }
        }
        return totalWeight <= capacity
                ? -totalValue
                : infeasibilityPenalty * (totalWeight - capacity) - totalValue;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        OneMaxProblem.requireLength(first, weights.length);
        OneMaxProblem.requireLength(second, weights.length);
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
        OneMaxProblem.requireLength(solution, weights.length);
        return OptimizationUtils.mutateBits(solution, rate, random);
    }
}
