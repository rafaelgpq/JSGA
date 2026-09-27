package com.rafaelgpq.jsga.core;

import com.rafaelgpq.jsga.model.Population;

/**
 * Defines the key operations in a Genetic Algorithm.
 */
public interface GeneticOperators {

    void initialize(Population population);

    void select(Population from, Population to);

    void mutate(Population population, int generation, int maxGenerations);

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

