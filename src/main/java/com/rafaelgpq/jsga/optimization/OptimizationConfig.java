package com.rafaelgpq.jsga.optimization;

/**
 * Immutable settings for the reusable, representation-independent GA runner.
 */
public final class OptimizationConfig {

    private final int populationSize;
    private final int maxGenerations;
    private final int tournamentSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final long seed;

    public OptimizationConfig(int populationSize, int maxGenerations, int tournamentSize,
                              double crossoverRate, double mutationRate, long seed) {
        if (populationSize < 2) {
            throw new IllegalArgumentException("Population size must be at least two.");
        }
        if (maxGenerations < 0) {
            throw new IllegalArgumentException("Maximum generations must not be negative.");
        }
        if (tournamentSize < 1 || tournamentSize > populationSize) {
            throw new IllegalArgumentException("Tournament size must be between one and population size.");
        }
        if (!Double.isFinite(crossoverRate) || crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("Crossover rate must be between zero and one.");
        }
        if (!Double.isFinite(mutationRate) || mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("Mutation rate must be between zero and one.");
        }
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.tournamentSize = tournamentSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.seed = seed;
    }

    public int getPopulationSize() { return populationSize; }
    public int getMaxGenerations() { return maxGenerations; }
    public int getTournamentSize() { return tournamentSize; }
    public double getCrossoverRate() { return crossoverRate; }
    public double getMutationRate() { return mutationRate; }
    public long getSeed() { return seed; }
}
