package org.example.jsga.recombine.crossover;

import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

public class TwoPointCrossover implements Crossover {

    private final int chromosomeLength;

    public TwoPointCrossover(int chromosomeLength) {
        this.chromosomeLength = chromosomeLength;
    }

    @Override
    public Individual[] crossover(Individual p1, Individual p2) {
        byte[] gene1 = p1.getGene().clone();
        byte[] gene2 = p2.getGene().clone();

        int x1 = RandomUtils.nextInt(chromosomeLength);
        int x2 = RandomUtils.nextInt(chromosomeLength);
        if (x1 > x2) { int tmp = x1; x1 = x2; x2 = tmp; }

        for (int i = x1; i <= x2; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            boolean a = ((gene1[byteIndex] >> bitIndex) & 1) != 0;
            boolean b = ((gene2[byteIndex] >> bitIndex) & 1) != 0;
            if (a != b) {
                gene1[byteIndex] ^= (byte) (1 << bitIndex);
                gene2[byteIndex] ^= (byte) (1 << bitIndex);
            }
        }

        Individual c1 = p1.clone(); c1.setGene(gene1);
        Individual c2 = p2.clone(); c2.setGene(gene2);
        return new Individual[]{c1, c2};
    }
}
