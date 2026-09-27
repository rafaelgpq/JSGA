package com.rafaelgpq.jsga.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Runs a minimization GA while delegating representation and variation to each problem.
 */
public final class GeneticAlgorithm {

    public <S> OptimizationResult<S> solve(OptimizationProblem<S> problem, OptimizationConfig config) {
        Objects.requireNonNull(problem, "Optimization problem must not be null.");
        Objects.requireNonNull(config, "Optimization configuration must not be null.");
        if (problem.name() == null || problem.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Optimization problem name must not be empty.");
        }
        Random random = new Random(config.getSeed());
        List<ScoredSolution<S>> population = new ArrayList<>(config.getPopulationSize());
        long evaluations = 0;
        for (int i = 0; i < config.getPopulationSize(); i++) {
            S solution = Objects.requireNonNull(problem.randomSolution(random),
                    "Problem generated a null solution.");
            double fitness = evaluate(problem, solution);
            population.add(new ScoredSolution<>(solution, fitness));
            evaluations++;
        }

        ScoredSolution<S> best = bestOf(population);
        if (problem.isSolved(best.fitness)) {
            return result(best, 0, evaluations);
        }

        int generations = 0;
        while (generations < config.getMaxGenerations()) {
            List<ScoredSolution<S>> next = new ArrayList<>(config.getPopulationSize());
            next.add(best);
            while (next.size() < config.getPopulationSize()) {
                S first = select(population, config.getTournamentSize(), random).solution;
                S second = select(population, config.getTournamentSize(), random).solution;
                S child = random.nextDouble() < config.getCrossoverRate()
                        ? problem.crossover(first, second, random)
                        : first;
                child = Objects.requireNonNull(child, "Problem generated a null crossover result.");
                child = Objects.requireNonNull(problem.mutate(child, config.getMutationRate(), random),
                        "Problem generated a null mutant.");
                double fitness = evaluate(problem, child);
                next.add(new ScoredSolution<>(child, fitness));
                evaluations++;
            }
            population = next;
            ScoredSolution<S> generationBest = bestOf(population);
            if (generationBest.fitness < best.fitness) {
                best = generationBest;
            }
            generations++;
            if (problem.isSolved(best.fitness)) {
                break;
            }
        }
        return result(best, generations, evaluations);
    }

    private static <S> double evaluate(OptimizationProblem<S> problem, S solution) {
        double fitness = problem.evaluate(solution);
        if (!Double.isFinite(fitness)) {
            throw new IllegalArgumentException("Problem '" + problem.name() + "' returned non-finite fitness.");
        }
        return fitness;
    }

    private static <S> ScoredSolution<S> select(List<ScoredSolution<S>> population,
                                                int tournamentSize, Random random) {
        ScoredSolution<S> winner = population.get(random.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            ScoredSolution<S> candidate = population.get(random.nextInt(population.size()));
            if (candidate.fitness < winner.fitness) winner = candidate;
        }
        return winner;
    }

    private static <S> ScoredSolution<S> bestOf(List<ScoredSolution<S>> population) {
        ScoredSolution<S> best = population.get(0);
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).fitness < best.fitness) best = population.get(i);
        }
        return best;
    }

    private static <S> OptimizationResult<S> result(ScoredSolution<S> best,
                                                    int generations, long evaluations) {
        return new OptimizationResult<>(best.solution, best.fitness, generations, evaluations);
    }

    private static final class ScoredSolution<S> {
        private final S solution;
        private final double fitness;

        private ScoredSolution(S solution, double fitness) {
            this.solution = solution;
            this.fitness = fitness;
        }
    }
}
