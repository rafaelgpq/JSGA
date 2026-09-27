package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Optimizes a one-hidden-layer dense network's weights by minimizing training MSE. */
public final class NeuralNetworkWeightProblem implements OptimizationProblem<List<Double>> {

    private final double[][] inputs;
    private final double[][] targets;
    private final int inputCount;
    private final int hiddenCount;
    private final int outputCount;
    private final int weightCount;

    public NeuralNetworkWeightProblem(double[][] inputs, double[][] targets, int hiddenCount) {
        if (inputs == null || targets == null || inputs.length == 0 || inputs.length != targets.length
                || inputs[0] == null || inputs[0].length == 0 || targets[0] == null
                || targets[0].length == 0 || hiddenCount <= 0) {
            throw new IllegalArgumentException("Neural-network training data and topology are invalid.");
        }
        this.inputCount = inputs[0].length;
        this.outputCount = targets[0].length;
        this.hiddenCount = hiddenCount;
        this.inputs = new double[inputs.length][inputCount];
        this.targets = new double[targets.length][outputCount];
        for (int row = 0; row < inputs.length; row++) {
            if (inputs[row] == null || inputs[row].length != inputCount
                    || targets[row] == null || targets[row].length != outputCount) {
                throw new IllegalArgumentException("Neural-network training rows must have consistent dimensions.");
            }
            this.inputs[row] = inputs[row].clone();
            this.targets[row] = targets[row].clone();
            for (double value : this.inputs[row]) {
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Network inputs must be finite.");
            }
            for (double value : this.targets[row]) {
                if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
                    throw new IllegalArgumentException("Network targets must be finite values in [0, 1].");
                }
            }
        }
        long weightCount = (long) inputCount * hiddenCount + hiddenCount
                + (long) hiddenCount * outputCount + outputCount;
        if (weightCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Neural network topology contains too many weights.");
        }
        this.weightCount = (int) weightCount;
    }

    @Override public String name() { return "Neural Network Weight Optimization"; }

    @Override
    public List<Double> randomSolution(Random random) {
        List<Double> weights = new ArrayList<>(weightCount);
        for (int i = 0; i < weightCount; i++) weights.add(random.nextDouble() * 2.0 - 1.0);
        return List.copyOf(weights);
    }

    @Override
    public double evaluate(List<Double> weights) {
        validate(weights);
        int hiddenBiasOffset = inputCount * hiddenCount;
        int outputWeightOffset = hiddenBiasOffset + hiddenCount;
        int outputBiasOffset = outputWeightOffset + hiddenCount * outputCount;
        double totalSquaredError = 0.0;
        double[] hidden = new double[hiddenCount];

        for (int row = 0; row < inputs.length; row++) {
            for (int h = 0; h < hiddenCount; h++) {
                double activation = weights.get(hiddenBiasOffset + h);
                for (int input = 0; input < inputCount; input++) {
                    activation += inputs[row][input] * weights.get(input * hiddenCount + h);
                }
                hidden[h] = Math.tanh(activation);
            }
            for (int output = 0; output < outputCount; output++) {
                double activation = weights.get(outputBiasOffset + output);
                for (int h = 0; h < hiddenCount; h++) {
                    activation += hidden[h] * weights.get(outputWeightOffset + h * outputCount + output);
                }
                double prediction = 1.0 / (1.0 + Math.exp(-activation));
                double error = prediction - targets[row][output];
                totalSquaredError += error * error;
            }
        }
        return totalSquaredError / (inputs.length * (double) outputCount);
    }

    @Override
    public List<Double> crossover(List<Double> first, List<Double> second, Random random) {
        validate(first);
        validate(second);
        List<Double> child = new ArrayList<>(weightCount);
        for (int i = 0; i < weightCount; i++) {
            double alpha = random.nextDouble();
            child.add(alpha * first.get(i) + (1.0 - alpha) * second.get(i));
        }
        return List.copyOf(child);
    }

    @Override
    public List<Double> mutate(List<Double> weights, double rate, Random random) {
        validate(weights);
        List<Double> mutant = new ArrayList<>(weightCount);
        for (double weight : weights) {
            if (random.nextDouble() < rate) weight += random.nextGaussian() * 0.25;
            mutant.add(Math.max(-5.0, Math.min(5.0, weight)));
        }
        return List.copyOf(mutant);
    }

    private void validate(List<Double> weights) {
        if (weights == null || weights.size() != weightCount) {
            throw new IllegalArgumentException("Network genome must contain " + weightCount + " weights.");
        }
        for (Double weight : weights) {
            if (weight == null || !Double.isFinite(weight)) {
                throw new IllegalArgumentException("Network weights must be finite.");
            }
        }
    }
}
