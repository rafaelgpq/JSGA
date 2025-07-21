package org.example.jsga.config;

public class Constants {
    public static final int DEFAULT_POPSIZE = 50;
    public static final int DEFAULT_LENGTH = 30;
    public static final int DEFAULT_MAX_GENERATIONS = 1000;
    public static final double DEFAULT_CROSSOVER_RATE = 0.6;
    public static final double DEFAULT_MUTATION_RATE = 0.005;
    public static final boolean DEFAULT_ELITISM_ENABLED = true;
    public static final double DEFAULT_GAP_SIZE = 1.0;
    public static final int DEFAULT_TOTAL_EXPERIMENTS = 1;
    public static final long DEFAULT_TOTAL_TRIALS = 5000L;
    public static final int DEFAULT_INTERVAL = 500;
    public static final int DEFAULT_WINDOW_SIZE = -1;
    public static final int DEFAULT_SAVESIZE = 1;
    public static final int DEFAULT_MAXSPIN = 2;
    public static final int DEFAULT_DUMP_FREQ = 0;
    public static final int DEFAULT_NUM_DUMPS = 1;
    public static final String DEFAULT_OPTIONS = "Aclu";
    public static final int DEFAULT_SEED = 123456789;

    // Convergence control thresholds
    public static final boolean DEFAULT_CONV_ENABLED = true;
    public static final double DEFAULT_CONV_THRESHOLD = 0.88;
    public static final int DEFAULT_MAXCONV = 0;
    public static final int DEFAULT_MINGEN_BEFORE_CHECK = 5;

    // Bias and diversity control
    public static final double DEFAULT_MAXBIAS = 0.5;
    public static final double DEFAULT_SIGMA_FACTOR = 1.0;

    public static final boolean BIAS_CHECK_ENABLED = false;
    public static final boolean SIGMA_CHECK_ENABLED = false;

    public static final int DEFAULT_FEW_THRESHOLD = 2;

    public static final String FORMAT_HEADER = "\n      Experiments = %d\n"
            + "     Total Trials = %d\n"
            + "  Population Size = %d\n"
            + " Structure Length = %d\n"
            + "   Crossover Rate = %.4f\n"
            + "    AdaptiveBitFlipPopulationMutation Rate = %.4f\n"
            + "   Generation Gap = %.4f\n"
            + "   Scaling Window = %d\n"
            + "  Report Interval = %d\n"
            + " Structures Saved = %d\n"
            + "Max Gens w/o Eval = %d\n"
            + "      Dump Interval = %d\n"
            + "        Dumps Saved = %d\n"
            + "            Options = %s\n"
            + "        Random Seed = %d\n";

    private Constants() {}
}
