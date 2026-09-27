package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** Weighted Maximum Cut represented by a Boolean partition assignment. */
public final class MaxCutProblem implements OptimizationProblem<List<Boolean>> {

    private final int vertices;
    private final List<WeightedEdge> edges;

    public MaxCutProblem(int vertices, List<WeightedEdge> edges) {
        if (vertices < 2 || edges == null || edges.isEmpty()) {
            throw new IllegalArgumentException("Max-Cut requires a non-empty graph with at least two vertices.");
        }
        for (WeightedEdge edge : edges) {
            if (edge == null || edge.first < 0 || edge.second < 0 || edge.first >= vertices
                    || edge.second >= vertices || edge.first == edge.second
                    || !Double.isFinite(edge.weight) || edge.weight <= 0.0) {
                throw new IllegalArgumentException("Max-Cut edge is invalid.");
            }
        }
        this.vertices = vertices;
        this.edges = List.copyOf(edges);
    }

    @Override public String name() { return "Maximum Cut"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(vertices, random);
    }

    @Override
    public double evaluate(List<Boolean> partition) {
        OneMaxProblem.requireLength(partition, vertices);
        double cutWeight = 0.0;
        for (WeightedEdge edge : edges) {
            if (partition.get(edge.first) != partition.get(edge.second)) cutWeight += edge.weight;
        }
        return -cutWeight;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        OneMaxProblem.requireLength(first, vertices);
        OneMaxProblem.requireLength(second, vertices);
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
        OneMaxProblem.requireLength(solution, vertices);
        return OptimizationUtils.mutateBits(solution, rate, random);
    }

    public static final class WeightedEdge {
        private final int first;
        private final int second;
        private final double weight;

        public WeightedEdge(int first, int second, double weight) {
            this.first = first;
            this.second = second;
            this.weight = weight;
        }
    }
}
