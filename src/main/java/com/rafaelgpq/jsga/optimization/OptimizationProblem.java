package com.rafaelgpq.jsga.optimization;

import java.util.Random;

/**
 * Defines an immutable solution representation and its problem-specific GA operators.
 * Returned solutions must be immutable because the runner retains elites by reference.
 */
public interface OptimizationProblem<S> {

    String name();

    S randomSolution(Random random);

    double evaluate(S solution);

    S crossover(S first, S second, Random random);

    S mutate(S solution, double mutationRate, Random random);

    default boolean isSolved(double fitness) {
        return false;
    }
}
