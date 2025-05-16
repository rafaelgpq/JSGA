package org.example.jsga;

import org.example.jsga.core.DoneChecker;
import org.example.jsga.init.Initializer;
import org.example.jsga.measure.MeasureEngine;
import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;
import org.example.jsga.operators.*;
import org.example.jsga.track.BestSetManager;
import org.example.jsga.util.ErrorHandler;

/**
 * Simulates the main Genetic Algorithm loop.
 */
public class MainSimulator {

    public static void main(String[] args) {
        try {
            // === Setup Phase ===
            int popSize = 50;
            int geneLength = 32;
            int maxGenerations = 100;

            Population population = PopulationFactory.create(popSize, geneLength);
            Initializer initializer = new Initializer(System.currentTimeMillis());
            initializer.initialize(population);

            MutationOperator mutation = new MutationOperator(0.01, geneLength, popSize);
            CrossoverOperator crossover = new CrossoverOperator(0.7, popSize, geneLength);
            SelectionOperator selection = new SelectionOperator(1.0);
            ElitistOperator elitist = new ElitistOperator();
            EvaluationOperator evaluator = new EvaluationOperator(
                    gene -> countOnes(gene),  // Simple fitness function: maximize 1s
                    new DummyStats()
            );

            GeneticOperatorsImpl operators = new GeneticOperatorsImpl(
                    selection, mutation, crossover, elitist, evaluator,
                    true, true, false, 0, 0
            );

            BestSetManager bestSet = new BestSetManager(5, false);
            MeasureEngine measure = new MeasureEngine(-1.0, 0, true, true, true);
            DoneChecker doneChecker = new DoneChecker(10, 5000, false);

            Population nextGen = PopulationFactory.create(popSize, geneLength);

            // === Main Evolution Loop ===
            for (int generation = 0; generation < maxGenerations; generation++) {
                operators.evaluate(population);
                measure.update(population);

                if (operators.isElitismEnabled()) {
                    bestSet.trySave(population.get(0), generation, measure.getTrials());
                }

                if (doneChecker.isDone()) {
                    System.out.println("Terminating early at generation " + generation);
                    break;
                }

                operators.select(population, nextGen);
                operators.crossover(nextGen);
                operators.mutate(nextGen);

                if (operators.isElitismEnabled()) {
                    operators.elitist(population, nextGen);
                }

                Population temp = population;
                population = nextGen;
                nextGen = temp;
            }

        } catch (Exception e) {
            ErrorHandler.fatal("GA Execution Failed: " + e.getMessage());
        }
    }

    private static double countOnes(byte[] gene) {
        int count = 0;
        for (byte b : gene) {
            for (int i = 0; i < 8; i++) {
                if ((b & (1 << i)) != 0) count++;
            }
        }
        return -count; // minimize negative = maximize ones
    }

    private static class DummyStats implements StatisticsTracker {
        public void incrementTrials() {}
        public void updateBest(double f) {}
        public double getBest() { return 0; }
        public void accumulateOnSum(double f) {}
        public void accumulateOffSum(double b) {}
        public boolean shouldSaveBest() { return false; }
        public void saveBest(Individual i) {}
        public boolean shouldDump() { return false; }
        public void dumpCheckpoint() {}
    }
}
