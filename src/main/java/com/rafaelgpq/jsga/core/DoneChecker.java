package com.rafaelgpq.jsga.core;

/**
 * Checks if the genetic algorithm run should be terminated.
 */
public class DoneChecker {

    private final int plateauLimit;
    private final long trialLimit;
    private final boolean lastFlag;

    private int plateauCounter = 0;
    private double previousBest;
    private boolean hasPreviousBest;
    private boolean done = false;

    public DoneChecker(int plateauLimit, long trialLimit, boolean lastFlag) {
        if (plateauLimit < 0) {
            throw new IllegalArgumentException("Plateau limit must not be negative.");
        }
        if (trialLimit < 0) {
            throw new IllegalArgumentException("Trial limit must not be negative.");
        }
        this.plateauLimit = plateauLimit;
        this.trialLimit = trialLimit;
        this.lastFlag = lastFlag;
    }

    /**
     * Call this method after each generation with the best fitness found.
     */
    public void update(double currentBest, long totalTrials) {
        if (!Double.isFinite(currentBest)) {
            throw new IllegalArgumentException("Best fitness must be finite.");
        }
        if (totalTrials < 0) {
            throw new IllegalArgumentException("Total trials must not be negative.");
        }
        if (!hasPreviousBest || currentBest < previousBest) {
            previousBest = currentBest;
            plateauCounter = 0;
            hasPreviousBest = true;
        } else {
            plateauCounter++;
        }

        if ((plateauLimit > 0 && plateauCounter >= plateauLimit) ||
                (trialLimit > 0 && totalTrials >= trialLimit) ||
                lastFlag) {
            done = true;
        }
    }

    public boolean isDone() {
        return done;
    }

    public int getPlateauCounter() {
        return plateauCounter;
    }

    public double getPreviousBest() {
        return previousBest;
    }
}
