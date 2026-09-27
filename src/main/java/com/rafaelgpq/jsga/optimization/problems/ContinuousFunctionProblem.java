package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Bounded real-valued Sphere, Rastrigin, or Rosenbrock benchmark. */
public final class ContinuousFunctionProblem implements OptimizationProblem<List<Double>> {

    public enum Function { SPHERE, RASTRIGIN, ROSENBROCK }

    private final int dimensions;
    private final double minimum;
    private final double maximum;
    private final Function function;

    public ContinuousFunctionProblem(int dimensions, double minimum, double maximum, Function function) {
        if (dimensions <= 0 || !Double.isFinite(minimum) || !Double.isFinite(maximum)
                || minimum >= maximum || function == null) {
            throw new IllegalArgumentException("Continuous optimization settings are invalid.");
        }
        this.dimensions = dimensions;
        this.minimum = minimum;
        this.maximum = maximum;
        this.function = function;
    }

    @Override public String name() { return "Continuous " + function; }
    @Override public List<Double> randomSolution(Random random) {
        return OptimizationUtils.randomValues(dimensions, minimum, maximum, random);
    }

    @Override
    public double evaluate(List<Double> values) {
        validate(values);
        double score = 0.0;
        switch (function) {
            case SPHERE:
                for (double value : values) score += value * value;
                break;
            case RASTRIGIN:
                score = 10.0 * dimensions;
                for (double value : values) score += value * value - 10.0 * Math.cos(2.0 * Math.PI * value);
                break;
            case ROSENBROCK:
                for (int i = 0; i + 1 < values.size(); i++) {
                    double first = values.get(i);
                    double second = values.get(i + 1);
                    score += 100.0 * Math.pow(second - first * first, 2) + Math.pow(1.0 - first, 2);
                }
                break;
            default:
                throw new IllegalStateException("Unsupported continuous function: " + function);
        }
        return score;
    }

    @Override
    public List<Double> crossover(List<Double> first, List<Double> second, Random random) {
        validate(first);
        validate(second);
        List<Double> child = new ArrayList<>(dimensions);
        for (int i = 0; i < dimensions; i++) {
            double alpha = random.nextDouble();
            child.add(clamp(alpha * first.get(i) + (1.0 - alpha) * second.get(i)));
        }
        return List.copyOf(child);
    }

    @Override
    public List<Double> mutate(List<Double> values, double rate, Random random) {
        validate(values);
        List<Double> child = new ArrayList<>(dimensions);
        for (double value : values) {
            if (random.nextDouble() < rate) {
                value = clamp(value + random.nextGaussian() * (maximum - minimum) * 0.1);
            }
            child.add(value);
        }
        return List.copyOf(child);
    }

    @Override
    public boolean isSolved(double fitness) {
        return function == Function.SPHERE && fitness <= 1e-8;
    }

    private void validate(List<Double> values) {
        if (values == null || values.size() != dimensions) {
            throw new IllegalArgumentException("Continuous solution must contain " + dimensions + " values.");
        }
        for (Double value : values) {
            if (value == null || !Double.isFinite(value) || value < minimum || value > maximum) {
                throw new IllegalArgumentException("Continuous solution values must be finite and within bounds.");
            }
        }
    }

    private double clamp(double value) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
