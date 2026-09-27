package com.rafaelgpq.jsga.optimization;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizationFrameworkTest {

    @Test
    void validatesEveryConfigurationBoundaryAndExposesImmutableSettings() {
        assertThatThrownBy(() -> new OptimizationConfig(1, 0, 1, 0.5, 0.1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OptimizationConfig(2, -1, 1, 0.5, 0.1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OptimizationConfig(2, 0, 0, 0.5, 0.1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OptimizationConfig(2, 0, 3, 0.5, 0.1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        for (double rate : new double[]{Double.NaN, -0.1, 1.1}) {
            assertThatThrownBy(() -> new OptimizationConfig(2, 0, 1, rate, 0.1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new OptimizationConfig(2, 0, 1, 0.5, rate, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        OptimizationConfig config = new OptimizationConfig(8, 5, 2, 0.75, 0.1, -7L);
        assertThat(config.getPopulationSize()).isEqualTo(8);
        assertThat(config.getMaxGenerations()).isEqualTo(5);
        assertThat(config.getTournamentSize()).isEqualTo(2);
        assertThat(config.getCrossoverRate()).isEqualTo(0.75);
        assertThat(config.getMutationRate()).isEqualTo(0.1);
        assertThat(config.getSeed()).isEqualTo(-7L);
    }

    @Test
    void geneticAlgorithmSupportsNoCrossoverAndEarlySolvedInitialPopulation() {
        OptimizationProblem<List<Boolean>> problem = new OptimizationProblem<List<Boolean>>() {
            @Override public String name() { return "fixed"; }
            @Override public List<Boolean> randomSolution(Random random) { return List.of(true); }
            @Override public double evaluate(List<Boolean> solution) { return solution.get(0) ? 0.0 : 1.0; }
            @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
                throw new AssertionError("Crossover should not run.");
            }
            @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
                return solution;
            }
            @Override public boolean isSolved(double fitness) { return fitness == 0.0; }
        };

        OptimizationResult<List<Boolean>> result = new GeneticAlgorithm().solve(problem,
                new OptimizationConfig(4, 10, 2, 0.0, 0.0, 1L));

        assertThat(result.getBestFitness()).isZero();
        assertThat(result.getBestSolution()).containsExactly(true);
        assertThat(result.getGenerations()).isZero();
        assertThat(result.getEvaluations()).isEqualTo(4);
    }

    @Test
    void rejectsInvalidProblemDefinitionsSolutionsAndFitness() {
        GeneticAlgorithm algorithm = new GeneticAlgorithm();
        OptimizationConfig config = new OptimizationConfig(2, 1, 1, 0.0, 0.0, 1L);
        assertThatThrownBy(() -> algorithm.solve(null, config)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> algorithm.solve(new InvalidProblem(" "), config))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("name");
        assertThatThrownBy(() -> algorithm.solve(new InvalidProblem("bad-fitness"), config))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("non-finite");
        assertThatThrownBy(() -> algorithm.solve(new InvalidProblem("null-solution"), config))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("null solution");
    }

    @Test
    void optimizationUtilitiesKeepPermutationAndBoundInvariants() {
        Random random = new Random(37);
        assertThat(OptimizationUtils.randomBits(5, random)).hasSize(5);
        assertThat(OptimizationUtils.uniformBits(List.of(true, false), List.of(false, true), random))
                .hasSize(2);
        assertThat(OptimizationUtils.mutateBits(List.of(true, false), 1.0, random))
                .containsExactly(false, true);
        assertThat(OptimizationUtils.randomPermutation(1, random)).containsExactly(0);
        assertThat(OptimizationUtils.orderedCrossover(List.of(4), List.of(4), random)).containsExactly(4);
        List<Integer> first = List.of(0, 1, 2, 3, 4);
        List<Integer> second = List.of(4, 3, 2, 1, 0);
        assertThat(OptimizationUtils.orderedCrossover(first, second, random))
                .containsExactlyInAnyOrder(0, 1, 2, 3, 4);
        assertThat(OptimizationUtils.swapMutation(List.of(1), 1.0, random)).containsExactly(1);
        assertThat(OptimizationUtils.swapMutation(first, 1.0, random))
                .containsExactlyInAnyOrder(0, 1, 2, 3, 4);
        assertThat(OptimizationUtils.randomValues(20, -2, 3, random))
                .allMatch(value -> value >= -2.0 && value <= 3.0);
        assertThat(OptimizationUtils.blendAndMutate(List.of(-10.0), List.of(10.0),
                1.0, -1.0, 1.0, random)).singleElement()
                .satisfies(value -> assertThat(value).isBetween(-1.0, 1.0));
        assertThatThrownBy(() -> OptimizationUtils.uniformBits(List.of(true), List.of(), random))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OptimizationUtils.orderedCrossover(null, List.of(), random))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class InvalidProblem implements OptimizationProblem<List<Boolean>> {
        private final String problemName;

        private InvalidProblem(String problemName) {
            this.problemName = problemName;
        }

        @Override public String name() { return problemName; }
        @Override public List<Boolean> randomSolution(Random random) {
            return "null-solution".equals(problemName) ? null : List.of(true);
        }
        @Override public double evaluate(List<Boolean> solution) { return Double.NaN; }
        @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
            return first;
        }
        @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
            return solution;
        }
    }
}
