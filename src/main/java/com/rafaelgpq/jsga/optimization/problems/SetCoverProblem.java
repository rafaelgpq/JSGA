package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Weighted Set Cover with an explicit penalty for each uncovered universe element. */
public final class SetCoverProblem implements OptimizationProblem<List<Boolean>> {

    private final int universeSize;
    private final List<List<Integer>> sets;
    private final double[] costs;
    private final double uncoveredPenalty;

    public SetCoverProblem(int universeSize, List<List<Integer>> sets, double[] costs) {
        if (universeSize <= 0 || sets == null || sets.isEmpty() || costs == null
                || costs.length != sets.size()) {
            throw new IllegalArgumentException("Set-cover universe, subsets, and costs are invalid.");
        }
        this.universeSize = universeSize;
        List<List<Integer>> copiedSets = new ArrayList<>(sets.size());
        for (List<Integer> subset : sets) {
            if (subset == null) throw new IllegalArgumentException("Cover subsets must not be null.");
            copiedSets.add(new ArrayList<>(subset));
        }
        this.sets = List.copyOf(copiedSets);
        this.costs = costs.clone();
        double totalCost = 0.0;
        for (int i = 0; i < this.sets.size(); i++) {
            if (this.sets.get(i).isEmpty() || this.sets.get(i).stream()
                    .anyMatch(element -> element == null || element < 0 || element >= universeSize)) {
                throw new IllegalArgumentException("Each subset must contain valid universe elements.");
            }
            if (!Double.isFinite(this.costs[i]) || this.costs[i] <= 0.0) {
                throw new IllegalArgumentException("Set costs must be positive and finite.");
            }
            totalCost += this.costs[i];
        }
        if (!Double.isFinite(totalCost)) {
            throw new IllegalArgumentException("Total set-cover cost must be finite.");
        }
        this.uncoveredPenalty = totalCost + 1.0;
    }

    @Override public String name() { return "Set Cover"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(sets.size(), random);
    }

    @Override
    public double evaluate(List<Boolean> selected) {
        OneMaxProblem.requireLength(selected, sets.size());
        boolean[] covered = new boolean[universeSize];
        double cost = 0.0;
        for (int i = 0; i < selected.size(); i++) {
            if (selected.get(i)) {
                cost += costs[i];
                for (int element : sets.get(i)) covered[element] = true;
            }
        }
        for (boolean present : covered) if (!present) cost += uncoveredPenalty;
        return cost;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        OneMaxProblem.requireLength(first, sets.size());
        OneMaxProblem.requireLength(second, sets.size());
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
        OneMaxProblem.requireLength(solution, sets.size());
        return OptimizationUtils.mutateBits(solution, rate, random);
    }
}
