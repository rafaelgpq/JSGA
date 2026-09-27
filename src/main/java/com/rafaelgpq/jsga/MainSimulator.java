package com.rafaelgpq.jsga;

import com.rafaelgpq.jsga.analysis.ConvergenceChecker;
import com.rafaelgpq.jsga.checkpoint.CheckpointState;
import com.rafaelgpq.jsga.checkpoint.CheckpointWriter;
import com.rafaelgpq.jsga.config.Constants;
import com.rafaelgpq.jsga.config.FlagConfigurator;
import com.rafaelgpq.jsga.core.DoneChecker;
import com.rafaelgpq.jsga.core.Recombination;
import com.rafaelgpq.jsga.diversity.CrowdingStrategy;
import com.rafaelgpq.jsga.diversity.DiversityStrategy;
import com.rafaelgpq.jsga.diversity.IslandModelStrategy;
import com.rafaelgpq.jsga.diversity.RandomImmigrantsStrategy;
import com.rafaelgpq.jsga.checkpoint.RestartManager;
import com.rafaelgpq.jsga.dpe.DPEConfiguration;
import com.rafaelgpq.jsga.dpe.DPEngine;
import com.rafaelgpq.jsga.elitism.ElitismHandler;
import com.rafaelgpq.jsga.evaluation.FitnessEvaluator;
import com.rafaelgpq.jsga.init.ManualInitializer;
import com.rafaelgpq.jsga.init.RandomInitializer;
import com.rafaelgpq.jsga.measure.MeasureEngine;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.problem.ParameterizedProblem;
import com.rafaelgpq.jsga.problem.Problem;
import com.rafaelgpq.jsga.problem.ProblemFactory;
import com.rafaelgpq.jsga.recombine.crossover.*;
import com.rafaelgpq.jsga.recombine.mutation.*;
import com.rafaelgpq.jsga.report.GaOutputWriter;
import com.rafaelgpq.jsga.selection.*;
import com.rafaelgpq.jsga.schema.SchemaAnalyzer;
import com.rafaelgpq.jsga.track.BestSetManager;
import com.rafaelgpq.jsga.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.rafaelgpq.jsga.config.Constants.DEFAULT_MAX_GENERATIONS;

