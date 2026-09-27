package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Binary OneMax benchmark, represented as an immutable bit vector. */
public final class OneMaxProblem implements OptimizationProblem<List<Boolean>> {

    private final int length;

    public OneMaxProblem(int length) {
        if (length <= 0) throw new IllegalArgumentException("OneMax length must be positive.");
        this.length = length;
    }

    @Override public String name() { return "OneMax"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(length, random);
    }

    @Override
    public double evaluate(List<Boolean> bits) {
        requireLength(bits, length);
        int ones = 0;
        for (boolean bit : bits) if (bit) ones++;
        return -ones;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        requireLength(first, length);
        requireLength(second, length);
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> bits, double rate, Random random) {
        requireLength(bits, length);
        return OptimizationUtils.mutateBits(bits, rate, random);
    }

    @Override public boolean isSolved(double fitness) { return fitness == -length; }

    static void requireLength(List<?> solution, int expected) {
        if (solution == null || solution.size() != expected || solution.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Solution length must be " + expected + ".");
        }
    }
}
