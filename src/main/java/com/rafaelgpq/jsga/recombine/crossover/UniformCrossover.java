package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class UniformCrossover implements Crossover {

    private final int chromosomeLength;

    public UniformCrossover(int chromosomeLength) {
        this.chromosomeLength = GeneticOperatorSupport.requireGeneLength(chromosomeLength);
    }

    @Override
    public Individual[] crossover(Individual p1, Individual p2) {
        byte[] gene1 = GeneticOperatorSupport.copyGene(p1, chromosomeLength);
        byte[] gene2 = GeneticOperatorSupport.copyGene(p2, chromosomeLength);

        for (int i = 0; i < chromosomeLength; i++) {
            if (RandomUtils.nextDouble() < 0.5) {
                int byteIndex = i / 8;
                int bitIndex = i % 8;
                boolean a = ((gene1[byteIndex] >> bitIndex) & 1) != 0;
                boolean b = ((gene2[byteIndex] >> bitIndex) & 1) != 0;
                if (a != b) {
                    gene1[byteIndex] ^= (byte) (1 << bitIndex);
                    gene2[byteIndex] ^= (byte) (1 << bitIndex);
                }
            }
        }

        return new Individual[]{
                GeneticOperatorSupport.offspring(p1, gene1),
                GeneticOperatorSupport.offspring(p2, gene2)
        };
    }
}
