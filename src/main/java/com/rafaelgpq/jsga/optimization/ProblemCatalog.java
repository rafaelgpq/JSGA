package com.rafaelgpq.jsga.optimization;

import com.rafaelgpq.jsga.optimization.problems.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.Supplier;

/** Small deterministic instances that demonstrate each bundled optimization problem. */
public final class ProblemCatalog {

    private static final Map<String, Supplier<? extends OptimizationProblem<?>>> PROBLEMS = createProblems();

    private ProblemCatalog() {
    }

    public static List<String> ids() {
        return List.copyOf(PROBLEMS.keySet());
    }

    public static OptimizationProblem<?> create(String id) {
        Supplier<? extends OptimizationProblem<?>> factory = PROBLEMS.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown problem '" + id + "'. Available: "
                    + String.join(", ", ids()));
        }
        return factory.get();
    }

    private static Map<String, Supplier<? extends OptimizationProblem<?>>> createProblems() {
        Map<String, Supplier<? extends OptimizationProblem<?>>> problems = new LinkedHashMap<>();
        problems.put("onemax", () -> new OneMaxProblem(24));
        problems.put("knapsack", () -> new KnapsackProblem(
                new int[]{12, 7, 11, 8, 9}, new double[]{24, 13, 23, 15, 16}, 26));
        problems.put("tsp", () -> new TspProblem(List.of(
                new TspProblem.City(0, 0), new TspProblem.City(2, 1), new TspProblem.City(4, 0),
                new TspProblem.City(5, 3), new TspProblem.City(3, 5), new TspProblem.City(1, 4))));
        problems.put("graph-coloring", () -> new GraphColoringProblem(6, List.of(
                new GraphColoringProblem.Edge(0, 1), new GraphColoringProblem.Edge(1, 2),
                new GraphColoringProblem.Edge(2, 3), new GraphColoringProblem.Edge(3, 4),
                new GraphColoringProblem.Edge(4, 5), new GraphColoringProblem.Edge(5, 0),
                new GraphColoringProblem.Edge(0, 3), new GraphColoringProblem.Edge(1, 4))));
        problems.put("continuous", () -> new ContinuousFunctionProblem(
                3, -5.12, 5.12, ContinuousFunctionProblem.Function.SPHERE));
        problems.put("n-queens", () -> new NQueensProblem(8));
        problems.put("job-shop", () -> new JobShopSchedulingProblem(
                new int[][]{{3, 2, 2}, {2, 1, 4}, {4, 3, 1}},
                new int[][]{{0, 1, 2}, {1, 2, 0}, {2, 0, 1}}, 3));
        problems.put("vrp", () -> new VehicleRoutingProblem(List.of(
                new TspProblem.City(0, 0), new TspProblem.City(2, 1), new TspProblem.City(3, 4),
                new TspProblem.City(-1, 3), new TspProblem.City(-3, 1), new TspProblem.City(1, -3)),
                new int[]{2, 3, 2, 2, 3}, 6, 2));
        problems.put("bin-packing", () -> new BinPackingProblem(
                new int[]{4, 8, 1, 4, 2, 1, 5, 3}, 10));
        problems.put("max-cut", () -> new MaxCutProblem(6, List.of(
                new MaxCutProblem.WeightedEdge(0, 1, 1), new MaxCutProblem.WeightedEdge(0, 2, 2),
                new MaxCutProblem.WeightedEdge(1, 2, 1), new MaxCutProblem.WeightedEdge(1, 3, 2),
                new MaxCutProblem.WeightedEdge(2, 4, 3), new MaxCutProblem.WeightedEdge(3, 4, 1),
                new MaxCutProblem.WeightedEdge(3, 5, 2), new MaxCutProblem.WeightedEdge(4, 5, 1))));
        problems.put("set-cover", () -> new SetCoverProblem(6,
                List.of(List.of(0, 1, 2), List.of(1, 3), List.of(2, 4), List.of(3, 4, 5), List.of(0, 5)),
                new double[]{3, 2, 2, 3, 2}));
        problems.put("max-sat", () -> new MaxSatProblem(4, List.of(
                new MaxSatProblem.Clause(1, -2, 3), new MaxSatProblem.Clause(-1, 2),
                new MaxSatProblem.Clause(2, 4), new MaxSatProblem.Clause(-3, -4),
                new MaxSatProblem.Clause(1, 3, -4))));
        problems.put("feature-selection", () -> new FeatureSelectionProblem(
                new double[][]{{0.0, 0.0}, {0.1, 0.1}, {0.2, 0.0}, {1.0, 1.0}, {0.9, 1.0}, {1.1, 0.9}},
                new int[]{0, 0, 0, 1, 1, 1}));
        problems.put("symbolic-regression", () -> new SymbolicRegressionProblem(
                new double[]{-2, -1, 0, 1, 2}, new double[]{4, 1, 0, 1, 4}, 2, 8));
        problems.put("neural-network", () -> new NeuralNetworkWeightProblem(
                new double[][]{{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                new double[][]{{0}, {1}, {1}, {0}}, 4));
        return Collections.unmodifiableMap(problems);
    }
}
