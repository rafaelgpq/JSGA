package org.example.jsga;

import org.example.jsga.config.Constants;
import org.example.jsga.config.FlagConfigurator;
import org.example.jsga.core.DoneChecker;
import org.example.jsga.core.GeneticOperatorsImpl;
import org.example.jsga.diversity.CrowdingStrategy;
import org.example.jsga.diversity.DiversityStrategy;
import org.example.jsga.diversity.IslandModelStrategy;
import org.example.jsga.diversity.RandomImmigrantsStrategy;
import org.example.jsga.elitism.ElitismHandler;
import org.example.jsga.evaluation.FitnessEvaluator;
import org.example.jsga.init.ManualInitializer;
import org.example.jsga.init.PopulationInitializer;
import org.example.jsga.measure.MeasureEngine;
import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;
import org.example.jsga.operators.CrossoverOperator;
import org.example.jsga.operators.MutationOperator;
import org.example.jsga.selection.SelectionStrategy;
import org.example.jsga.track.BestSetManager;
import org.example.jsga.util.DebugUtils;
import org.example.jsga.util.InputPrinter;
import org.example.jsga.util.LogManager;
import org.example.jsga.util.RandomUtils;

import java.util.List;

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
            boolean elitismEnabled = config.getBoolean("elitism.enabled", true);
            long seed = config.getLong("seed", Constants.DEFAULT_SEED);
            RandomUtils.initialize(seed);

            // Load diversity settings
            boolean diversityEnabled = config.getBoolean("diversity.enabled", false);
            String diversityStrategyName = config.getString("diversity.strategy", "").trim().toLowerCase();
            int randomImmigrantsCount = config.getInt("random_immigrants.count", 0);
            double similarityThreshold = config.getDouble("crowding.similarity.threshold", 0.3);
            int numIslands = config.getInt("island.count", 2);
            int migrationInterval = config.getInt("island.migration.interval", 10);
            int migrationSize = config.getInt("island.migration.size", 1);

            DiversityStrategy diversityStrategy = null;
            // Only initialize diversityStrategy if fitness sharing is not handled by FitnessEvaluator
            if (diversityEnabled && !"fitness_sharing".equals(diversityStrategyName)) {
                switch (diversityStrategyName) {
                    case "random_immigrants":
                        diversityStrategy = new RandomImmigrantsStrategy(randomImmigrantsCount, geneLength);
                        break;
                    case "crowding":
                        diversityStrategy = new CrowdingStrategy(geneLength, similarityThreshold);
                        break;
                    case "island_model":
                        diversityStrategy = new IslandModelStrategy(numIslands, migrationInterval, migrationSize, geneLength, popSize);
                        break;
                    default:
                        System.out.println("[JSGA][Warning] Unknown diversity strategy: " + diversityStrategyName);
                }
            }

            Population population = PopulationFactory.create(popSize, geneLength);

            // Manual gene initialization
            List<String> genes = List.of("11100010", "00011001", "10101010", "00001111", "11110000", "01010101", "01101101", "10010010");
            PopulationInitializer initializer = new ManualInitializer(genes);
            initializer.initialize(population);

            CrossoverOperator crossover = new CrossoverOperator(crossoverRate, popSize, geneLength);
            boolean adaptiveMutation = config.getBoolean("mutation.adaptive", false);
            MutationOperator mutation = new MutationOperator(mutationRate, geneLength, popSize, adaptiveMutation);

            SelectionStrategy selection = new SelectionStrategy(1.0);
            ElitismHandler elitism = new ElitismHandler(elitismEnabled);

            // Initialize FitnessEvaluator with optional sharing
            FitnessEvaluator evaluator = new FitnessEvaluator(
                    gene -> countOnes(gene),
                    config.getBoolean("fitness_sharing.enabled", false),
                    config.getDouble("fitness_sharing.niche_radius", 0.3)
            );

            GeneticOperatorsImpl operators = new GeneticOperatorsImpl(mutation, crossover);
            BestSetManager bestSet = new BestSetManager(5, false);
            MeasureEngine measure = new MeasureEngine(-1.0, 0, true, true, true);
            DoneChecker doneChecker = new DoneChecker(10, maxGenerations, false);

            Population nextGen = PopulationFactory.create(popSize, geneLength);

            // === Main Evolution Loop ===
            for (int generation = 0; generation < maxGenerations; generation++) {
                evaluator.evaluate(population);
                measure.update(population);

                selection.select(population, nextGen);
                operators.crossover(nextGen);
                operators.mutate(nextGen, generation, maxGenerations);

                if (diversityStrategy != null) {
                    diversityStrategy.apply(nextGen, generation);
                }

                if (elitism.isEnabled()) {
                    int bestIndex = getBestIndex(population);
                    bestSet.trySave(population.get(bestIndex), generation, measure.getTrials());
                    System.out.println("[RGP][AFTER][MainSimulator][main()] bestIndex = " + bestIndex
                            + " and 'population.get(bestIndex)' individual: |" + population.get(bestIndex) + "|.");

                    elitism.apply(population, nextGen);
                }

                DebugUtils.printPopulation(population, generation);

                Population temp = population;
                population = nextGen;
                nextGen = temp;
            }

        } catch (Exception e) {
            LogManager.fatal("GA Execution Failed: " + e.getMessage());
        }
    }

    private static int getBestIndex(Population population) {
        int bestIndex = 0;
        double bestFitness = population.get(0).getFitness();
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).getFitness() < bestFitness) {
                bestFitness = population.get(i).getFitness();
                bestIndex = i;
            }
        }
        return bestIndex;
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
