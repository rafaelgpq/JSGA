package com.rafaelgpq.jsga.optimization.problems;

import java.util.Random;

/** Immutable real-valued expression tree over x and bounded constants. */
public final class Expression {

    private enum Operator { VARIABLE, CONSTANT, ADD, SUBTRACT, MULTIPLY, DIVIDE, SIN, COS }

    private final Operator operator;
    private final double constant;
    private final Expression left;
    private final Expression right;

    private Expression(Operator operator, double constant, Expression left, Expression right) {
        this.operator = operator;
        this.constant = constant;
        this.left = left;
        this.right = right;
    }

    public static Expression variable() {
        return new Expression(Operator.VARIABLE, 0.0, null, null);
    }

    public static Expression constant(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Expression constant must be finite.");
        return new Expression(Operator.CONSTANT, value, null, null);
    }

    public static Expression random(int maximumDepth, Random random) {
        if (maximumDepth < 0 || maximumDepth > 32 || random == null) {
            throw new IllegalArgumentException("Expression depth must be non-negative and RNG must be present.");
        }
        if (maximumDepth == 0 || random.nextDouble() < 0.3) {
            return random.nextBoolean() ? variable() : constant(random.nextDouble() * 6.0 - 3.0);
        }
        switch (random.nextInt(6)) {
            case 0: return new Expression(Operator.ADD, 0, random(maximumDepth - 1, random),
                    random(maximumDepth - 1, random));
            case 1: return new Expression(Operator.SUBTRACT, 0, random(maximumDepth - 1, random),
                    random(maximumDepth - 1, random));
            case 2: return new Expression(Operator.MULTIPLY, 0, random(maximumDepth - 1, random),
                    random(maximumDepth - 1, random));
            case 3: return new Expression(Operator.DIVIDE, 0, random(maximumDepth - 1, random),
                    random(maximumDepth - 1, random));
            case 4: return new Expression(Operator.SIN, 0, random(maximumDepth - 1, random), null);
            default: return new Expression(Operator.COS, 0, random(maximumDepth - 1, random), null);
        }
    }

    public double evaluate(double x) {
        switch (operator) {
            case VARIABLE: return x;
            case CONSTANT: return constant;
            case ADD: return left.evaluate(x) + right.evaluate(x);
            case SUBTRACT: return left.evaluate(x) - right.evaluate(x);
            case MULTIPLY: return left.evaluate(x) * right.evaluate(x);
            case DIVIDE:
                double divisor = right.evaluate(x);
                return Math.abs(divisor) < 1e-8 ? left.evaluate(x) : left.evaluate(x) / divisor;
            case SIN: return Math.sin(left.evaluate(x));
            case COS: return Math.cos(left.evaluate(x));
            default: throw new IllegalStateException("Unsupported expression operator.");
        }
    }

    public int size() {
        return 1 + (left == null ? 0 : left.size()) + (right == null ? 0 : right.size());
    }

    public int depth() {
        return 1 + Math.max(left == null ? 0 : left.depth(), right == null ? 0 : right.depth());
    }

    public Expression nodeAt(int index) {
        if (index < 0 || index >= size()) throw new IllegalArgumentException("Expression node index is out of range.");
        if (index == 0) return this;
        int leftSize = left == null ? 0 : left.size();
        return index <= leftSize ? left.nodeAt(index - 1) : right.nodeAt(index - leftSize - 1);
    }

    public Expression replaceAt(int index, Expression replacement) {
        if (replacement == null || index < 0 || index >= size()) {
            throw new IllegalArgumentException("Expression replacement target is invalid.");
        }
        if (index == 0) return replacement;
        int leftSize = left == null ? 0 : left.size();
        if (index <= leftSize) {
            return new Expression(operator, constant, left.replaceAt(index - 1, replacement), right);
        }
        return new Expression(operator, constant, left, right.replaceAt(index - leftSize - 1, replacement));
    }

    @Override
    public String toString() {
        switch (operator) {
            case VARIABLE: return "x";
            case CONSTANT: return String.format(java.util.Locale.ROOT, "%.3f", constant);
            case SIN: return "sin(" + left + ")";
            case COS: return "cos(" + left + ")";
            case ADD: return "(" + left + " + " + right + ")";
            case SUBTRACT: return "(" + left + " - " + right + ")";
            case MULTIPLY: return "(" + left + " * " + right + ")";
            case DIVIDE: return "(" + left + " / " + right + ")";
            default: throw new IllegalStateException("Unsupported expression operator.");
        }
    }
}
