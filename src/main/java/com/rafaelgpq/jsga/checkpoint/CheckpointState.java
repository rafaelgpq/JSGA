package com.rafaelgpq.jsga.checkpoint;

import com.rafaelgpq.jsga.model.Population;

/**
 * Generation and population restored from a checkpoint.
 */
public final class CheckpointState {

    private final int generation;
    private final Population population;
    private final long evaluationCount;
    private final long[] randomState;

    public CheckpointState(int generation, Population population) {
        this(generation, population, -1L, null);
    }

    public CheckpointState(int generation, Population population, long evaluationCount, long[] randomState) {
        if (generation < 0 || population == null || population.size() == 0) {
            throw new IllegalArgumentException("Checkpoint state requires a non-negative generation and population.");
        }
        if (evaluationCount < -1L) {
            throw new IllegalArgumentException("Checkpoint evaluation count must be non-negative or unavailable.");
        }
        this.generation = generation;
        this.population = population;
        this.evaluationCount = evaluationCount;
        this.randomState = randomState == null ? null : randomState.clone();
    }

    public int getGeneration() {
        return generation;
    }

    public Population getPopulation() {
        return population;
    }

    public long getEvaluationCount() {
        return evaluationCount;
    }

    public long[] getRandomState() {
        return randomState == null ? null : randomState.clone();
    }
}
