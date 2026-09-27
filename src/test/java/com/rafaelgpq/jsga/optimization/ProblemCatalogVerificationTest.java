package com.rafaelgpq.jsga.optimization;

import com.rafaelgpq.jsga.optimization.problems.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemCatalogVerificationTest {

    private static final OptimizationConfig DEMO_CONFIG =
            new OptimizationConfig(80, 150, 3, 0.9, 0.04, 12345L);

    @Test
    void seededDiscreteDemosMatchExhaustiveOrProvenOptima() {
        verifyExact(new OneMaxProblem(24), -24.0);
        KnapsackProblem knapsack = knapsack();
        verifyExact(knapsack, exhaustiveBits(knapsack, 5));
        verifyExact(tsp(), exhaustivePermutations(tsp(), 6));
        assertThat(exhaustiveColorings(coloring(), 6, 4)).isEqualTo(2.0);
        verifyQuality(coloring(), 4.0);
        verifyExact(new NQueensProblem(8), exhaustivePermutations(new NQueensProblem(8), 8));
        verifyExact(jobShop(), exhaustiveJobShop(jobShop()));
        verifyExact(vrp(), exhaustivePermutations(vrp(), 5));
        BinPackingProblem packing = new BinPackingProblem(new int[]{4, 8, 1, 4, 2, 1, 5, 3}, 10);
        assertThat((28 + 10 - 1) / 10).isEqualTo(3);
        verifyExact(packing, 3.0);
        verifyExact(maxCut(), exhaustiveBits(maxCut(), 6));
        verifyExact(setCover(), exhaustiveBits(setCover(), 5));
        verifyExact(maxSat(), exhaustiveBits(maxSat(), 4));
        verifyExact(featureSelection(), exhaustiveBits(featureSelection(), 2));
    }

    @Test
    void continuousAndLearningDemosMeetDocumentedQualityThresholds() {
        verifyQuality(new ContinuousFunctionProblem(
                3, -5.12, 5.12, ContinuousFunctionProblem.Function.SPHERE), 1e-6);
        verifyQuality(new SymbolicRegressionProblem(
                new double[]{-2, -1, 0, 1, 2}, new double[]{4, 1, 0, 1, 4}, 2, 8), 1e-6);
        verifyQuality(new NeuralNetworkWeightProblem(
                new double[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                new double[][]{{0}, {1}, {1}, {0}}, 4), 0.02);
    }

    private static <S> void verifyExact(OptimizationProblem<S> problem, double exactOptimum) {
        OptimizationResult<S> result = new GeneticAlgorithm().solve(problem, DEMO_CONFIG);
        assertThat(result.getBestFitness()).as(problem.name()).isCloseTo(exactOptimum,
                org.assertj.core.data.Offset.offset(1e-9));
        assertThat(problem.evaluate(result.getBestSolution())).as(problem.name())
                .isCloseTo(result.getBestFitness(), org.assertj.core.data.Offset.offset(1e-12));
    }

    private static <S> void verifyQuality(OptimizationProblem<S> problem, double maximumFitness) {
        OptimizationResult<S> result = new GeneticAlgorithm().solve(problem, DEMO_CONFIG);
        assertThat(result.getBestFitness()).as(problem.name()).isBetween(0.0, maximumFitness);
        assertThat(problem.evaluate(result.getBestSolution())).as(problem.name())
                .isCloseTo(result.getBestFitness(), org.assertj.core.data.Offset.offset(1e-12));
    }

    private static double exhaustiveBits(OptimizationProblem<List<Boolean>> problem, int size) {
        double best = Double.POSITIVE_INFINITY;
        for (int mask = 0; mask < (1 << size); mask++) {
            List<Boolean> solution = new ArrayList<>(size);
            for (int bit = 0; bit < size; bit++) solution.add((mask & (1 << bit)) != 0);
            best = Math.min(best, problem.evaluate(solution));
        }
        return best;
    }

    private static double exhaustivePermutations(OptimizationProblem<List<Integer>> problem, int size) {
        return permuteAndEvaluate(problem, new ArrayList<>(), new boolean[size], size,
                Double.POSITIVE_INFINITY);
    }

    private static double permuteAndEvaluate(OptimizationProblem<List<Integer>> problem,
                                               List<Integer> prefix, boolean[] used,
                                               int size, double best) {
        if (prefix.size() == size) return Math.min(best, problem.evaluate(prefix));
        for (int value = 0; value < size; value++) {
            if (used[value]) continue;
            used[value] = true;
            prefix.add(value);
            best = permuteAndEvaluate(problem, prefix, used, size, best);
            prefix.remove(prefix.size() - 1);
            used[value] = false;
        }
        return best;
    }

    private static double exhaustiveColorings(GraphColoringProblem problem, int vertices, int colors) {
        double best = Double.POSITIVE_INFINITY;
        for (int encoding = 0; encoding < Math.pow(colors, vertices); encoding++) {
            int remaining = encoding;
            List<Integer> assignment = new ArrayList<>(vertices);
            for (int vertex = 0; vertex < vertices; vertex++) {
                assignment.add(remaining % colors);
                remaining /= colors;
            }
            best = Math.min(best, problem.evaluate(assignment));
        }
        return best;
    }

    private static double exhaustiveJobShop(JobShopSchedulingProblem problem) {
        return enumerateJobShop(problem, new ArrayList<>(), new int[3], Double.POSITIVE_INFINITY);
    }

    private static double enumerateJobShop(JobShopSchedulingProblem problem, List<Integer> priorities,
                                           int[] nextOperations, double best) {
        if (priorities.size() == 9) return Math.min(best, problem.evaluate(priorities));
        for (int job = 0; job < nextOperations.length; job++) {
            if (nextOperations[job] == 3) continue;
            priorities.add(job * 3 + nextOperations[job]++);
            best = enumerateJobShop(problem, priorities, nextOperations, best);
            priorities.remove(priorities.size() - 1);
            nextOperations[job]--;
        }
        return best;
    }

    private static TspProblem tsp() {
        return new TspProblem(List.of(new TspProblem.City(0, 0), new TspProblem.City(2, 1),
                new TspProblem.City(4, 0), new TspProblem.City(5, 3),
                new TspProblem.City(3, 5), new TspProblem.City(1, 4)));
    }

    private static KnapsackProblem knapsack() {
        return new KnapsackProblem(
                new int[]{12, 7, 11, 8, 9}, new double[]{24, 13, 23, 15, 16}, 26);
    }

    private static GraphColoringProblem coloring() {
        return new GraphColoringProblem(6, List.of(
                new GraphColoringProblem.Edge(0, 1), new GraphColoringProblem.Edge(1, 2),
                new GraphColoringProblem.Edge(2, 3), new GraphColoringProblem.Edge(3, 4),
                new GraphColoringProblem.Edge(4, 5), new GraphColoringProblem.Edge(5, 0),
                new GraphColoringProblem.Edge(0, 3), new GraphColoringProblem.Edge(1, 4)));
    }

    private static JobShopSchedulingProblem jobShop() {
        return new JobShopSchedulingProblem(
                new int[][]{{3, 2, 2}, {2, 1, 4}, {4, 3, 1}},
                new int[][]{{0, 1, 2}, {1, 2, 0}, {2, 0, 1}}, 3);
    }

    private static VehicleRoutingProblem vrp() {
        return new VehicleRoutingProblem(List.of(new TspProblem.City(0, 0),
                new TspProblem.City(2, 1), new TspProblem.City(3, 4),
                new TspProblem.City(-1, 3), new TspProblem.City(-3, 1),
                new TspProblem.City(1, -3)), new int[]{2, 3, 2, 2, 3}, 6, 2);
    }

    private static MaxCutProblem maxCut() {
        return new MaxCutProblem(6, List.of(
                new MaxCutProblem.WeightedEdge(0, 1, 1), new MaxCutProblem.WeightedEdge(0, 2, 2),
                new MaxCutProblem.WeightedEdge(1, 2, 1), new MaxCutProblem.WeightedEdge(1, 3, 2),
                new MaxCutProblem.WeightedEdge(2, 4, 3), new MaxCutProblem.WeightedEdge(3, 4, 1),
                new MaxCutProblem.WeightedEdge(3, 5, 2), new MaxCutProblem.WeightedEdge(4, 5, 1)));
    }

    private static SetCoverProblem setCover() {
        return new SetCoverProblem(6,
                List.of(List.of(0, 1, 2), List.of(1, 3), List.of(2, 4),
                        List.of(3, 4, 5), List.of(0, 5)),
                new double[]{3, 2, 2, 3, 2});
    }

    private static MaxSatProblem maxSat() {
        return new MaxSatProblem(4, List.of(
                new MaxSatProblem.Clause(1, -2, 3), new MaxSatProblem.Clause(-1, 2),
                new MaxSatProblem.Clause(2, 4), new MaxSatProblem.Clause(-3, -4),
                new MaxSatProblem.Clause(1, 3, -4)));
    }

    private static FeatureSelectionProblem featureSelection() {
        return new FeatureSelectionProblem(
                new double[][]{{0, 0}, {0.1, 0.1}, {0.2, 0}, {1, 1}, {0.9, 1}, {1.1, 0.9}},
                new int[]{0, 0, 0, 1, 1, 1});
    }
}
