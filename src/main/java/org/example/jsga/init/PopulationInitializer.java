package org.example.jsga.init;

import org.example.jsga.model.Population;

@FunctionalInterface
public interface PopulationInitializer {
    void initialize(Population population);
}
