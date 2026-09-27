package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.model.Individual;

@FunctionalInterface
public interface Mutation {
    /**
     * Creates an offspring without modifying the input individual.
     */
    Individual mutate(Individual individual);
}
