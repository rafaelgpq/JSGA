package com.rafaelgpq.jsga.analysis;

import com.rafaelgpq.jsga.config.Constants;
import com.rafaelgpq.jsga.model.Population;

import static com.rafaelgpq.jsga.util.MeasureUtils.calculateGeneticBias;
import static com.rafaelgpq.jsga.util.MeasureUtils.calculateSigma;

public class ConvergenceChecker {

    private final int geneLength;
    private final int fewThreshold;
    private final boolean enabled;
    private final int minGenerationsBeforeCheck;
    private final double convergenceThreshold;
    private final int maxConsecutiveStagnantGenerations;

    private int lostBits;
    private int convergedBits;
    private double bias;

    private double lastBestFitness = Double.NEGATIVE_INFINITY;
    private int stagnantGenerations = 0;

    public ConvergenceChecker(int geneLength, int fewThreshold, boolean enabled, int minGenerationsBeforeCheck) {
        this(geneLength, fewThreshold, enabled, minGenerationsBeforeCheck,
                Constants.DEFAULT_CONV_THRESHOLD, Constants.DEFAULT_MAXCONV);
    }

    public ConvergenceChecker(int geneLength, int fewThreshold, boolean enabled, int minGenerationsBeforeCheck,
                              double convergenceThreshold, int maxConsecutiveStagnantGenerations) {
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        if (fewThreshold < 0) {
            throw new IllegalArgumentException("Few threshold must not be negative.");
        }
        if (minGenerationsBeforeCheck < 0) {
            throw new IllegalArgumentException("Minimum generations before checking must not be negative.");
        }
        if (!Double.isFinite(convergenceThreshold) || convergenceThreshold < 0.0) {
            throw new IllegalArgumentException("Convergence threshold must be finite and not negative.");
        }
        if (maxConsecutiveStagnantGenerations < 0) {
            throw new IllegalArgumentException("Maximum stagnant generations must not be negative.");
        }
        this.geneLength = geneLength;
        this.fewThreshold = fewThreshold;
        this.enabled = enabled;
        this.minGenerationsBeforeCheck = minGenerationsBeforeCheck;
        this.convergenceThreshold = convergenceThreshold;
        this.maxConsecutiveStagnantGenerations = maxConsecutiveStagnantGenerations;
    }

    public void analyze(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        lostBits = 0;
        convergedBits = 0;
        bias = 0.0;
        if (!enabled || population.size() == 0) return;

        int popSize = population.size();
        double totalBias = 0;

        for (int j = 0; j < geneLength; j++) {
            int byteIndex = j / 8;
            int bitIndex = j % 8;
            int ones = 0;
            for (int i = 0; i < popSize; i++) {
                if (population.get(i) == null || population.get(i).getGene() == null
                        || population.get(i).getGene().length != (geneLength + Byte.SIZE - 1) / Byte.SIZE
                        || population.get(i).getGeneLength() != geneLength) {
                    throw new IllegalArgumentException("Individual at index " + i
                            + " has an invalid genome shape for configured gene length " + geneLength + ".");
                }
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
        if (generation < 0) {
            throw new IllegalArgumentException("Generation must not be negative.");
        }
        if (!enabled || maxConsecutiveStagnantGenerations == 0
                || generation < minGenerationsBeforeCheck) return false;
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        if (population.size() == 0) {
            return false;
        }

        analyze(population);
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i) == null || !Double.isFinite(population.get(i).getFitness())) {
                throw new IllegalArgumentException("Population fitness values must be finite.");
            }
        }
        double bestFitness = population.getBestIndividual().getFitness();
        if (!Double.isFinite(bestFitness)) {
            throw new IllegalArgumentException("Best fitness must be finite.");
        }

        if (Math.abs(bestFitness - lastBestFitness) <= convergenceThreshold) {
            stagnantGenerations++;
        } else {
            stagnantGenerations = 0;
            lastBestFitness = bestFitness;
        }

        return stagnantGenerations >= maxConsecutiveStagnantGenerations;
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
