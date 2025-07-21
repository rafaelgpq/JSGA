package org.example.jsga.recombine.crossover;

import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

public class UniformCrossover implements Crossover {

    private final int chromosomeLength;

    public UniformCrossover(int chromosomeLength) {
        this.chromosomeLength = chromosomeLength;
    }

    @Override
    public Individual[] crossover(Individual p1, Individual p2) {
        byte[] gene1 = p1.getGene().clone();
        byte[] gene2 = p2.getGene().clone();

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

        Individual c1 = p1.clone(); c1.setGene(gene1);
        Individual c2 = p2.clone(); c2.setGene(gene2);
        return new Individual[]{c1, c2};
    }
}
