package org.example.jsga.util;

import org.example.jsga.config.Constants;
import org.example.jsga.config.FlagConfigurator;

public class InputPrinter {

    public static void printConfiguration(FlagConfigurator config) {
        String formatted = String.format(Constants.FORMAT_HEADER,
                config.getInt("experiments", Constants.DEFAULT_TOTAL_EXPERIMENTS),
                config.getLong("total_trials", Constants.DEFAULT_TOTAL_TRIALS),
                config.getInt("pop_size", Constants.DEFAULT_POPSIZE),
                config.getInt("gene_length", Constants.DEFAULT_LENGTH),
                config.getDouble("crossover_rate", Constants.DEFAULT_CROSSOVER_RATE),
                config.getDouble("mutation_rate", Constants.DEFAULT_MUTATION_RATE),
                config.getDouble("gap_size", Constants.DEFAULT_GAP_SIZE),
                config.getInt("window_size", Constants.DEFAULT_WINDOW_SIZE),
                config.getInt("interval", Constants.DEFAULT_INTERVAL),
                config.getInt("save_size", Constants.DEFAULT_SAVESIZE),
                config.getInt("max_spin", Constants.DEFAULT_MAXSPIN),
                config.getInt("dump_freq", Constants.DEFAULT_DUMP_FREQ),
                config.getInt("num_dumps", Constants.DEFAULT_NUM_DUMPS),
                config.getOptions(),
                config.getInt("seed", Constants.DEFAULT_SEED)
        );
        System.out.println("\n====== GA CONFIGURATION ======");
        System.out.println(formatted);
    }
}
