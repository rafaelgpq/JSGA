package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.model.Individual;

@FunctionalInterface
public interface Crossover {
    /**
     * Creates two offspring without modifying either parent.
     */
    Individual[] crossover(Individual parent1, Individual parent2);
}
