package org.example.jsga.recombine.mutation;

import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

public class BitFlipMutation implements Mutation {
    private final double mutationRate;
    private final int geneLength;
    private final boolean adaptive;

    public BitFlipMutation(double mutationRate, int geneLength, boolean adaptive) {
        this.mutationRate = mutationRate;
        this.geneLength = geneLength;
        this.adaptive = adaptive;
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = individual.getGene().clone();
        for (int bit = 0; bit < geneLength; bit++) {
            if (RandomUtils.nextDouble() < mutationRate) {
                int byteIndex = bit / 8;
                int bitIndex = bit % 8;
                gene[byteIndex] ^= (byte) (1 << bitIndex);
            }
        }
        Individual mutated = individual.clone();
        mutated.setGene(gene);
        mutated.setNeedsEvaluation(true);
        return mutated;
    }
}