public class MainSimulator {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: MainSimulator [path/to/jsga.properties]");
        }
        String configPath = args.length == 0 ? "jsga.properties" : args[0];
        FlagConfigurator config = new FlagConfigurator(configPath);
        config.validate();
        run(config);
    }

    private static void run(FlagConfigurator config) throws IOException {
        InputPrinter.printConfiguration(config);

        int popSize = config.getInt("population.size", Constants.DEFAULT_POPSIZE);
        int geneLength = config.getInt("gene.length", Constants.DEFAULT_LENGTH);
        double gapSize = config.getDouble("gap.size", Constants.DEFAULT_GAP_SIZE);
        int maxGenerations = config.getInt("max.generations", DEFAULT_MAX_GENERATIONS);
        double mutationRate = config.getDouble("mutation.rate", Constants.DEFAULT_MUTATION_RATE);
        double crossoverRate = config.getDouble("crossover.rate", Constants.DEFAULT_CROSSOVER_RATE);
        boolean elitismEnabled = config.getBoolean("elitism.enabled", Constants.DEFAULT_ELITISM_ENABLED);
        long seed = config.getLong("seed", Constants.DEFAULT_SEED);
        RandomUtils.initialize(seed);

        int fewThreshold = config.getInt("convergence.few.threshold", Constants.DEFAULT_FEW_THRESHOLD);
        boolean convergenceEnabled = config.getBoolean("convergence.enabled", Constants.DEFAULT_CONV_ENABLED);
        int minGenBeforeCheck = config.getInt("convergence.min_generations", Constants.DEFAULT_MINGEN_BEFORE_CHECK);
        double convergenceThreshold = config.getDouble(
                "convergence.threshold", Constants.DEFAULT_CONV_THRESHOLD);
        int maxConsecutiveStagnantGenerations = config.getInt(
                "convergence.maxconv", Constants.DEFAULT_MAXCONV);
        double maxBias = config.getDouble("maxbias", Constants.DEFAULT_MAXBIAS);
        double sigmaFactor = config.getDouble("sigma.factor", Constants.DEFAULT_SIGMA_FACTOR);
        boolean doneTerminationEnabled = config.getBoolean(
                "termination.done.enabled", Constants.DEFAULT_DONE_TERMINATION_ENABLED);
        int stagnationLimit = config.getInt(
                "termination.stagnation.generations", Constants.DEFAULT_STAGNATION_GENERATIONS);
        long trialLimit = config.getLong("termination.trial_limit", Constants.DEFAULT_TRIAL_LIMIT);

        DiversityStrategy diversityStrategy = null;
        String diversityType = config.getString("diversity.strategy", "").toLowerCase(Locale.ROOT);
        if (config.getBoolean("diversity.enabled", false) && !"fitness_sharing".equals(diversityType)) {
            switch (diversityType) {
                case "random_immigrants":
                    diversityStrategy = new RandomImmigrantsStrategy(
                            config.getInt("random_immigrants.count", 0), geneLength);
                    break;
                case "crowding":
                    diversityStrategy = new CrowdingStrategy(geneLength,
                            config.getDouble("crowding.similarity.threshold", 0.3));
                    break;
                case "island_model":
                    diversityStrategy = new IslandModelStrategy(
                            config.getInt("island.count", 2),
                            config.getInt("island.migration.interval", 10),
                            config.getInt("island.migration.size", 1),
                            geneLength, popSize);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported diversity strategy: " + diversityType);
            }
        }

        Population population;
        int firstGeneration = 0;
        long restoredEvaluationCount = -1L;
        if (config.getBoolean("restart.enabled", false)) {
            CheckpointState state = new RestartManager(config.getString("restart.file", ""))
                    .restartState(geneLength, popSize);
            population = state.getPopulation();
            firstGeneration = state.getGeneration();
            restoredEvaluationCount = state.getEvaluationCount();
            if (state.getRandomState() != null) {
                RandomUtils.restore(state.getRandomState());
            }
            if (firstGeneration >= maxGenerations) {
                throw new IllegalArgumentException("Checkpoint generation must be less than max.generations.");
            }
        } else {
            population = PopulationFactory.create(popSize, geneLength);
            String initializationType = config.getString(
                    "initialization.type", Constants.DEFAULT_INITIALIZATION_TYPE).toLowerCase(Locale.ROOT);
            if ("manual".equals(initializationType)) {
                List<String> initialGenes = new ArrayList<>();
                Arrays.stream(config.getString("initialization.genes", "").split(",", -1))
                        .map(String::trim)
                        .forEach(initialGenes::add);
                new ManualInitializer(initialGenes).initialize(population);
            } else {
                new RandomInitializer(RandomUtils.getInstance()).initialize(population);
            }
        }

        Crossover crossover = CrossoverFactory.create(
                config.getString("crossover.type", Constants.DEFAULT_CROSSOVER_TYPE).toLowerCase(Locale.ROOT),
                geneLength);
        Mutation mutation = MutationFactory.create(
                config.getString("mutation.type", Constants.DEFAULT_MUTATION_TYPE).toLowerCase(Locale.ROOT),
                mutationRate,
                geneLength,
                config.getBoolean("mutation.adaptive", false)
        );
        String selectionType = config.getString("selection.type", Constants.DEFAULT_SELECTION_TYPE)
                .toLowerCase(Locale.ROOT);
        double selectionParam = config.getDouble("selection.parameter", Constants.DEFAULT_SELECTION_PARAMETER);
        Selection selection = SelectionFactory.create(selectionType, selectionParam);

        Recombination recombination = new Recombination();
        recombination.configure(crossover, crossoverRate, mutation);

        ElitismHandler elitism = new ElitismHandler(elitismEnabled);
        Problem problem = ProblemFactory.create(config.getString("problem", ProblemFactory.DEFAULT_PROBLEM));
        boolean dpeEnabled = config.getBoolean("dpe.enabled", false);
        ParameterizedProblem parameterizedProblem = problem instanceof ParameterizedProblem
                ? (ParameterizedProblem) problem : null;
        if (dpeEnabled != (parameterizedProblem != null)) {
            throw new IllegalArgumentException(dpeEnabled
                    ? "DPE requires a configured problem that implements ParameterizedProblem."
                    : "ParameterizedProblem objectives require dpe.enabled=true.");
        }
        DPEngine dpeEngine = null;
        boolean grayEncoded = config.getBoolean("dpe.gray", false);
        if (dpeEnabled) {
            DPEConfiguration dpeConfiguration = DPEConfiguration.from(config, geneLength);
            dpeEngine = dpeConfiguration.createEngine(
                    config.getInt("dpe.frequency", 10),
                    config.getInt("dpe.few.threshold", Constants.DEFAULT_FEW_THRESHOLD),
                    popSize,
                    config.getString("dpe.log", "dpe.log"));
        }
        final DPEngine activeDpeEngine = dpeEngine;
        boolean sharingEnabled = config.getBoolean("fitness_sharing.enabled", false);
        double nicheRadius = config.getDouble("fitness_sharing.niche_radius", 0.3);
        FitnessEvaluator evaluator;
        if (parameterizedProblem != null) {
            DPEngine configuredDpeEngine = java.util.Objects.requireNonNull(activeDpeEngine);
            evaluator = new FitnessEvaluator(gene -> parameterizedProblem.evaluateParameters(
                    configuredDpeEngine.decodeParameters(gene, geneLength, grayEncoded)),
                    sharingEnabled, nicheRadius);
        } else {
            evaluator = new FitnessEvaluator(gene -> problem.evaluate(gene, geneLength),
                    sharingEnabled, nicheRadius);
        }

        ConvergenceChecker convergenceChecker = new ConvergenceChecker(
                geneLength, fewThreshold, convergenceEnabled, minGenBeforeCheck,
                convergenceThreshold, maxConsecutiveStagnantGenerations);

        BestSetManager bestSet = new BestSetManager(5, false);
        MeasureEngine measure = new MeasureEngine(-1.0, 0, config.isTraceEnabled(), true, true);
        DoneChecker doneChecker = new DoneChecker(stagnationLimit, trialLimit, false);
        SchemaAnalyzer schemaAnalyzer = null;
        if (config.getBoolean("schema.enabled", false)) {
            String schemaFile = config.getString("schema.file", "");
            schemaAnalyzer = new SchemaAnalyzer(schemaFile,
                    config.getString("schema.output", schemaFile + ".analysis"),
                    config.getDouble("schema.worst_fitness", 0.0));
        }

        Population nextGen = PopulationFactory.create(popSize, geneLength);
        GapHandler gapHandler = new GapHandler();
        int evaluationsOnLoad = evaluator.evaluate(population);
        long totalEvaluations = restoredEvaluationCount >= 0
                ? restoredEvaluationCount + evaluationsOnLoad : evaluationsOnLoad;

        boolean checkpointEnabled = config.getBoolean("checkpoint.enabled", false);
        int checkpointInterval = config.getInt("checkpoint.interval", 1);
        CheckpointWriter checkpointWriter = checkpointEnabled ? new CheckpointWriter() : null;
        String checkpointPath = config.getString("checkpoint.file", "");

        boolean reportEnabled = config.getBoolean("report.enabled", Constants.DEFAULT_REPORT_ENABLED);
        String reportPath = config.getString("report.file", Constants.DEFAULT_REPORT_FILE);
        int reportInterval = config.getInt("report.interval", Constants.DEFAULT_REPORT_INTERVAL);
        if (reportEnabled) {
            GaOutputWriter.initialize(reportPath);
        }

        for (int generation = firstGeneration; generation < maxGenerations; generation++) {
            measure.update(population, totalEvaluations);
            if (schemaAnalyzer != null) {
                schemaAnalyzer.analyze(population, generation);
            }
            int bestIndex = getBestIndex(population);
            double bestFitness = population.get(bestIndex).getFitness();
            bestSet.trySave(population.get(bestIndex), generation, totalEvaluations);
            DebugUtils.printPopulation(population, generation);

            if (checkpointWriter != null && (generation % checkpointInterval == 0
                    || generation + 1 >= maxGenerations)) {
                checkpointWriter.writeCheckpoint(checkpointPath, population, generation,
                        null, RandomUtils.snapshot(), 0, null, true, totalEvaluations);
            }

            if (reportEnabled && (generation % reportInterval == 0 || generation + 1 >= maxGenerations)) {
                convergenceChecker.analyze(population);
                GaOutputWriter.appendGeneration(reportPath, generation, totalEvaluations,
                        convergenceChecker.getLostBits(), convergenceChecker.getConvergedBits(),
                        convergenceChecker.getBias(), measure.getOnline(), measure.getOffline(),
                        bestFitness, measure.getAverage());
            }

            if (doneTerminationEnabled) {
                doneChecker.update(bestFitness, totalEvaluations);
            }
            if (doneTerminationEnabled && doneChecker.isDone()) {
                System.out.println("Done termination condition met at generation " + generation);
                break;
            }

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

            if (generation + 1 >= maxGenerations) {
                break;
            }

            selection.select(population, nextGen);
            int offspringCount = gapHandler.applyGap(population, nextGen, gapSize);
            recombination.apply(nextGen, generation, maxGenerations, offspringCount);
            if (activeDpeEngine != null) {
                activeDpeEngine.apply(nextGen, generation + 1, totalEvaluations);
            }
            totalEvaluations += evaluator.evaluate(nextGen);
            if (diversityStrategy != null) {
                diversityStrategy.apply(nextGen, generation);
                totalEvaluations += evaluator.evaluate(nextGen);
            }
            if (elitism.isEnabled()) {
                elitism.apply(population, nextGen);
            }

            Population temp = population;
            population = nextGen;
            nextGen = temp;
        }

        DebugUtils.printBestSet(bestSet);
    }

    static int getBestIndex(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        if (population.size() == 0) {
            throw new IllegalArgumentException("Cannot find the best individual in an empty population.");
        }
        int bestIndex = 0;
        double bestFitness = population.get(0).getFitness();
        if (!Double.isFinite(bestFitness)) {
            throw new IllegalArgumentException("Population fitness values must be finite.");
        }
        for (int i = 1; i < population.size(); i++) {
            double fitness = population.get(i).getFitness();
            if (!Double.isFinite(fitness)) {
                throw new IllegalArgumentException("Population fitness values must be finite.");
            }
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    static double countOnes(byte[] gene) {
        if (gene == null) {
            throw new IllegalArgumentException("Gene must not be null.");
        }
        int count = 0;
        for (byte b : gene)
            for (int i = 0; i < 8; i++)
                if ((b & (1 << i)) != 0) count++;
        return -count;
    }
}
