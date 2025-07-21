package org.example.jsga.recombine.mutation;

import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

public class AdaptiveBitFlipMutation implements Mutation {
    private final double baseMutationRate;
    private final int geneLength;
    private final boolean adaptiveEnabled;
    private final double adaptiveFactor = 1.5;

    public AdaptiveBitFlipMutation(double baseMutationRate, int geneLength, boolean adaptiveEnabled) {
        this.baseMutationRate = baseMutationRate;
        this.geneLength = geneLength;
        this.adaptiveEnabled = adaptiveEnabled;
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = individual.getGene().clone();
        double mutationRate = baseMutationRate;
        if (adaptiveEnabled) mutationRate *= 1 + adaptiveFactor * RandomUtils.nextDouble();
        for (int bit = 0; bit < geneLength; bit++) {
            if (RandomUtils.nextDouble() < mutationRate) {
                int byteIndex = bit / 8, bitIndex = bit % 8;
                gene[byteIndex] ^= (1 << bitIndex);
            }
        }
        Individual mutated = individual.clone(); mutated.setGene(gene); mutated.setNeedsEvaluation(true); return mutated;
    }
}
