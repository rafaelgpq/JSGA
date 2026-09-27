package com.rafaelgpq.jsga.config;

import com.rafaelgpq.jsga.dpe.DPEConfiguration;
import com.rafaelgpq.jsga.problem.ProblemFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.function.IntPredicate;
import java.util.function.DoublePredicate;

/**
 * Loads and manages runtime flags and parameters from jsga.properties.
 */
public class FlagConfigurator {

    private final Properties props;

    public FlagConfigurator(String configPath) throws IOException {
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration path must not be empty.");
        }
        props = new Properties();
        try (FileInputStream in = new FileInputStream(configPath)) {
            props.load(in);
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            throw invalidValue(key, e);
        }
    }

    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(getString(key, Long.toString(defaultValue)));
        } catch (NumberFormatException e) {
            throw invalidValue(key, e);
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            double value = Double.parseDouble(getString(key, Double.toString(defaultValue)));
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Invalid value for '" + key + "': expected a finite number.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw invalidValue(key, e);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, Boolean.toString(defaultValue)).toLowerCase(Locale.ROOT);
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;
        throw new IllegalArgumentException("Invalid value for '" + key + "': expected true or false.");
    }

    public String getString(String key, String defaultValue) {
        return props.getProperty(key, defaultValue).trim();
    }

    /**
     * Validates the runtime settings consumed by the simulator before any run state is created.
     */
    public void validate() {
        requireIntRange("population.size", getInt("population.size", Constants.DEFAULT_POPSIZE), value -> value > 0,
                "must be greater than zero");
        int geneLength = getInt("gene.length", Constants.DEFAULT_LENGTH);
        requireIntRange("gene.length", geneLength,
                value -> value > 0, "must be greater than zero");
        requireIntRange("max.generations", getInt("max.generations", Constants.DEFAULT_MAX_GENERATIONS),
                value -> value > 0, "must be greater than zero");
        requireDoubleRange("crossover.rate", getDouble("crossover.rate", Constants.DEFAULT_CROSSOVER_RATE),
                value -> value >= 0.0 && value <= 1.0, "must be between 0 and 1");
        requireDoubleRange("mutation.rate", getDouble("mutation.rate", Constants.DEFAULT_MUTATION_RATE),
                value -> value >= 0.0 && value <= 1.0, "must be between 0 and 1");

        String crossoverType = getString("crossover.type", Constants.DEFAULT_CROSSOVER_TYPE)
                .toLowerCase(Locale.ROOT);
        requireChoice("crossover.type", crossoverType, "onepoint", "twopoint", "uniform");

        String mutationType = getString("mutation.type", Constants.DEFAULT_MUTATION_TYPE)
                .toLowerCase(Locale.ROOT);
        requireChoice("mutation.type", mutationType, "bitflip", "swap", "gaussian", "adaptivebitflip");

        String selectionType = getString("selection.type", Constants.DEFAULT_SELECTION_TYPE)
                .toLowerCase(Locale.ROOT);
        requireChoice("selection.type", selectionType, "tournament", "roulette", "rank", "sus");
        requireDoubleRange("selection.parameter", getDouble("selection.parameter", Constants.DEFAULT_SELECTION_PARAMETER),
                value -> value > 0.0 && value <= 1.0, "must be greater than 0 and at most 1");
        requireDoubleRange("gap.size", getDouble("gap.size", Constants.DEFAULT_GAP_SIZE),
                value -> value >= 0.0 && value <= 1.0, "must be between 0 and 1");

        String initializationType = getString("initialization.type", Constants.DEFAULT_INITIALIZATION_TYPE)
                .toLowerCase(Locale.ROOT);
        requireChoice("initialization.type", initializationType, "random", "manual");
        if ("manual".equals(initializationType)) {
            String genes = getString("initialization.genes", "");
            if (genes.isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration 'initialization.genes' is required when initialization.type=manual.");
            }
            for (String entry : genes.split(",", -1)) {
                String gene = entry.trim();
                if (!gene.matches("[01]+")) {
                    throw new IllegalArgumentException(
                            "Invalid binary value in configuration 'initialization.genes': '" + gene + "'.");
                }
                if (gene.length() != geneLength) {
                    throw new IllegalArgumentException("Every value in 'initialization.genes' must have length "
                            + geneLength + "; found " + gene.length() + ".");
                }
            }
        }

        getBoolean("elitism.enabled", Constants.DEFAULT_ELITISM_ENABLED);
        getBoolean("mutation.adaptive", false);
        getBoolean("diversity.enabled", false);
        getBoolean("fitness_sharing.enabled", false);
        getBoolean("convergence.enabled", Constants.DEFAULT_CONV_ENABLED);
        getBoolean("dpe.enabled", false);
        getBoolean("dpe.gray", false);
        boolean checkpointEnabled = getBoolean("checkpoint.enabled", false);
        boolean restartEnabled = getBoolean("restart.enabled", false);
        if (checkpointEnabled && getString("checkpoint.file", "").isEmpty()) {
            throw new IllegalArgumentException(
                    "Configuration 'checkpoint.file' is required when checkpoint.enabled=true.");
        }
        if (checkpointEnabled) {
            requireIntRange("checkpoint.interval", getInt("checkpoint.interval", 1),
                    value -> value > 0, "must be greater than zero");
        }
        if (restartEnabled) {
            String restartFile = getString("restart.file", "");
            if (restartFile.isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration 'restart.file' is required when restart.enabled=true.");
            }
            if (getBoolean("dpe.enabled", false)) {
                throw new IllegalArgumentException(
                        "Restart is not supported with DPE because adaptive DPE history is not checkpointed.");
            }
            if (getBoolean("schema.enabled", false)) {
                throw new IllegalArgumentException(
                        "Restart is not supported with schema tracking because its generation history is not resumed.");
            }
            if (getInt("termination.stagnation.generations", Constants.DEFAULT_STAGNATION_GENERATIONS) > 0) {
                throw new IllegalArgumentException(
                        "Restart is not supported with stagnation termination because its history is not checkpointed.");
            }
            if (getBoolean("convergence.enabled", Constants.DEFAULT_CONV_ENABLED)
                    && getInt("convergence.maxconv", Constants.DEFAULT_MAXCONV) > 0) {
                throw new IllegalArgumentException(
                        "Restart is not supported with convergence stagnation because its history is not checkpointed.");
            }
        }
        String problem = getString("problem", ProblemFactory.DEFAULT_PROBLEM);
        if (problem.isEmpty()) {
            throw new IllegalArgumentException("Configuration 'problem' must not be empty.");
        }
        if (getBoolean("dpe.enabled", false)) {
            DPEConfiguration.from(this, geneLength);
            requireIntRange("dpe.frequency", getInt("dpe.frequency", 10), value -> value > 0,
                    "must be greater than zero");
            requireIntRange("dpe.few.threshold", getInt("dpe.few.threshold", Constants.DEFAULT_FEW_THRESHOLD),
                    value -> value > 0 && value <= getInt("population.size", Constants.DEFAULT_POPSIZE),
                    "must be between one and population.size");
            if (getString("dpe.log", "dpe.log").isEmpty()) {
                throw new IllegalArgumentException("Configuration 'dpe.log' must not be empty when DPE is enabled.");
            }
        }
        boolean schemaEnabled = getBoolean("schema.enabled", false);
        if (schemaEnabled) {
            String schemaFile = getString("schema.file", "");
            if (schemaFile.isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration 'schema.file' is required when schema.enabled=true.");
            }
            String schemaOutput = getString("schema.output", "");
            if (!schemaOutput.isEmpty()
                    && java.nio.file.Path.of(schemaFile).toAbsolutePath().normalize()
                    .equals(java.nio.file.Path.of(schemaOutput).toAbsolutePath().normalize())) {
                throw new IllegalArgumentException("Configuration 'schema.output' must not overwrite schema.file.");
            }
            getDouble("schema.worst_fitness", 0.0);
        }
        getBoolean("termination.done.enabled", Constants.DEFAULT_DONE_TERMINATION_ENABLED);
        getBoolean("termination.convergence.enabled", false);
        requireIntRange("termination.stagnation.generations",
                getInt("termination.stagnation.generations", Constants.DEFAULT_STAGNATION_GENERATIONS),
                value -> value >= 0, "must not be negative");
        requireLongRange("termination.trial_limit",
                getLong("termination.trial_limit", Constants.DEFAULT_TRIAL_LIMIT),
                value -> value >= 0L, "must not be negative");

        String diversityType = getString("diversity.strategy", "").toLowerCase(Locale.ROOT);
        if (!diversityType.isEmpty()) {
            requireChoice("diversity.strategy", diversityType,
                    "random_immigrants", "crowding", "fitness_sharing", "island_model");
        }
        if (getBoolean("diversity.enabled", false) && diversityType.isEmpty()) {
            throw new IllegalArgumentException("Configuration 'diversity.strategy' is required when diversity is enabled.");
        }
        if (getBoolean("diversity.enabled", false) && "fitness_sharing".equals(diversityType)
                && !getBoolean("fitness_sharing.enabled", false)) {
            throw new IllegalArgumentException(
                    "Configuration 'fitness_sharing.enabled' must be true when diversity.strategy=fitness_sharing.");
        }

        requireIntRange("random_immigrants.count", getInt("random_immigrants.count", 0),
                value -> value >= 0 && value <= getInt("population.size", Constants.DEFAULT_POPSIZE),
                "must be between zero and population.size");
        requireDoubleRange("crowding.similarity.threshold", getDouble("crowding.similarity.threshold", 0.3),
                value -> value >= 0.0 && value <= 1.0, "must be between 0 and 1");
        requireDoubleRange("fitness_sharing.niche_radius", getDouble("fitness_sharing.niche_radius", 0.3),
                value -> value > 0.0, "must be greater than zero");
        requireIntRange("island.count", getInt("island.count", 2), value -> value > 0,
                "must be greater than zero");
        requireIntRange("island.migration.interval", getInt("island.migration.interval", 10),
                value -> value > 0, "must be greater than zero");
        requireIntRange("island.migration.size", getInt("island.migration.size", 1),
                value -> value >= 0, "must not be negative");

        requireIntRange("convergence.few.threshold", getInt("convergence.few.threshold", Constants.DEFAULT_FEW_THRESHOLD),
                value -> value >= 0, "must not be negative");
        requireIntRange("convergence.min_generations",
                getInt("convergence.min_generations", Constants.DEFAULT_MINGEN_BEFORE_CHECK),
                value -> value >= 0, "must not be negative");
        requireDoubleRange("convergence.threshold", getDouble("convergence.threshold", Constants.DEFAULT_CONV_THRESHOLD),
                value -> value >= 0.0, "must not be negative");
        requireIntRange("convergence.maxconv", getInt("convergence.maxconv", Constants.DEFAULT_MAXCONV),
                value -> value >= 0, "must not be negative");
        requireDoubleRange("maxbias", getDouble("maxbias", Constants.DEFAULT_MAXBIAS),
                value -> value >= 0.0 && value <= 1.0, "must be between 0 and 1");
        requireDoubleRange("sigma.factor", getDouble("sigma.factor", Constants.DEFAULT_SIGMA_FACTOR),
                value -> value >= 0.0, "must not be negative");

        boolean reportEnabled = getBoolean("report.enabled", Constants.DEFAULT_REPORT_ENABLED);
        if (reportEnabled) {
            if (getString("report.file", Constants.DEFAULT_REPORT_FILE).isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration 'report.file' must not be empty when report.enabled=true.");
            }
            requireIntRange("report.interval", getInt("report.interval", Constants.DEFAULT_REPORT_INTERVAL),
                    value -> value > 0, "must be greater than zero");
        }
    }

    private static void requireChoice(String key, String value, String... choices) {
        for (String choice : choices) {
            if (choice.equals(value)) return;
        }
        throw new IllegalArgumentException("Invalid value for '" + key + "': '" + value
                + "'. Expected one of " + String.join(", ", choices) + ".");
    }

    private static void requireIntRange(String key, int value, IntPredicate predicate, String requirement) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException("Invalid value for '" + key + "': " + value + "; " + requirement + ".");
        }
    }

    private static void requireDoubleRange(String key, double value, DoublePredicate predicate, String requirement) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException("Invalid value for '" + key + "': " + value + "; " + requirement + ".");
        }
    }

    private static void requireLongRange(String key, long value,
                                         java.util.function.LongPredicate predicate, String requirement) {
        if (!predicate.test(value)) {
            throw new IllegalArgumentException("Invalid value for '" + key + "': " + value + "; " + requirement + ".");
        }
    }

    private static IllegalArgumentException invalidValue(String key, NumberFormatException cause) {
        return new IllegalArgumentException("Invalid numeric value for '" + key + "'.", cause);
    }

    // Standard GA Option Flags
    public boolean isTraceEnabled() {
        return getBoolean("trace", false);
    }

    public boolean isLogEnabled() {
        return getBoolean("log", false);
    }

    public boolean isBestEnabled() {
        return getBoolean("best", true);
    }

    public String getOptions() {
        return getString("options", Constants.DEFAULT_OPTIONS);
    }
}
