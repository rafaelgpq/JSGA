package com.rafaelgpq.jsga.init;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.util.Random;

/**
 * Initializes a population with random genes.
 */
public class RandomInitializer implements PopulationInitializer {

    private final Random random;

    public RandomInitializer(long seed) {
        this(new Random(seed));
    }

    public RandomInitializer(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random generator must not be null.");
        }
        this.random = random;
    }

    /**
     * Randomizes each individual's gene in the population.
     */
    public void initialize(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        for (int i = 0; i < population.size(); i++) {
            Individual ind = population.get(i);
            if (ind.getGeneLength() <= 0) {
                throw new IllegalArgumentException("Gene length must be greater than zero.");
            }
            byte[] gene = new byte[ind.getGeneLength() / 8 + ((ind.getGeneLength() % 8 == 0) ? 0 : 1)];
            random.nextBytes(gene);
            int usedBitsInLastByte = ind.getGeneLength() % 8;
            if (usedBitsInLastByte != 0) {
                gene[gene.length - 1] &= (byte) ((1 << usedBitsInLastByte) - 1);
            }
            ind.setGene(gene);
            ind.setNeedsEvaluation(true);
        }
    }

    public Random getRandom() {
        return random;
    }
}
