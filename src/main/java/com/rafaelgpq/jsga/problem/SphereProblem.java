package com.rafaelgpq.jsga.problem;

/**
 * Continuous minimization benchmark: sum of squared parameter values.
 */
public class SphereProblem implements ParameterizedProblem {

    @Override
    public double evaluateParameters(double[] parameters) {
        if (parameters == null || parameters.length == 0) {
            throw new IllegalArgumentException("At least one parameter is required.");
        }
        double sum = 0.0;
        for (double parameter : parameters) {
            if (!Double.isFinite(parameter)) {
                throw new IllegalArgumentException("Parameters must be finite.");
            }
            sum += parameter * parameter;
        }
        if (!Double.isFinite(sum)) {
            throw new IllegalArgumentException("Sphere objective overflowed.");
        }
        return sum;
    }
}
