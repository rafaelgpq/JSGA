package org.example.jsga;

import org.example.jsga.config.Constants;
import org.example.jsga.config.FlagConfigurator;
import org.example.jsga.core.DoneChecker;
import org.example.jsga.init.Initializer;
import org.example.jsga.measure.MeasureEngine;
import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;
import org.example.jsga.operators.*;
import org.example.jsga.track.BestSetManager;
import org.example.jsga.util.DebugUtils;
import org.example.jsga.util.InputPrinter;
import org.example.jsga.util.RandomUtils;
import org.example.jsga.track.impl.DummyStats;

/**
 * Simulates the main Genetic Algorithm loop.
 */
public class MainSimulator {

    public static void main(String[] args) {
        try {
            // === Setup Phase ===
            FlagConfigurator config = new FlagConfigurator("jsga.properties");
            InputPrinter.printConfiguration(config);

            int popSize = config.getInt("population.size", Constants.DEFAULT_POPSIZE);
            int geneLength = config.getInt("gene.length", Constants.DEFAULT_LENGTH);
            int maxGenerations = config.getInt("max.generations", 1000);
            double mutationRate = config.getDouble("mutation.rate", Constants.DEFAULT_MUTATION_RATE);
            double crossoverRate = config.getDouble("crossover.rate", Constants.DEFAULT_CROSSOVER_RATE);
            boolean elitism = config.getBoolean("elitism.enabled", true);
            long seed = config.getLong("seed", Constants.DEFAULT_SEED);
            RandomUtils.initialize(seed);

            Population population = PopulationFactory.create(popSize, geneLength);
            Initializer initializer = new Initializer(seed);
            initializer.initialize(population);

            MutationOperator mutation = new MutationOperator(
                    mutationRate, geneLength, popSize);
            CrossoverOperator crossover = new CrossoverOperator(
                    crossoverRate, popSize, geneLength);
            SelectionOperator selection = new SelectionOperator(1.0);
            ElitistOperator elitistOperator = new ElitistOperator();
            EvaluationOperator evaluator = new EvaluationOperator(
                    gene -> countOnes(gene),
                    new DummyStats()
            );

            GeneticOperatorsImpl operators = new GeneticOperatorsImpl(
                    selection, mutation, crossover, elitistOperator, evaluator,
                    true, true, false, 0, 0
            );

            BestSetManager bestSet = new BestSetManager(5, false);
            MeasureEngine measure = new MeasureEngine(-1.0, 0, true, true, true);
            DoneChecker doneChecker = new DoneChecker(10, maxGenerations, false);

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

                DebugUtils.printPopulation(population, generation);

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
}
