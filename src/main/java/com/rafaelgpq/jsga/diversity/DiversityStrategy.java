package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Population;

@FunctionalInterface
public interface DiversityStrategy {
    void apply(Population nextGen, int generation);
}
