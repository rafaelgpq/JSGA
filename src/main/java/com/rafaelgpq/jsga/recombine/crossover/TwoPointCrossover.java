package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

public class TwoPointCrossover implements Crossover {

    private final int chromosomeLength;

    public TwoPointCrossover(int chromosomeLength) {
        this.chromosomeLength = GeneticOperatorSupport.requireGeneLength(chromosomeLength);
    }

    @Override
    public Individual[] crossover(Individual p1, Individual p2) {
        byte[] gene1 = GeneticOperatorSupport.copyGene(p1, chromosomeLength);
        byte[] gene2 = GeneticOperatorSupport.copyGene(p2, chromosomeLength);

        int x1 = RandomUtils.nextInt(chromosomeLength + 1);
        int x2 = RandomUtils.nextInt(chromosomeLength);
        if (x2 >= x1) {
            x2++;
        }
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);

        for (int i = start; i < end; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            boolean a = ((gene1[byteIndex] >> bitIndex) & 1) != 0;
            boolean b = ((gene2[byteIndex] >> bitIndex) & 1) != 0;
            if (a != b) {
                gene1[byteIndex] ^= (byte) (1 << bitIndex);
                gene2[byteIndex] ^= (byte) (1 << bitIndex);
            }
        }

        return new Individual[]{
                GeneticOperatorSupport.offspring(p1, gene1),
                GeneticOperatorSupport.offspring(p2, gene2)
        };
    }
}
