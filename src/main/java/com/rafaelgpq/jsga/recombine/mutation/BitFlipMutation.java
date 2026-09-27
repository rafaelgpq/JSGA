package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class BitFlipMutation implements Mutation {
    private final double mutationRate;
    private final int geneLength;

    public BitFlipMutation(double mutationRate, int geneLength) {
        this.mutationRate = GeneticOperatorSupport.requireProbability("Mutation rate", mutationRate);
        this.geneLength = GeneticOperatorSupport.requireGeneLength(geneLength);
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = GeneticOperatorSupport.copyGene(individual, geneLength);
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