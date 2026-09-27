package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Four-color graph coloring; fitness prioritizes zero edge conflicts, then fewer colors. */
public final class GraphColoringProblem implements OptimizationProblem<List<Integer>> {

    private final int vertices;
    private final List<Edge> edges;

    public GraphColoringProblem(int vertices, List<Edge> edges) {
        if (vertices <= 0 || edges == null) throw new IllegalArgumentException("Graph is invalid.");
        for (Edge edge : edges) {
            if (edge == null || edge.first < 0 || edge.second < 0
                    || edge.first >= vertices || edge.second >= vertices || edge.first == edge.second) {
                throw new IllegalArgumentException("Graph edge has an invalid endpoint.");
            }
        }
        this.vertices = vertices;
        this.edges = List.copyOf(edges);
    }

    @Override public String name() { return "Four-Color Graph Coloring"; }

    @Override
    public List<Integer> randomSolution(Random random) {
        List<Integer> colors = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) colors.add(random.nextInt(4));
        return List.copyOf(colors);
    }

    @Override
    public double evaluate(List<Integer> colors) {
        validate(colors);
        int conflicts = 0;
        boolean[] used = new boolean[4];
        int usedCount = 0;
        for (int color : colors) {
            if (!used[color]) {
                used[color] = true;
                usedCount++;
            }
        }
        for (Edge edge : edges) if (colors.get(edge.first).equals(colors.get(edge.second))) conflicts++;
        return conflicts * (vertices + 1.0) + usedCount;
    }

    @Override
    public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validate(first);
        validate(second);
        List<Integer> child = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) child.add(random.nextBoolean() ? first.get(i) : second.get(i));
        return List.copyOf(child);
    }

    @Override
    public List<Integer> mutate(List<Integer> solution, double rate, Random random) {
        validate(solution);
        List<Integer> child = new ArrayList<>(solution);
        for (int i = 0; i < vertices; i++) {
            if (random.nextDouble() < rate) child.set(i, random.nextInt(4));
        }
        return List.copyOf(child);
    }

    @Override public boolean isSolved(double fitness) { return fitness <= 4.0; }

    private void validate(List<Integer> colors) {
        if (colors == null || colors.size() != vertices
                || colors.stream().anyMatch(color -> color == null || color < 0 || color >= 4)) {
            throw new IllegalArgumentException("Coloring must assign one of four colors to every vertex.");
        }
    }

    public static final class Edge {
        private final int first;
        private final int second;

        public Edge(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() { return first; }
        public int getSecond() { return second; }
    }
}
