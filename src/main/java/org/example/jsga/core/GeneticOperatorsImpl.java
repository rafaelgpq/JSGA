package org.example.jsga.core;

import org.example.jsga.model.Population;
import org.example.jsga.operators.CrossoverOperator;
import org.example.jsga.operators.MutationOperator;

/**
 * Coordinates only mutation and crossover operations in a GA run.
 * Excludes selection, evaluation, and elitism responsibilities.
 */
public class GeneticOperatorsImpl implements GeneticOperators {

    private final MutationOperator mutation;
    private final CrossoverOperator crossover;

    public GeneticOperatorsImpl(
            MutationOperator mutation,
            CrossoverOperator crossover
    ) {
        this.mutation = mutation;
        this.crossover = crossover;
    }

    @Override
    public void initialize(Population population) {
        // Initialization handled externally
    }

    @Override
    public void select(Population from, Population to) {
        // Selection handled externally
    }

    // In GeneticOperatorsImpl.java
    @Override
    public void mutate(Population population, int generation, int maxGenerations) {
        mutation.mutate(population, generation, maxGenerations);
    }

    @Override
    public void crossover(Population population) {
        crossover.performCrossover(population);
    }

    @Override
    public void elitist(Population oldPop, Population newPop) {
        // Elitism handled externally
    }

    @Override
    public void evaluate(Population population) {
        // Evaluation handled externally
    }

    @Override
    public void restart() {
        // Optional restart hook
    }

    @Override
    public void performDpe(Population population) {
        // Optional: dynamic parameter encoding hook
    }

    @Override
    public void printBest(Population population) {
        // Optional logging
    }

    @Override
    public boolean isElitismEnabled() {
        return false;
    }

    @Override
    public boolean isEvaluateAll() {
        return false;
    }

    @Override
    public boolean shouldSaveBest() {
        return false;
    }

    @Override
    public int getDpeFrequency() {
        return 0;
    }
}
