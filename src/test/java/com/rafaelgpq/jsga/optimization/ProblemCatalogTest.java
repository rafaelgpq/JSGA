package com.rafaelgpq.jsga.optimization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.optimization")
class ProblemCatalogTest {

    @Test
    void catalogExposesAndRunsEveryBundledProblem() {
        assertThat(ProblemCatalog.ids()).hasSize(15).contains(
                "onemax", "knapsack", "tsp", "graph-coloring", "continuous", "n-queens",
                "job-shop", "vrp", "bin-packing", "max-cut", "set-cover", "max-sat",
                "feature-selection", "symbolic-regression", "neural-network");

        OptimizationConfig config = new OptimizationConfig(12, 3, 3, 0.9, 0.2, 44L);
        GeneticAlgorithm algorithm = new GeneticAlgorithm();
        for (String id : ProblemCatalog.ids()) {
            OptimizationResult<?> result = solve(algorithm, ProblemCatalog.create(id), config);
            assertThat(result.getBestFitness()).as(id).isFinite();
            assertThat(result.getGenerations()).as(id).isBetween(0, 3);
            assertThat(result.getEvaluations()).as(id).isGreaterThanOrEqualTo(12);
        }
    }

    @Test
    void rejectsUnknownCatalogEntries() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ProblemCatalog.create("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Available");
    }

    @Test
    void seededRunsAreReproducible() {
        OptimizationConfig config = new OptimizationConfig(30, 20, 3, 0.9, 0.05, 77L);
        GeneticAlgorithm algorithm = new GeneticAlgorithm();
        OptimizationResult<java.util.List<Boolean>> first =
                algorithm.solve(new com.rafaelgpq.jsga.optimization.problems.OneMaxProblem(12), config);
        OptimizationResult<java.util.List<Boolean>> second =
                algorithm.solve(new com.rafaelgpq.jsga.optimization.problems.OneMaxProblem(12), config);

        assertThat(second.getBestFitness()).isEqualTo(first.getBestFitness());
        assertThat(second.getBestSolution()).isEqualTo(first.getBestSolution());
        assertThat(second.getEvaluations()).isEqualTo(first.getEvaluations());
    }

    @Test
    void demoCliRunsASeededFamilyWithConfigurableParameters() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            ProblemDemoMain.main(new String[]{"onemax", "--generations", "2", "--population", "12",
                    "--tournament", "2", "--crossover", "0.8", "--mutation", "0.1",
                    "--seed", "99", "--runs", "2"});
        } finally {
            System.setOut(original);
        }
        String report = output.toString(StandardCharsets.UTF_8);
        assertThat(report).contains("population=12 generations=2 tournament=2",
                "crossover=0.800 mutation=0.100 runs=2",
                "run=1 seed=99", "run=2 seed=100", "family summary:");
    }

    @Test
    void demoCliRejectsInvalidAndInconsistentParameters() {
        assertThatThrownBy(() -> ProblemDemoMain.main(
                new String[]{"onemax", "--population", "4", "--tournament", "5"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not exceed");
        assertThatThrownBy(() -> ProblemDemoMain.main(
                new String[]{"onemax", "--mutation", "1.1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between zero and one");
        assertThatThrownBy(() -> ProblemDemoMain.main(new String[]{"onemax", "--unknown", "1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown option");
        assertThatThrownBy(() -> ProblemDemoMain.main(new String[]{"onemax", "--seed"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing value");
        assertThatThrownBy(() -> ProblemDemoMain.main(new String[]{"onemax", "--generations", "many"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid integer");
        assertThatThrownBy(() -> ProblemDemoMain.main(new String[]{"onemax", "--seed", "many"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid integer");
        assertThatThrownBy(() -> ProblemDemoMain.main(new String[]{"onemax", "--crossover", "many"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid rate");
    }

    private static <S> OptimizationResult<S> solve(GeneticAlgorithm algorithm,
                                                    OptimizationProblem<S> problem,
                                                    OptimizationConfig config) {
        return algorithm.solve(problem, config);
    }
}
