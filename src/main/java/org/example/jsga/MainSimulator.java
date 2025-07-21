package org.example.jsga;

import org.example.jsga.analysis.ConvergenceChecker;
import org.example.jsga.config.Constants;
import org.example.jsga.config.FlagConfigurator;
import org.example.jsga.core.DoneChecker;
import org.example.jsga.core.Recombination;
import org.example.jsga.diversity.CrowdingStrategy;
import org.example.jsga.diversity.DiversityStrategy;
import org.example.jsga.diversity.IslandModelStrategy;
import org.example.jsga.diversity.RandomImmigrantsStrategy;
import org.example.jsga.elitism.ElitismHandler;
import org.example.jsga.evaluation.FitnessEvaluator;
import org.example.jsga.init.RandomInitializer;
import org.example.jsga.measure.MeasureEngine;
import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;
import org.example.jsga.recombine.crossover.*;
import org.example.jsga.recombine.mutation.*;
import org.example.jsga.selection.*;
import org.example.jsga.track.BestSetManager;
import org.example.jsga.util.*;

import static org.example.jsga.config.Constants.DEFAULT_MAX_GENERATIONS;

public class MainSimulator {
    public static void main(String[] args) {
        try {
            FlagConfigurator config = new FlagConfigurator("jsga.properties");
            InputPrinter.printConfiguration(config);

            int popSize = config.getInt("population.size", Constants.DEFAULT_POPSIZE);
            int geneLength = config.getInt("gene.length", Constants.DEFAULT_LENGTH);
            int maxGenerations = config.getInt("max.generations", DEFAULT_MAX_GENERATIONS);
            double mutationRate = config.getDouble("mutation.rate", Constants.DEFAULT_MUTATION_RATE);
            double crossoverRate = config.getDouble("crossover.rate", Constants.DEFAULT_CROSSOVER_RATE);
            boolean elitismEnabled = config.getBoolean("elitism.enabled", Constants.DEFAULT_ELITISM_ENABLED);
            long seed = config.getLong("seed", Constants.DEFAULT_SEED);
            RandomUtils.initialize(seed);

            // Dynamic thresholds
            int fewThreshold = config.getInt("convergence.few.threshold", Constants.DEFAULT_FEW_THRESHOLD);
            boolean convergenceEnabled = config.getBoolean("convergence.enabled", Constants.DEFAULT_CONV_ENABLED);
            int minGenBeforeCheck = config.getInt("convergence.min_generations", Constants.DEFAULT_MINGEN_BEFORE_CHECK);
            double convThreshold = config.getDouble("convergence.threshold", Constants.DEFAULT_CONV_THRESHOLD);
            int maxConvGen = config.getInt("convergence.maxconv", Constants.DEFAULT_MAXCONV);
            double maxBias = config.getDouble("maxbias", Constants.DEFAULT_MAXBIAS);
            double sigmaFactor = config.getDouble("sigma.factor", Constants.DEFAULT_SIGMA_FACTOR);

            DiversityStrategy diversityStrategy = null;
            String diversityType = config.getString("diversity.strategy", "").trim().toLowerCase();
            if (config.getBoolean("diversity.enabled", false) && !"fitness_sharing".equals(diversityType)) {
                switch (diversityType) {
                    case "random_immigrants":
                        diversityStrategy = new RandomImmigrantsStrategy(config.getInt("random_immigrants.count", 0), geneLength);
                        break;
                    case "crowding":
                        diversityStrategy = new CrowdingStrategy(geneLength, config.getDouble("crowding.similarity.threshold", 0.3));
                        break;
                    case "island_model":
                        diversityStrategy = new IslandModelStrategy(
                                config.getInt("island.count", 2),
                                config.getInt("island.migration.interval", 10),
                                config.getInt("island.migration.size", 1),
                                geneLength, popSize);
                        break;
                }
            }

//            Population population = PopulationFactory.create(popSize, geneLength);
//            new ManualInitializer(List.of("11100010", "00011001", "10101010", "00001111", "11110000", "01010101", "01101101", "10010010")).initialize(population);

            Population population = PopulationFactory.create(popSize, geneLength);
            RandomInitializer initializer = new RandomInitializer(seed);
            initializer.initialize(population);

            Crossover crossover = CrossoverFactory.create(config.getString("crossover.type", "onepoint"), geneLength);
            Mutation mutation = MutationFactory.create(
                    config.getString("mutation.type", "bitflip"),
                    mutationRate,
                    geneLength,
                    popSize,
                    config.getBoolean("mutation.adaptive", false)
            );
            String selectionType = config.getString("selection.type", "tournament");
            double selectionParam = config.getDouble("selection.parameter", 1.0);
            Selection selection = SelectionFactory.create(selectionType, selectionParam);

            Recombination recombination = new Recombination();
            recombination.configure(crossover, crossoverRate, mutation);

            ElitismHandler elitism = new ElitismHandler(elitismEnabled);
            FitnessEvaluator evaluator = new FitnessEvaluator(MainSimulator::countOnes,
                    config.getBoolean("fitness_sharing.enabled", false),
                    config.getDouble("fitness_sharing.niche_radius", 0.3));

            ConvergenceChecker convergenceChecker = new ConvergenceChecker(
                    geneLength, fewThreshold, convergenceEnabled, minGenBeforeCheck);


            BestSetManager bestSet = new BestSetManager(5, false);
            MeasureEngine measure = new MeasureEngine(-1.0, 0, true, true, true);
            DoneChecker doneChecker = new DoneChecker(10, maxGenerations, false);

            Population nextGen = PopulationFactory.create(popSize, geneLength);
            evaluator.evaluate(population);

            for (int generation = 0; generation < maxGenerations; generation++) {
                measure.update(population);
                int bestIndex = getBestIndex(population);
                double bestFitness = population.get(bestIndex).getFitness();

                // PLEASE REVIEW THIS CHATGPT ALSO TO ADD IT AS PART OF THE convergenceEnabled????
                // BEGIN - CHATGPT REVIEW
                doneChecker.update(bestFitness, measure.getTrials());
                if (doneChecker.isDone()) {
                    System.out.println("Termination condition met at generation " + generation);
                    break;
                }
                // END - CHATGPT REVIEW

                selection.select(population, nextGen);
                recombination.apply(nextGen, generation, maxGenerations);
                evaluator.evaluate(nextGen);
                if (diversityStrategy != null) diversityStrategy.apply(nextGen, generation);

                if (elitism.isEnabled()) {
                    bestIndex = getBestIndex(population);
                    bestSet.trySave(population.get(bestIndex), generation, measure.getTrials());
                    elitism.apply(population, nextGen);
                }

                DebugUtils.printPopulation(population, generation);

                if (convergenceEnabled) {
                    if (convergenceChecker.hasConverged(population, generation)) {
                        System.out.println("Convergence detected. Stopping early at generation " + generation);
                        break;
                    }

                    double bias = MeasureUtils.calculateGeneticBias(population);
                    if (bias > maxBias) {
                        System.out.println("Population bias too high (" + bias + "). Stopping early.");
                        break;
                    }

                    double sigma = MeasureUtils.calculateSigma(population);
                    if (sigma < sigmaFactor) {
                        System.out.println("Population diversity too low (sigma = " + sigma + "). Stopping early.");
                        break;
                    }
                }

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
            if (population.get(i).getFitness() > bestFitness) {
                bestFitness = population.get(i).getFitness();
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static double countOnes(byte[] gene) {
        int count = 0;
        for (byte b : gene)
            for (int i = 0; i < 8; i++)
                if ((b & (1 << i)) != 0) count++;
        return -count;
    }
}
