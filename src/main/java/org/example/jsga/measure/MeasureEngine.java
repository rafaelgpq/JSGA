package org.example.jsga.measure;

import org.example.jsga.model.Population;

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
    private long trials = 0;
    private double onSum = 0;
    private double offSum = 0;

    private double worst = Double.MIN_VALUE;
    private double online = 0;
    private double offline = 0;

    public MeasureEngine(double sigFactor, int windowSize, boolean trace, boolean trackOnline, boolean trackOffline) {
        this.sigFactor = sigFactor;
        this.windowSize = windowSize;
        this.trace = trace;
        this.trackOnline = trackOnline;
        this.trackOffline = trackOffline;
        this.window = new double[Math.max(windowSize, 1)];
    }

    public void update(Population population) {
        int popSize = population.size();
        double sum = 0;
        double sumSq = 0;
        double best = Double.MAX_VALUE;
        double worstCurrent = Double.MIN_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < popSize; i++) {
            double perf = population.get(i).getFitness();
            sum += perf;
            sumSq += perf * perf;
            if (perf < best) {
                best = perf;
                bestIndex = i;
            }
            if (perf > worstCurrent) {
                worstCurrent = perf;
            }
        }

        double avg = sum / popSize;
        double sigma = Math.sqrt(Math.max(0, (sumSq - popSize * avg * avg) / (popSize - 1)));

        // Update worst based on scaling
        if (windowSize < 0) {
            worst = avg + sigFactor * sigma;
        } else if (windowSize > 0) {
            window[generation % windowSize] = worstCurrent;
            worst = Arrays.stream(window).max().orElse(worstCurrent);
        } else {
            worst = Math.max(worst, worstCurrent);
        }

        // Online/offline metrics
        trials++;
        onSum += avg;
        offSum += best;
        online = onSum / trials;
        offline = offSum / trials;

        if (trace) {
            System.out.printf("Gen %d, Trials %d\n", generation, trials);
            if (trackOnline) System.out.printf("  Online:  %e\n", online);
            if (trackOffline) System.out.printf("  Offline: %e\n", offline);
        }

        generation++;
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

    public long getTrials() {
        return trials;
    }

    public int getGeneration() {
        return generation;
    }
}
