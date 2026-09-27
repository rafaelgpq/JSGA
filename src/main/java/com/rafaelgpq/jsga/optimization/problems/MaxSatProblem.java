package com.rafaelgpq.jsga.optimization.problems;

import com.rafaelgpq.jsga.optimization.OptimizationProblem;
import com.rafaelgpq.jsga.optimization.OptimizationUtils;

import java.util.List;
import java.util.Random;

/** MAX-SAT represented by Boolean variable assignments; each clause is a list of signed literals. */
public final class MaxSatProblem implements OptimizationProblem<List<Boolean>> {

    private final int variables;
    private final List<Clause> clauses;

    public MaxSatProblem(int variables, List<Clause> clauses) {
        if (variables <= 0 || clauses == null || clauses.isEmpty()) {
            throw new IllegalArgumentException("MAX-SAT requires variables and at least one clause.");
        }
        for (Clause clause : clauses) {
            if (clause == null || clause.literals.length == 0) {
                throw new IllegalArgumentException("MAX-SAT clauses must not be empty.");
            }
            for (int literal : clause.literals) {
                if (literal == 0 || Math.abs((long) literal) > variables) {
                    throw new IllegalArgumentException("MAX-SAT literal references an invalid variable.");
                }
            }
        }
        this.variables = variables;
        this.clauses = List.copyOf(clauses);
    }

    @Override public String name() { return "MAX-SAT"; }
    @Override public List<Boolean> randomSolution(Random random) {
        return OptimizationUtils.randomBits(variables, random);
    }

    @Override
    public double evaluate(List<Boolean> assignment) {
        OneMaxProblem.requireLength(assignment, variables);
        int satisfied = 0;
        for (Clause clause : clauses) {
            for (int literal : clause.literals) {
                boolean value = assignment.get(Math.abs(literal) - 1);
                if (literal < 0) value = !value;
                if (value) {
                    satisfied++;
                    break;
                }
            }
        }
        return -satisfied;
    }

    @Override public List<Boolean> crossover(List<Boolean> first, List<Boolean> second, Random random) {
        OneMaxProblem.requireLength(first, variables);
        OneMaxProblem.requireLength(second, variables);
        return OptimizationUtils.uniformBits(first, second, random);
    }

    @Override public List<Boolean> mutate(List<Boolean> solution, double rate, Random random) {
        OneMaxProblem.requireLength(solution, variables);
        return OptimizationUtils.mutateBits(solution, rate, random);
    }

    @Override public boolean isSolved(double fitness) { return fitness == -clauses.size(); }

    public static final class Clause {
        private final int[] literals;

        public Clause(int... literals) {
            if (literals == null) throw new IllegalArgumentException("Clause literals must not be null.");
            this.literals = literals.clone();
        }
    }
}
