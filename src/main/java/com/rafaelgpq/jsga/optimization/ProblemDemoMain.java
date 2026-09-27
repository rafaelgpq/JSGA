package com.rafaelgpq.jsga.optimization;

import java.util.List;
import java.util.Locale;

/** Runs reproducible problem demonstrations using the bundled catalog. */
public final class ProblemDemoMain {

    private ProblemDemoMain() {
    }

    public static void main(String[] args) {
        Options options = Options.parse(args);
        List<String> ids = "all".equals(options.problemId)
                ? ProblemCatalog.ids() : List.of(options.problemId);
        GeneticAlgorithm algorithm = new GeneticAlgorithm();
        for (String id : ids) {
            OptimizationProblem<?> problem = ProblemCatalog.create(id);
            run(algorithm, problem, options);
        }
    }

    private static <S> void run(GeneticAlgorithm algorithm, OptimizationProblem<S> problem, Options options) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        double mean = 0.0;
        System.out.printf(Locale.ROOT,
                "%n%s | population=%d generations=%d tournament=%d crossover=%.3f mutation=%.3f runs=%d%n",
                problem.name(), options.population, options.generations, options.tournament,
                options.crossover, options.mutation, options.runs);
        for (int run = 0; run < options.runs; run++) {
            long runSeed = Math.addExact(options.seed, run);
            OptimizationConfig config = new OptimizationConfig(options.population, options.generations,
                    options.tournament, options.crossover, options.mutation, runSeed);
            OptimizationResult<S> result = algorithm.solve(problem, config);
            minimum = Math.min(minimum, result.getBestFitness());
            maximum = Math.max(maximum, result.getBestFitness());
            mean = mean * (run / (double) (run + 1)) + result.getBestFitness() / (run + 1.0);
            System.out.printf(Locale.ROOT,
                    "  run=%d seed=%d bestFitness=%.10g generations=%d evaluations=%d bestSolution=%s%n",
                    run + 1, runSeed, result.getBestFitness(), result.getGenerations(),
                    result.getEvaluations(), result.getBestSolution());
        }
        System.out.printf(Locale.ROOT, "  family summary: best=%.10g mean=%.10g worst=%.10g%n",
                minimum, mean, maximum);
    }

    private static final class Options {
        private String problemId = "onemax";
        private int generations = 150;
        private int population = 80;
        private int tournament = 3;
        private double crossover = 0.9;
        private double mutation = 0.04;
        private long seed = 12345L;
        private int runs = 1;

        private static Options parse(String[] args) {
            Options options = new Options();
            int index = 0;
            if (args.length > 0 && !args[0].startsWith("--")) {
                options.problemId = args[index++];
            }
            while (index < args.length) {
                String name = args[index++];
                if (index >= args.length) {
                    throw new IllegalArgumentException("Missing value for " + name + ". " + usage());
                }
                String value = args[index++];
                switch (name) {
                    case "--generations":
                        options.generations = parseNonNegativeInt(value, name);
                        break;
                    case "--population":
                        options.population = parsePositiveInt(value, name);
                        break;
                    case "--tournament":
                        options.tournament = parsePositiveInt(value, name);
                        break;
                    case "--crossover":
                        options.crossover = parseRate(value, name);
                        break;
                    case "--mutation":
                        options.mutation = parseRate(value, name);
                        break;
                    case "--seed":
                        options.seed = parseLong(value, name);
                        break;
                    case "--runs":
                        options.runs = parsePositiveInt(value, name);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown option '" + name + "'. " + usage());
                }
            }
            if (options.tournament > options.population) {
                throw new IllegalArgumentException("--tournament must not exceed --population.");
            }
            return options;
        }

        private static int parseNonNegativeInt(String value, String name) {
            int parsed = parseInteger(value, name);
            if (parsed < 0) throw new IllegalArgumentException(name + " must not be negative.");
            return parsed;
        }

        private static int parsePositiveInt(String value, String name) {
            int parsed = parseInteger(value, name);
            if (parsed <= 0) throw new IllegalArgumentException(name + " must be positive.");
            return parsed;
        }

        private static int parseInteger(String value, String name) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid integer for " + name + ": " + value, exception);
            }
        }

        private static long parseLong(String value, String name) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid integer for " + name + ": " + value, exception);
            }
        }

        private static double parseRate(String value, String name) {
            final double parsed;
            try {
                parsed = Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid rate for " + name + ": " + value, exception);
            }
            if (!Double.isFinite(parsed) || parsed < 0.0 || parsed > 1.0) {
                throw new IllegalArgumentException(name + " must be between zero and one.");
            }
            return parsed;
        }

        private static String usage() {
            return "Usage: ProblemDemoMain [problem-id|all] [--generations N] [--population N] "
                    + "[--tournament N] [--crossover RATE] [--mutation RATE] [--seed N] [--runs N]";
        }
    }
}
