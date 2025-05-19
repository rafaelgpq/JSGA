package org.example.jsga.init;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.Random;

/**
 * Initializes a population with random genes.
 */
public class Initializer {

    private final Random random;

    public Initializer(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Randomizes each individual's gene in the population.
     */
    public void initialize(Population population) {
        for (int i = 0; i < population.size(); i++) {
            Individual ind = population.get(i);
            byte[] gene = new byte[ind.getGeneLength() / 8 + ((ind.getGeneLength() % 8 == 0) ? 0 : 1)];
            random.nextBytes(gene);
            ind.setGene(gene);
            ind.setNeedsEvaluation(true);
        }
    }

    public Random getRandom() {
        return random;
    }
}
