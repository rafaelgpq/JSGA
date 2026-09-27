package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class OnePointCrossover implements Crossover {

    private final int chromosomeLength;

    public OnePointCrossover(int chromosomeLength) {
        this.chromosomeLength = GeneticOperatorSupport.requireGeneLength(chromosomeLength);
    }

    @Override
    public Individual[] crossover(Individual parent1, Individual parent2) {
        byte[] gene1 = GeneticOperatorSupport.copyGene(parent1, chromosomeLength);
        byte[] gene2 = GeneticOperatorSupport.copyGene(parent2, chromosomeLength);
        int crossoverPoint = chromosomeLength == 1
                ? 1 : 1 + RandomUtils.nextInt(chromosomeLength - 1);

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

        return new Individual[]{
                GeneticOperatorSupport.offspring(parent1, gene1),
                GeneticOperatorSupport.offspring(parent2, gene2)
        };
    }
}
