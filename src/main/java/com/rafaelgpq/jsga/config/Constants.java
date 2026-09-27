package com.rafaelgpq.jsga.config;

public class Constants {
    public static final int DEFAULT_POPSIZE = 50;
    public static final int DEFAULT_LENGTH = 30;
    public static final int DEFAULT_MAX_GENERATIONS = 1000;
    public static final double DEFAULT_CROSSOVER_RATE = 0.6;
    public static final double DEFAULT_MUTATION_RATE = 0.005;
    public static final String DEFAULT_CROSSOVER_TYPE = "onepoint";
    public static final String DEFAULT_MUTATION_TYPE = "bitflip";
    public static final String DEFAULT_SELECTION_TYPE = "tournament";
    public static final String DEFAULT_INITIALIZATION_TYPE = "random";
    public static final double DEFAULT_SELECTION_PARAMETER = 1.0;
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
    public static final long DEFAULT_SEED = 123456789L;
    public static final boolean DEFAULT_DONE_TERMINATION_ENABLED = true;
    public static final int DEFAULT_STAGNATION_GENERATIONS = 0;
    public static final long DEFAULT_TRIAL_LIMIT = 0L;

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

    // Report output (GAucsd report.c / measure.c parity)
    public static final boolean DEFAULT_REPORT_ENABLED = false;
    public static final String DEFAULT_REPORT_FILE = "ga_output.txt";
    public static final int DEFAULT_REPORT_INTERVAL = 1;

    private Constants() {}
}
