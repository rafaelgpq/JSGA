package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class SwapMutation implements Mutation {
    private final double mutationRate;
    private final int geneLength;

    public SwapMutation(double mutationRate, int geneLength) {
        this.mutationRate = GeneticOperatorSupport.requireProbability("Mutation rate", mutationRate);
        this.geneLength = GeneticOperatorSupport.requireGeneLength(geneLength);
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = GeneticOperatorSupport.copyGene(individual, geneLength);
        if (geneLength > 1 && mutationRate > 0.0 && RandomUtils.nextDouble() < mutationRate) {
            int pos1 = RandomUtils.nextInt(geneLength);
            int pos2 = RandomUtils.nextInt(geneLength - 1);
            if (pos2 >= pos1) {
                pos2++;
            }
            int byte1 = pos1 / 8, bit1 = pos1 % 8;
            int byte2 = pos2 / 8, bit2 = pos2 % 8;
            boolean bit1Val = ((gene[byte1] >> bit1) & 1) != 0;
            boolean bit2Val = ((gene[byte2] >> bit2) & 1) != 0;
            if (bit1Val != bit2Val) {
                gene[byte1] ^= (byte) (1 << bit1);
                gene[byte2] ^= (byte) (1 << bit2);
            }
        }
        return GeneticOperatorSupport.offspring(individual, gene);
    }
}
