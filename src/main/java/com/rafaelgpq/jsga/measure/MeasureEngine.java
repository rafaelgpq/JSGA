package com.rafaelgpq.jsga.measure;

import com.rafaelgpq.jsga.model.Population;

import java.util.Arrays;

/**
 * Measures performance after each GA generation.
 */
public class MeasureEngine {

    private final double sigFactor;
    private final int windowSize;
    private final boolean trace;
    private final boolean trackOnline;
    private final boolean trackOffline;
    private final double[] window;

    private int generation = 0;
    private int windowCount = 0;
    private long trials = 0;

    private double worst = Double.NEGATIVE_INFINITY;
    private double online = 0;
    private double offline = 0;
    private double average = 0;

    public MeasureEngine(double sigFactor, int windowSize, boolean trace, boolean trackOnline, boolean trackOffline) {
        if (!Double.isFinite(sigFactor)) {
            throw new IllegalArgumentException("Sigma factor must be finite.");
        }
        if (windowSize < -1) {
            throw new IllegalArgumentException("Window size must be -1, zero, or a positive value.");
        }
        this.sigFactor = sigFactor;
        this.windowSize = windowSize;
        this.trace = trace;
        this.trackOnline = trackOnline;
        this.trackOffline = trackOffline;
        this.window = new double[Math.max(windowSize, 1)];
    }

    public void update(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        update(population, trials + population.size());
    }

    /**
     * Records one population observation and the cumulative number of objective evaluations.
     */
    public void update(Population population, long totalTrials) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        int popSize = population.size();
        if (popSize == 0) {
            throw new IllegalArgumentException("Cannot measure an empty population.");
        }
        if (totalTrials < trials) {
            throw new IllegalArgumentException("Total trials must not decrease.");
        }
        double scale = 0.0;
        double worstCurrent = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < popSize; i++) {
            if (population.get(i) == null) {
                throw new IllegalArgumentException("Population individuals must not be null.");
            }
            double perf = population.get(i).getFitness();
            if (!Double.isFinite(perf)) {
                throw new IllegalArgumentException("Population fitness values must be finite.");
            }
            scale = Math.max(scale, Math.abs(perf));
            if (perf > worstCurrent) {
                worstCurrent = perf;
            }
        }

        double normalizedSum = 0.0;
        double normalizedSumSquares = 0.0;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < popSize; i++) {
            double fitness = population.get(i).getFitness();
            double normalized = scale == 0.0 ? 0.0 : fitness / scale;
            normalizedSum += normalized;
            normalizedSumSquares += normalized * normalized;
            best = Math.min(best, fitness);
        }
        double normalizedAverage = normalizedSum / popSize;
        double avg = scale == 0.0 ? 0.0 : normalizedAverage * scale;
        double variance = popSize == 1 ? 0.0
                : Math.max(0.0, (normalizedSumSquares - popSize * normalizedAverage * normalizedAverage)
                / (popSize - 1));
        double sigma = scale == 0.0 ? 0.0 : Math.sqrt(variance) * scale;

        double nextWorst = worst;
        int nextWindowCount = windowCount;
        double[] nextWindow = window.clone();
        if (windowSize < 0) {
            nextWorst = avg + sigFactor * sigma;
        } else if (windowSize > 0) {
            nextWindow[generation % windowSize] = worstCurrent;
            nextWindowCount = Math.min(windowSize, windowCount + 1);
            nextWorst = Arrays.stream(nextWindow, 0, nextWindowCount).max().orElse(worstCurrent);
        } else {
            nextWorst = Math.max(worst, worstCurrent);
        }
        if (!Double.isFinite(avg) || !Double.isFinite(sigma) || !Double.isFinite(nextWorst)) {
            throw new IllegalArgumentException("Population statistics exceed the finite numeric range.");
        }
        if (generation == Integer.MAX_VALUE) {
            throw new IllegalStateException("Generation counter overflow.");
        }

        double nextGeneration = generation + 1.0;
        double nextOnline = generation == 0
                ? avg : online * (generation / nextGeneration) + avg / nextGeneration;
        double nextOffline = generation == 0
                ? best : offline * (generation / nextGeneration) + best / nextGeneration;
        if (!Double.isFinite(nextOnline) || !Double.isFinite(nextOffline)) {
            throw new IllegalArgumentException("Aggregate statistics exceed the finite numeric range.");
        }

        System.arraycopy(nextWindow, 0, window, 0, window.length);
        windowCount = nextWindowCount;
        worst = nextWorst;
        trials = totalTrials;
        generation++;
        online = nextOnline;
        offline = nextOffline;
        average = avg;

        if (trace) {
            System.out.printf("Gen %d, Evaluations %d\n", generation - 1, trials);
            if (trackOnline) System.out.printf("  Online:  %e\n", online);
            if (trackOffline) System.out.printf("  Offline: %e\n", offline);
        }

    }

    public double getWorst() {
        return worst;
    }

    public double getOnline() {
        return online;
    }

    public double getOffline() {
        return offline;
    }

    /**
     * Returns the mean fitness of the most recently measured population
     * (equivalent to GAucsd's {@code Ave_current_perf}).
     */
    public double getAverage() {
        return average;
    }

    public long getTrials() {
        return trials;
    }

    public int getGeneration() {
        return generation;
    }
}
