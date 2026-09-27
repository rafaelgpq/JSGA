package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** Capacitated vehicle routing with a fixed depot and greedy capacity-feasible route splitting. */
public final class VehicleRoutingProblem implements OptimizationProblem<List<Integer>> {

    private final List<TspProblem.City> locations;
    private final int[] demands;
    private final int capacity;
    private final int maxVehicles;
    private final double infeasiblePenalty;

    public VehicleRoutingProblem(List<TspProblem.City> locations, int[] demands, int capacity, int maxVehicles) {
        if (locations == null || demands == null || locations.size() < 2
                || demands.length != locations.size() - 1 || capacity <= 0 || maxVehicles <= 0) {
            throw new IllegalArgumentException("VRP locations, demands, capacity, and fleet size are invalid.");
        }
        if (locations.stream().anyMatch(location -> location == null)) {
            throw new IllegalArgumentException("VRP locations must not be null.");
        }
        this.locations = List.copyOf(locations);
        this.demands = demands.clone();
        this.capacity = capacity;
        this.maxVehicles = maxVehicles;
        double maxDistance = 0.0;
        for (int i = 0; i < this.demands.length; i++) {
            if (this.demands[i] <= 0 || this.demands[i] > capacity) {
                throw new IllegalArgumentException("Customer demand must be positive and fit vehicle capacity.");
            }
        }
        for (int i = 0; i < locations.size(); i++) {
            for (int j = i + 1; j < locations.size(); j++) {
                maxDistance = Math.max(maxDistance, distance(locations.get(i), locations.get(j)));
            }
        }
        this.infeasiblePenalty = (locations.size() + maxVehicles + 1.0) * maxDistance + 1.0;
    }

    @Override public String name() { return "Capacitated Vehicle Routing"; }
    @Override public List<Integer> randomSolution(Random random) {
        return OptimizationUtils.randomPermutation(demands.length, random);
    }

    @Override
    public double evaluate(List<Integer> routeOrder) {
        validate(routeOrder);
        double distance = 0.0;
        int vehicles = 1;
        int load = 0;
        int previousLocation = 0;
        for (int customerId : routeOrder) {
            int location = customerId + 1;
            int demand = demands[customerId];
            if (load + demand > capacity) {
                distance += distance(locations.get(previousLocation), locations.get(0));
                vehicles++;
                load = 0;
                previousLocation = 0;
            }
            distance += distance(locations.get(previousLocation), locations.get(location));
            previousLocation = location;
            load += demand;
        }
        distance += distance(locations.get(previousLocation), locations.get(0));
        return distance + Math.max(0, vehicles - maxVehicles) * infeasiblePenalty;
    }

    @Override public List<Integer> crossover(List<Integer> first, List<Integer> second, Random random) {
        validate(first);
        validate(second);
        return OptimizationUtils.orderedCrossover(first, second, random);
    }

    @Override public List<Integer> mutate(List<Integer> solution, double rate, Random random) {
        validate(solution);
        return OptimizationUtils.swapMutation(solution, rate, random);
    }

    private void validate(List<Integer> order) {
        if (order == null || order.size() != demands.length
                || order.stream().anyMatch(id -> id == null || id < 0 || id >= demands.length)
                || order.stream().distinct().count() != demands.length) {
            throw new IllegalArgumentException("VRP solution must visit every customer exactly once.");
        }
    }

    private static double distance(TspProblem.City first, TspProblem.City second) {
        return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
    }
}
