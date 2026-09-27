package com.rafaelgpq.jsga.track;

import com.rafaelgpq.jsga.model.Individual;

/**
 * Optional statistics and best-tracking interface (stub).
 */
public interface StatisticsTracker {
    void incrementTrials();
    void updateBest(double fitness);
    double getBest();
    void accumulateOnSum(double fitness);
    void accumulateOffSum(double best);

    boolean shouldSaveBest();
    void saveBest(Individual individual);

    boolean shouldDump();
    void dumpCheckpoint();
}