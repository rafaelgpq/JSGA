package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class AdaptiveBitFlipMutation implements Mutation {
    private final double baseMutationRate;
    private final int geneLength;
    private final boolean adaptiveEnabled;
    private static final double ADAPTIVE_FACTOR = 1.5;

    public AdaptiveBitFlipMutation(double baseMutationRate, int geneLength, boolean adaptiveEnabled) {
        this.baseMutationRate = GeneticOperatorSupport.requireProbability("Mutation rate", baseMutationRate);
        this.geneLength = GeneticOperatorSupport.requireGeneLength(geneLength);
        this.adaptiveEnabled = adaptiveEnabled;
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = GeneticOperatorSupport.copyGene(individual, geneLength);
        double mutationRate = adaptiveEnabled && baseMutationRate > 0.0
                ? Math.min(1.0, baseMutationRate * (1.0 + ADAPTIVE_FACTOR * RandomUtils.nextDouble()))
                : baseMutationRate;
        if (mutationRate > 0.0) {
            for (int bit = 0; bit < geneLength; bit++) {
                if (RandomUtils.nextDouble() < mutationRate) {
                    int byteIndex = bit / 8;
                    int bitIndex = bit % 8;
                    gene[byteIndex] ^= (byte) (1 << bitIndex);
                }
            }
        }
        return GeneticOperatorSupport.offspring(individual, gene);
    }
}
