package org.example.jsga.analysis;

import org.example.jsga.config.Constants;
import org.example.jsga.model.Population;

import static org.example.jsga.util.MeasureUtils.calculateGeneticBias;
import static org.example.jsga.util.MeasureUtils.calculateSigma;

public class ConvergenceChecker {

    private final int geneLength;
    private final int fewThreshold;
    private final boolean enabled;
    private final int minGenerationsBeforeCheck;

    private int lostBits;
    private int convergedBits;
    private double bias;

    private double lastBestFitness = Double.NEGATIVE_INFINITY;
    private int stagnantGenerations = 0;

    public ConvergenceChecker(int geneLength, int fewThreshold, boolean enabled, int minGenerationsBeforeCheck) {
        this.geneLength = geneLength;
        this.fewThreshold = fewThreshold;
        this.enabled = enabled;
        this.minGenerationsBeforeCheck = minGenerationsBeforeCheck;
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
                if ((gene[byteIndex] & (1 << bitIndex)) != 0) ones++;
            }
            if (ones == 0 || ones == popSize) lostBits++;
            if (ones <= fewThreshold || ones >= popSize - fewThreshold) convergedBits++;
            totalBias += Math.max(ones, popSize - ones);
        }

        bias = totalBias / (popSize * geneLength);
    }

    public boolean hasConverged(Population population, int generation) {
        if (!enabled || generation < minGenerationsBeforeCheck) return false;

        analyze(population);
        double bestFitness = population.getBestIndividual().getFitness();

        if (Math.abs(bestFitness - lastBestFitness) < Constants.DEFAULT_CONV_THRESHOLD) {
            stagnantGenerations++;
        } else {
            stagnantGenerations = 0;
            lastBestFitness = bestFitness;
        }

        return stagnantGenerations >= Constants.DEFAULT_MAXCONV;
    }

    public int getLostBits() { return lostBits; }
    public int getConvergedBits() { return convergedBits; }
    public double getBias() { return bias; }

    public boolean isBiasExceeded(Population population) {
        return Constants.BIAS_CHECK_ENABLED && calculateGeneticBias(population) > Constants.DEFAULT_MAXBIAS;
    }

    public boolean isSigmaTooLow(Population population) {
        return Constants.SIGMA_CHECK_ENABLED && calculateSigma(population) < Constants.DEFAULT_SIGMA_FACTOR;
    }
}
