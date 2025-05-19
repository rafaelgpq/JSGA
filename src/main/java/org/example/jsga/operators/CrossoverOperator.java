package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

/**
 * Performs two-point crossover between pairs of individuals.
 */
public class CrossoverOperator {

    private final double crossoverRate;
    private final int populationSize;
    private final int chromosomeLength;

    public CrossoverOperator(double crossoverRate, int populationSize, int chromosomeLength) {
        this.crossoverRate = crossoverRate;
        this.populationSize = populationSize;
        this.chromosomeLength = chromosomeLength;
    }

    public void performCrossover(Population population) {
        int numberOfCrosses = (int) (crossoverRate * populationSize);

        for (int count = 0; count + 1 < numberOfCrosses; count += 2) {
            Individual mom = population.get(count % populationSize);
            Individual dad = population.get((count + 1) % populationSize);

            byte[] gene1 = mom.getGene();
            byte[] gene2 = dad.getGene();

            int x1 = RandomUtils.randint(chromosomeLength);
            int x2 = RandomUtils.randint(chromosomeLength);
            if (x1 == x2) x2 = (x2 + 1) % chromosomeLength;
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }

            swapBitsBetween(gene1, gene2, x1, x2);

            mom.setGene(gene1);
            dad.setGene(gene2);

            mom.setNeedsEvaluation(true);
            dad.setNeedsEvaluation(true);
        }
    }

    /**
     * Swaps bits between two gene byte arrays from bit start to end (inclusive).
     */
    private void swapBitsBetween(byte[] a, byte[] b, int startBit, int endBit) {
        for (int bit = startBit; bit <= endBit; bit++) {
            int byteIndex = bit / 8;
            int bitIndex = bit % 8;

            boolean abit = ((a[byteIndex] >> bitIndex) & 1) != 0;
            boolean bbit = ((b[byteIndex] >> bitIndex) & 1) != 0;

            if (abit != bbit) {
                a[byteIndex] ^= (1 << bitIndex);
                b[byteIndex] ^= (1 << bitIndex);
            }
        }
    }
}
