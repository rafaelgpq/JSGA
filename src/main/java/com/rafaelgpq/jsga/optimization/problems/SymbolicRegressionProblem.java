package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;

import java.util.Random;

/** Genetic programming over immutable expression trees using mean squared error. */
public final class SymbolicRegressionProblem implements OptimizationProblem<Expression> {

    private final double[] inputs;
    private final double[] targets;
    private final int initialDepth;
    private final int maximumDepth;

    public SymbolicRegressionProblem(double[] inputs, double[] targets, int initialDepth, int maximumDepth) {
        if (inputs == null || targets == null || inputs.length == 0 || inputs.length != targets.length
                || initialDepth < 0 || maximumDepth < Math.max(1, initialDepth)) {
            throw new IllegalArgumentException("Symbolic regression samples and tree depths are invalid.");
        }
        this.inputs = inputs.clone();
        this.targets = targets.clone();
        this.initialDepth = initialDepth;
        this.maximumDepth = maximumDepth;
        for (int i = 0; i < inputs.length; i++) {
            if (!Double.isFinite(inputs[i]) || !Double.isFinite(targets[i])) {
                throw new IllegalArgumentException("Symbolic regression samples must be finite.");
            }
        }
    }

    @Override public String name() { return "Symbolic Regression"; }
    @Override public Expression randomSolution(Random random) {
        return Expression.random(initialDepth, random);
    }

    @Override
    public double evaluate(Expression expression) {
        if (expression == null || expression.depth() > maximumDepth) {
            throw new IllegalArgumentException("Expression is null or exceeds the maximum tree depth.");
        }
        double squaredError = 0.0;
        for (int i = 0; i < inputs.length; i++) {
            double prediction = expression.evaluate(inputs[i]);
            if (!Double.isFinite(prediction)) return Double.MAX_VALUE;
            double error = prediction - targets[i];
            squaredError += error * error;
            if (!Double.isFinite(squaredError)) return Double.MAX_VALUE;
        }
        return squaredError / inputs.length + expression.size() * 1e-8;
    }

    @Override
    public Expression crossover(Expression first, Expression second, Random random) {
        Expression child = first.replaceAt(random.nextInt(first.size()),
                second.nodeAt(random.nextInt(second.size())));
        return child.depth() <= maximumDepth ? child : first;
    }

    @Override
    public Expression mutate(Expression expression, double rate, Random random) {
        if (random.nextDouble() >= rate) return expression;
        int target = random.nextInt(expression.size());
        Expression replacement = Expression.random(Math.min(2, maximumDepth - 1), random);
        Expression child = expression.replaceAt(target, replacement);
        return child.depth() <= maximumDepth ? child : expression;
    }
}
