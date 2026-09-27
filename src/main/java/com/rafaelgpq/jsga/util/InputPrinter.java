package com.rafaelgpq.jsga.util;

import com.rafaelgpq.jsga.config.Constants;
import com.rafaelgpq.jsga.config.FlagConfigurator;
import com.rafaelgpq.jsga.problem.ProblemFactory;

public class InputPrinter {

    public static void printConfiguration(FlagConfigurator config) {
        System.out.println("\n====== GA CONFIGURATION ======");
        System.out.printf("      Problem = %s%n",
                config.getString("problem", ProblemFactory.DEFAULT_PROBLEM));
        System.out.printf("  Population Size = %d%n", config.getInt("population.size", Constants.DEFAULT_POPSIZE));
        System.out.printf("     Gene Length = %d%n", config.getInt("gene.length", Constants.DEFAULT_LENGTH));
        System.out.printf(" Initialization  = %s%n",
                config.getString("initialization.type", Constants.DEFAULT_INITIALIZATION_TYPE));
        System.out.printf(" Maximum Gens    = %d%n",
                config.getInt("max.generations", Constants.DEFAULT_MAX_GENERATIONS));
        System.out.printf("   Selection     = %s%n",
                config.getString("selection.type", Constants.DEFAULT_SELECTION_TYPE));
        System.out.printf("   Gap Size      = %.4f%n",
                config.getDouble("gap.size", Constants.DEFAULT_GAP_SIZE));
        System.out.printf(" Crossover       = %s (rate %.4f)%n",
                config.getString("crossover.type", Constants.DEFAULT_CROSSOVER_TYPE),
                config.getDouble("crossover.rate", Constants.DEFAULT_CROSSOVER_RATE));
        System.out.printf(" Mutation        = %s (rate %.4f)%n",
                config.getString("mutation.type", Constants.DEFAULT_MUTATION_TYPE),
                config.getDouble("mutation.rate", Constants.DEFAULT_MUTATION_RATE));
        System.out.printf(" Elitism         = %s%n",
                config.getBoolean("elitism.enabled", Constants.DEFAULT_ELITISM_ENABLED));
        System.out.printf(" Random Seed     = %d%n", config.getLong("seed", Constants.DEFAULT_SEED));
        System.out.printf(" Trial Limit     = %d%n",
                config.getLong("termination.trial_limit", Constants.DEFAULT_TRIAL_LIMIT));
        System.out.printf(" Stagnation Limit = %d generations%n",
                config.getInt("termination.stagnation.generations", Constants.DEFAULT_STAGNATION_GENERATIONS));
        System.out.printf(" DPE Enabled     = %s%n", config.getBoolean("dpe.enabled", false));
    }
}
