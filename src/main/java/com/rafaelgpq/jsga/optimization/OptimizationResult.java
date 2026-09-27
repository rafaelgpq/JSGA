package com.rafaelgpq.jsga.optimization;

/**
 * Immutable summary of one genetic algorithm run.
 */
public final class OptimizationResult<S> {

    private final S bestSolution;
    private final double bestFitness;
    private final int generations;
    private final long evaluations;

    OptimizationResult(S bestSolution, double bestFitness, int generations, long evaluations) {
        this.bestSolution = bestSolution;
        this.bestFitness = bestFitness;
        this.generations = generations;
        this.evaluations = evaluations;
    }

    public S getBestSolution() { return bestSolution; }
    public double getBestFitness() { return bestFitness; }
    public int getGenerations() { return generations; }
    public long getEvaluations() { return evaluations; }
}
