package org.example.jsga.operators;

import org.example.jsga.model.Population;

/**
 * Concrete implementation of the GeneticOperators interface.
 */
public class GeneticOperatorsImpl implements GeneticOperators {

    private final SelectionOperator selection;
    private final MutationOperator mutation;
    private final CrossoverOperator crossover;
    private final ElitistOperator elitist;
    private final EvaluationOperator evaluator;

    private final boolean elitismEnabled;
    private final boolean evaluateAll;
    private final boolean saveBest;
    private final int dpeFrequency;

    private final int bestIndex;

    public GeneticOperatorsImpl(
            SelectionOperator selection,
            MutationOperator mutation,
            CrossoverOperator crossover,
            ElitistOperator elitist,
            EvaluationOperator evaluator,
            boolean elitismEnabled,
            boolean evaluateAll,
            boolean saveBest,
            int dpeFrequency,
            int bestIndex
    ) {
        this.selection = selection;
        this.mutation = mutation;
        this.crossover = crossover;
        this.elitist = elitist;
        this.evaluator = evaluator;
        this.elitismEnabled = elitismEnabled;
        this.evaluateAll = evaluateAll;
        this.saveBest = saveBest;
        this.dpeFrequency = dpeFrequency;
        this.bestIndex = bestIndex;
    }

    @Override
    public void initialize(Population population) {
        // Assume population is already initialized or externally handled.
    }

    @Override
    public void select(Population from, Population to) {
        selection.select(from, to);
    }

    @Override
    public void mutate(Population population) {
        mutation.mutate(population);
    }

    @Override
    public void crossover(Population population) {
        crossover.performCrossover(population);
    }

    @Override
    public void elitist(Population oldPop, Population newPop) {
        elitist.apply(oldPop, newPop, bestIndex);
    }

    @Override
    public void evaluate(Population population) {
        evaluator.evaluate(population);
    }

    @Override
    public void restart() {
        // Placeholder: add logic for checkpoint-based restart if needed.
    }

    @Override
    public void performDpe(Population population) {
        // Placeholder: DPE logic not yet implemented.
    }

    @Override
    public void printBest(Population population) {
        // Placeholder: can implement logging or export of best individual.
    }

    @Override
    public boolean isElitismEnabled() {
        return elitismEnabled;
    }

    @Override
    public boolean isEvaluateAll() {
        return evaluateAll;
    }

    @Override
    public boolean shouldSaveBest() {
        return saveBest;
    }

    @Override
    public int getDpeFrequency() {
        return dpeFrequency;
    }
}

