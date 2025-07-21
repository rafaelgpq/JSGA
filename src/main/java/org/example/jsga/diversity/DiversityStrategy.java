package org.example.jsga.diversity;

import org.example.jsga.model.Population;

@FunctionalInterface
public interface DiversityStrategy {
    void apply(Population nextGen, int generation);
}
