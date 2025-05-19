package org.example.jsga.track.impl;

import org.example.jsga.model.Individual;
import org.example.jsga.track.StatisticsTracker;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Tracks basic GA performance statistics and logs them to a CSV file.
 */
public class BasicStatsTracker implements StatisticsTracker {

    private int trialCount = 0;
    private double bestFitness = Double.POSITIVE_INFINITY;
    private double onSum = 0.0;
    private double offSum = 0.0;
    private final boolean logToFile;
    private final String outputFile;

    public BasicStatsTracker(boolean logToFile, String outputFile) {
        this.logToFile = logToFile;
        this.outputFile = outputFile;
        if (logToFile) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("Trial,BestFitness,OnlineSum,OfflineSum");
            } catch (IOException e) {
                System.err.println("[BasicStatsTracker] Failed to initialize output file: " + e.getMessage());
            }
        }
    }

    @Override
    public void incrementTrials() {
        trialCount++;
    }

    @Override
    public void updateBest(double fitness) {
        if (fitness < bestFitness) {
            bestFitness = fitness;
        }
    }

    @Override
    public double getBest() {
        return bestFitness;
    }

    @Override
    public void accumulateOnSum(double fitness) {
        onSum += fitness;
    }

    @Override
    public void accumulateOffSum(double fitness) {
        offSum += fitness;
    }

    @Override
    public boolean shouldSaveBest() {
        return false; // Hook for BestSetManager if needed
    }

    @Override
    public void saveBest(Individual i) {
        // Not used in basic stats
    }

    @Override
    public boolean shouldDump() {
        return logToFile;
    }

    @Override
    public void dumpCheckpoint() {
        if (!logToFile) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.printf("%d,%.5f,%.5f,%.5f\n", trialCount, bestFitness, onSum, offSum);
        } catch (IOException e) {
            System.err.println("[BasicStatsTracker] Failed to write checkpoint: " + e.getMessage());
        }
    }
}
