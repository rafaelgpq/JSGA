package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** N-Queens with one queen per column and permutation-preserving operators. */
public final class NQueensProblem implements OptimizationProblem<List<Integer>> {

    private final int size;

    public NQueensProblem(int size) {
        if (size < 4) throw new IllegalArgumentException("N-Queens size must be at least four.");
        this.size = size;
    }

    @Override public String name() { return size + "-Queens"; }
    @Override public List<Integer> randomSolution(Random random) {
        return OptimizationUtils.randomPermutation(size, random);
    }

    @Override
    public double evaluate(List<Integer> rows) {
        validate(rows);
        int conflicts = 0;
        for (int first = 0; first < size; first++) {
            for (int second = first + 1; second < size; second++) {
                if (Math.abs(rows.get(first) - rows.get(second)) == second - first) conflicts++;
            }
        }
        return conflicts;
    }

    @Override public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validate(first);
        validate(second);
        return OptimizationUtils.orderedCrossover(first, second, random);
    }

    @Override public List<Integer> mutate(List<Integer> rows, double rate, Random random) {
        validate(rows);
        return OptimizationUtils.swapMutation(rows, rate, random);
    }

    @Override public boolean isSolved(double fitness) { return fitness == 0.0; }

    private void validate(List<Integer> rows) {
        if (rows == null || rows.size() != size
                || rows.stream().anyMatch(row -> row == null || row < 0 || row >= size)
                || rows.stream().distinct().count() != size) {
            throw new IllegalArgumentException("N-Queens solution must be a permutation of board rows.");
        }
    }
}
