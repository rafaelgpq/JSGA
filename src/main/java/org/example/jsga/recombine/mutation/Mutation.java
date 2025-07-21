package org.example.jsga.recombine.mutation;

import org.example.jsga.model.Individual;

@FunctionalInterface
public interface Mutation {
    Individual mutate(Individual individual);
}
