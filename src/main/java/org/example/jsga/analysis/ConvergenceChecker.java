package org.example.jsga.analysis;

import org.example.jsga.model.Population;

/**
 * Measures system convergence across all gene bit positions.
 */
public class ConvergenceChecker {

    private final int geneLength;
    private final int fewThreshold;
    private final boolean enabled;

    private int lostBits;
    private int convergedBits;
    private double bias;

    public ConvergenceChecker(int geneLength, int fewThreshold, boolean enabled) {
        this.geneLength = geneLength;
        this.fewThreshold = fewThreshold;
        this.enabled = enabled;
    }

    public void analyze(Population population) {
        if (!enabled) return;

        int popSize = population.size();
        lostBits = 0;
        convergedBits = 0;
        double totalBias = 0;

        for (int j = 0; j < geneLength; j++) {
            int byteIndex = j / 8;
            int bitIndex = j % 8;
            int ones = 0;

            for (int i = 0; i < popSize; i++) {
                byte[] gene = population.get(i).getGene();
                if ((gene[byteIndex] & (1 << bitIndex)) != 0) {
                    ones++;
                }
            }

            if (ones == 0 || ones == popSize) lostBits++;
            if (ones <= fewThreshold || ones >= popSize - fewThreshold) convergedBits++;
            totalBias += Math.max(ones, popSize - ones);
        }

        bias = totalBias / (popSize * geneLength);
    }

    public int getLostBits() {
        return lostBits;
    }

    public int getConvergedBits() {
        return convergedBits;
    }

    public double getBias() {
        return bias;
    }
}
