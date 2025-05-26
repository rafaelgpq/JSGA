package org.example.jsga.model;

import java.util.List;

/**
 * Represents a population of individuals in the genetic algorithm.
 */
public interface Population {
    int size();
    Individual get(int index);
    void markAllForEvaluation();
    void swapWith(Population other);
    List<Individual> getAll();
    void setIndividuals(List<Individual> individuals);
}
