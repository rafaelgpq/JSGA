package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

final class SelectionWeights {

    private static final double MINIMUM_WEIGHT = 1.0e-12;

    private SelectionWeights() {}

    static double[] forMinimization(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Source population must not be null.");
        }
        if (population.size() == 0) {
            throw new IllegalArgumentException("Source population must not be empty.");
        }

        double scale = 0.0;
        for (Individual individual : population.getAll()) {
            if (individual == null || !Double.isFinite(individual.getFitness())) {
                throw new IllegalArgumentException("Selection requires finite fitness values.");
            }
            scale = Math.max(scale, Math.abs(individual.getFitness()));
        }

        double[] scaledFitness = new double[population.size()];
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < population.size(); i++) {
            double value = scale == 0.0 ? 0.0 : population.get(i).getFitness() / scale;
            scaledFitness[i] = value;
            minimum = Math.min(minimum, value);
            maximum = Math.max(maximum, value);
        }

        double[] weights = new double[population.size()];
        double sum = 0.0;
        double range = maximum - minimum;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = range == 0.0
                    ? 1.0
                    : (maximum - scaledFitness[i]) / range + MINIMUM_WEIGHT;
            sum += weights[i];
        }
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }
        return weights;
    }
}
