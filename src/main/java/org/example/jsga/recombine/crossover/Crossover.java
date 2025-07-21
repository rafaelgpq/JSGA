package org.example.jsga.recombine.crossover;

import org.example.jsga.model.Individual;

@FunctionalInterface
public interface Crossover {
    Individual[] crossover(Individual parent1, Individual parent2);
}
