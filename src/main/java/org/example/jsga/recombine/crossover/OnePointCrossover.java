package org.example.jsga.recombine.crossover;

import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

public class OnePointCrossover implements Crossover {

    private final int chromosomeLength;

    public OnePointCrossover(int chromosomeLength) {
        this.chromosomeLength = chromosomeLength;
    }

    @Override
    public Individual[] crossover(Individual parent1, Individual parent2) {
        byte[] gene1 = parent1.getGene().clone();
        byte[] gene2 = parent2.getGene().clone();

        // Perform one-point crossover
        int crossoverPoint = RandomUtils.nextInt(chromosomeLength);

        for (int i = crossoverPoint; i < chromosomeLength; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;

            boolean abit = ((gene1[byteIndex] >> bitIndex) & 1) != 0;
            boolean bbit = ((gene2[byteIndex] >> bitIndex) & 1) != 0;

            if (abit != bbit) {
                gene1[byteIndex] ^= (byte) (1 << bitIndex);
                gene2[byteIndex] ^= (byte) (1 << bitIndex);
            }
        }

        Individual child1 = parent1.clone();
        child1.setGene(gene1);
        Individual child2 = parent2.clone();
        child2.setGene(gene2);

        return new Individual[] { child1, child2 };
    }
}
