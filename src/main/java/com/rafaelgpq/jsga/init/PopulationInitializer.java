package com.rafaelgpq.jsga.init;

import com.rafaelgpq.jsga.model.Population;

@FunctionalInterface
public interface PopulationInitializer {
    void initialize(Population population);
}
