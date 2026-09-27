package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

/**
 * Flips binary loci when a Gaussian perturbation exceeds the configured threshold.
 */
public class GaussianMutation implements Mutation {
    private static final double NOISE_STANDARD_DEVIATION = 0.1;
    private static final double FLIP_THRESHOLD = 0.05;

    private final double mutationRate;
    private final int geneLength;

    public GaussianMutation(double mutationRate, int geneLength) {
        this.mutationRate = GeneticOperatorSupport.requireProbability("Mutation rate", mutationRate);
        this.geneLength = GeneticOperatorSupport.requireGeneLength(geneLength);
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = GeneticOperatorSupport.copyGene(individual, geneLength);
        if (mutationRate > 0.0) {
            for (int bit = 0; bit < geneLength; bit++) {
                double noise = RandomUtils.nextGaussian() * NOISE_STANDARD_DEVIATION;
                if (Math.abs(noise) > FLIP_THRESHOLD && RandomUtils.nextDouble() < mutationRate) {
                    int byteIndex = bit / 8;
                    int bitIndex = bit % 8;
                    gene[byteIndex] ^= (byte) (1 << bitIndex);
                }
            }
        }
        return GeneticOperatorSupport.offspring(individual, gene);
    }
}
