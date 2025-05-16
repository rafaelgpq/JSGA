package org.example.jsga.core;

/**
 * Checks if the genetic algorithm run should be terminated.
 */
public class DoneChecker {

    private final int plateauLimit;
    private final long trialLimit;
    private final boolean lastFlag;

    private int plateauCounter = 0;
    private double previousBest = Double.MAX_VALUE;
    private boolean done = false;

    public DoneChecker(int plateauLimit, long trialLimit, boolean lastFlag) {
        this.plateauLimit = plateauLimit;
        this.trialLimit = trialLimit;
        this.lastFlag = lastFlag;
    }

    /**
     * Call this method after each generation with the best fitness found.
     */
    public void update(double currentBest, long totalTrials) {
        if (currentBest < previousBest) {
            previousBest = currentBest;
            plateauCounter = 0;
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
