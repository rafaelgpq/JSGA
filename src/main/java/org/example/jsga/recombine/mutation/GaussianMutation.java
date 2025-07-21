package org.example.jsga.recombine.mutation;

import org.example.jsga.model.Individual;
import org.example.jsga.recombine.mutation.Mutation;
import org.example.jsga.util.RandomUtils;

public class GaussianMutation implements Mutation {
    private final double mutationRate;
    private final int geneLength;

    public GaussianMutation(double mutationRate, int geneLength) {
        this.mutationRate = mutationRate;
        this.geneLength = geneLength;
    }

    @Override
    public Individual mutate(Individual individual) {
        byte[] gene = individual.getGene().clone();
        double stddev = 0.1;
        for (int bit = 0; bit < geneLength; bit++) {
            double noise = RandomUtils.nextGaussian() * stddev;
            if (Math.abs(noise) > 0.05 && RandomUtils.nextDouble() < mutationRate) {
                int byteIndex = bit / 8;
                int bitIndex = bit % 8;
                gene[byteIndex] ^= (1 << bitIndex);
            }
        }
        Individual mutated = individual.clone();
        mutated.setGene(gene);
        mutated.setNeedsEvaluation(true);
        return mutated;
    }
}
