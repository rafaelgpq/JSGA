package org.example.jsga.recombine.mutation;

import org.example.jsga.model.Individual;
import org.example.jsga.recombine.mutation.Mutation;
import org.example.jsga.util.RandomUtils;

public class SwapMutation implements Mutation {
    private final double mutationRate;
    private final int geneLength;

    public SwapMutation(double mutationRate, int geneLength) {
        this.mutationRate = mutationRate;
        this.geneLength = geneLength;
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = individual.getGene().clone();
        if (RandomUtils.nextDouble() < mutationRate) {
            int pos1 = RandomUtils.nextInt(geneLength);
            int pos2 = RandomUtils.nextInt(geneLength);
            int byte1 = pos1 / 8, bit1 = pos1 % 8;
            int byte2 = pos2 / 8, bit2 = pos2 % 8;
            boolean bit1Val = ((gene[byte1] >> bit1) & 1) != 0;
            boolean bit2Val = ((gene[byte2] >> bit2) & 1) != 0;
            if (bit1Val != bit2Val) {
                gene[byte1] ^= (byte) (1 << bit1);
                gene[byte2] ^= (byte) (1 << bit2);
            }
        }
        Individual mutated = individual.clone();
        mutated.setGene(gene);
        mutated.setNeedsEvaluation(true);
        return mutated;
    }
}
