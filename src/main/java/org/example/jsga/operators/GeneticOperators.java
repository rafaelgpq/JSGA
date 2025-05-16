package org.example.jsga.operators;

import org.example.jsga.model.Population;

/**
 * Defines the key operations in a Genetic Algorithm.
 */
public interface GeneticOperators {

    void initialize(Population population);

    void select(Population from, Population to);

    void mutate(Population population);

    void crossover(Population population);

    void elitist(Population oldPop, Population newPop);

    void evaluate(Population population);

    void restart();

    void performDpe(Population population);

    void printBest(Population population);

    boolean isElitismEnabled();

    boolean isEvaluateAll();

    boolean shouldSaveBest();

    int getDpeFrequency();
}

