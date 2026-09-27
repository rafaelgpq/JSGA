package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Euclidean TSP with ordered crossover, swap mutation, and 2-opt local improvement. */
public final class TspProblem implements OptimizationProblem<List<Integer>> {

    private final List<City> cities;

    public TspProblem(List<City> cities) {
        if (cities == null || cities.size() < 3) {
            throw new IllegalArgumentException("TSP requires at least three cities.");
        }
        if (cities.stream().anyMatch(city -> city == null)) {
            throw new IllegalArgumentException("TSP cities must not be null.");
        }
        this.cities = List.copyOf(cities);
    }

    @Override public String name() { return "Traveling Salesman"; }
    @Override public List<Integer> randomSolution(Random random) {
        return OptimizationUtils.randomPermutation(cities.size(), random);
    }

    @Override
    public double evaluate(List<Integer> tour) {
        validateTour(tour);
        double distance = 0.0;
        for (int i = 0; i < tour.size(); i++) {
            City from = cities.get(tour.get(i));
            City to = cities.get(tour.get((i + 1) % tour.size()));
            distance += Math.hypot(from.x - to.x, from.y - to.y);
        }
        return distance;
    }

    @Override public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validateTour(first);
        validateTour(second);
        return improve(OptimizationUtils.orderedCrossover(first, second, random));
    }

    @Override public List<Integer> mutate(List<Integer> tour, double rate, Random random) {
        validateTour(tour);
        return improve(OptimizationUtils.swapMutation(tour, rate, random));
    }

    private List<Integer> improve(List<Integer> tour) {
        List<Integer> improved = new ArrayList<>(tour);
        boolean changed;
        do {
            changed = false;
            for (int first = 0; first < improved.size() - 1 && !changed; first++) {
                for (int last = first + 1; last < improved.size(); last++) {
                    if (first == 0 && last == improved.size() - 1) continue;
                    City before = cities.get(improved.get((first + improved.size() - 1) % improved.size()));
                    City start = cities.get(improved.get(first));
                    City end = cities.get(improved.get(last));
                    City after = cities.get(improved.get((last + 1) % improved.size()));
                    double oldEdges = distance(before, start) + distance(end, after);
                    double newEdges = distance(before, end) + distance(start, after);
                    if (newEdges + 1e-12 < oldEdges) {
                        Collections.reverse(improved.subList(first, last + 1));
                        changed = true;
                        break;
                    }
                }
            }
        } while (changed);
        return List.copyOf(improved);
    }

    private static double distance(City first, City second) {
        return Math.hypot(first.x - second.x, first.y - second.y);
    }

    private void validateTour(List<Integer> tour) {
        if (tour == null || tour.size() != cities.size()
                || tour.stream().anyMatch(city -> city == null || city < 0 || city >= cities.size())
                || tour.stream().distinct().count() != cities.size()) {
            throw new IllegalArgumentException("TSP solution must be a permutation of all city indices.");
        }
    }

    public static final class City {
        private final double x;
        private final double y;

        public City(double x, double y) {
            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                throw new IllegalArgumentException("City coordinates must be finite.");
            }
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }
        public double getY() { return y; }
    }
}
