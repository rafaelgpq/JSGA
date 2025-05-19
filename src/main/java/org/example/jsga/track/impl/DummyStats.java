package org.example.jsga.track.impl;

import org.example.jsga.model.Individual;
import org.example.jsga.track.StatisticsTracker;

public class DummyStats implements StatisticsTracker {

    @Override
    public void incrementTrials() {}

    @Override
    public void updateBest(double f) {}

    @Override
    public double getBest() { return 0; }

    @Override
    public void accumulateOnSum(double f) {}

    @Override
    public void accumulateOffSum(double b) {}

    @Override
    public boolean shouldSaveBest() { return false; }

    @Override
    public void saveBest(Individual i) {}

    @Override
    public boolean shouldDump() { return false; }

    @Override
    public void dumpCheckpoint() {}
}

