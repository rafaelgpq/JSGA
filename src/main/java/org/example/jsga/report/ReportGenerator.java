package org.example.jsga.report;

import java.io.*;
import java.util.*;

/**
 * Processes GA output files and computes summary statistics.
 */
public class ReportGenerator {

    private static final int COLUMNS = 9;

    public static void main(String[] args) throws IOException {
        String outputFile = args.length > 0 ? args[0] : "ga_output.txt";

        List<double[]> lines = readData(outputFile);
        if (lines.isEmpty()) {
            System.err.println("No data found in output file.");
            return;
        }

        double[] avg = new double[COLUMNS];
        double[] var = new double[COLUMNS];
        int rows = lines.size();

        // Compute means
        for (double[] line : lines) {
            for (int i = 0; i < COLUMNS; i++) {
                avg[i] += line[i];
            }
        }
        for (int i = 0; i < COLUMNS; i++) {
            avg[i] /= rows;
        }

        // Compute variances
        for (double[] line : lines) {
            for (int i = 0; i < COLUMNS; i++) {
                var[i] += Math.pow(line[i] - avg[i], 2);
            }
        }
        for (int i = 0; i < COLUMNS; i++) {
            var[i] /= (rows - 1);
        }

        // Print results
        System.out.println("\n=== GA Summary Report ===");
        for (int i = 0; i < COLUMNS; i++) {
            System.out.printf("Col %d: Avg = %.5f | Var = %.5f\n", i + 1, avg[i], var[i]);
        }
    }

    /**
     * Reads whitespace-separated numeric lines from a file.
     */
    private static List<double[]> readData(String path) throws IOException {
        List<double[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < COLUMNS) continue;
                double[] values = new double[COLUMNS];
                for (int i = 0; i < COLUMNS; i++) {
                    values[i] = Double.parseDouble(tokens[i]);
                }
                lines.add(values);
            }
        }
        return lines;
    }
}
