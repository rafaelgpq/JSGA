package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** Feature selection using leave-one-out 1-nearest-neighbor error plus a sparsity penalty. */
public final class FeatureSelectionProblem implements OptimizationProblem<List<Boolean>> {

    private final double[][] features;
    private final int[] labels;
    private final int featureCount;

    public FeatureSelectionProblem(double[][] features, int[] labels) {
        if (features == null || labels == null || features.length < 2 || features.length != labels.length
                || features[0] == null || features[0].length == 0) {
            throw new IllegalArgumentException("Feature data and labels are invalid.");
        }
        this.featureCount = features[0].length;
        this.features = new double[features.length][featureCount];
        this.labels = labels.clone();
        for (int row = 0; row < features.length; row++) {
            if (features[row] == null || features[row].length != featureCount) {
                throw new IllegalArgumentException("Every feature row must have the same positive width.");
            }
            this.features[row] = features[row].clone();
            for (double value : this.features[row]) {
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Feature values must be finite.");
            }
        }
    }

    @Override public String name() { return "Feature Selection"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(featureCount, random);
    }

    @Override
    public double evaluate(List<Boolean> selected) {
        OneMaxProblem.requireLength(selected, featureCount);
        int count = 0;
        for (boolean include : selected) if (include) count++;
        if (count == 0) return 1.0;

        int errors = 0;
        for (int row = 0; row < features.length; row++) {
            int nearest = -1;
            double nearestDistance = Double.POSITIVE_INFINITY;
            for (int candidate = 0; candidate < features.length; candidate++) {
                if (candidate == row) continue;
                double distance = 0.0;
                for (int feature = 0; feature < featureCount; feature++) {
                    if (selected.get(feature)) {
                        double delta = features[row][feature] - features[candidate][feature];
                        distance += delta * delta;
                    }
                }
                if (nearest < 0 || distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = candidate;
                }
            }
            if (labels[row] != labels[nearest]) errors++;
        }
        return errors / (double) features.length + 0.001 * count / featureCount;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        OneMaxProblem.requireLength(first, featureCount);
        OneMaxProblem.requireLength(second, featureCount);
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> selected, double rate, Random random) {
        OneMaxProblem.requireLength(selected, featureCount);
        return OptimizationUtils.mutateBits(selected, rate, random);
    }
}
