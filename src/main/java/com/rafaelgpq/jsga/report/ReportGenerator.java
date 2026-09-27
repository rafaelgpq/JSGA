package com.rafaelgpq.jsga.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Computes summary statistics for whitespace-separated GA report files.
 */
public final class ReportGenerator {

    private static final int COLUMNS = 9;

    private ReportGenerator() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: ReportGenerator [path/to/report.txt]");
        }
        Path input = Path.of(args.length == 0 ? "ga_output.txt" : args[0]);
        Summary summary = summarize(input);
        if (summary.getRowCount() == 0) {
            System.err.println("No data found in output file.");
            return;
        }

        System.out.println("\n=== GA Summary Report ===");
        double[] means = summary.getMeans();
        double[] variances = summary.getSampleVariances();
        for (int i = 0; i < COLUMNS; i++) {
            System.out.printf("Col %d: Avg = %.5f | Var = %.5f%n",
                    i + 1, means[i], variances[i]);
        }
    }

    /**
     * Reads the first nine numeric fields from each data row and computes sample statistics.
     * Blank lines, comments, and a non-numeric header before the first data row are ignored.
     */
    public static Summary summarize(Path input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Report path must not be null.");
        }
        double[] means = new double[COLUMNS];
        double[] sumSquaredDifferences = new double[COLUMNS];
        int rows = 0;
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                String[] tokens = trimmed.split("\\s+");
                double[] values = parseRow(tokens, rows, lineNumber);
                if (values == null) continue;
                rows++;
                for (int i = 0; i < COLUMNS; i++) {
                    double delta = values[i] - means[i];
                    means[i] += delta / rows;
                    sumSquaredDifferences[i] += delta * (values[i] - means[i]);
                }
            }
        }

        double[] variances = new double[COLUMNS];
        if (rows > 1) {
            for (int i = 0; i < COLUMNS; i++) {
                variances[i] = sumSquaredDifferences[i] / (rows - 1);
                if (!Double.isFinite(means[i]) || !Double.isFinite(variances[i])) {
                    throw new IOException("Report statistics overflowed for column " + (i + 1) + ".");
                }
            }
        }
        return new Summary(rows, means, variances);
    }

    private static double[] parseRow(String[] tokens, int rowsRead, int lineNumber) throws IOException {
        boolean firstFieldIsNumeric;
        try {
            Double.parseDouble(tokens[0]);
            firstFieldIsNumeric = true;
        } catch (NumberFormatException exception) {
            if (rowsRead == 0) return null;
            throw new IOException("Unexpected non-numeric report row at line " + lineNumber + ".", exception);
        }

        if (tokens.length < COLUMNS) {
            if (!firstFieldIsNumeric && rowsRead == 0) return null;
            throw new IOException("Report row at line " + lineNumber + " has fewer than "
                    + COLUMNS + " columns.");
        }
        double[] values = new double[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            try {
                values[i] = Double.parseDouble(tokens[i]);
            } catch (NumberFormatException exception) {
                throw new IOException("Invalid number in report column " + (i + 1)
                        + " at line " + lineNumber + ".", exception);
            }
            if (!Double.isFinite(values[i])) {
                throw new IOException("Non-finite number in report column " + (i + 1)
                        + " at line " + lineNumber + ".");
            }
        }
        return values;
    }

    public static final class Summary {
        private final int rowCount;
        private final double[] means;
        private final double[] sampleVariances;

        private Summary(int rowCount, double[] means, double[] sampleVariances) {
            this.rowCount = rowCount;
            this.means = Arrays.copyOf(means, means.length);
            this.sampleVariances = Arrays.copyOf(sampleVariances, sampleVariances.length);
        }

        public int getRowCount() {
            return rowCount;
        }

        public double[] getMeans() {
            return Arrays.copyOf(means, means.length);
        }

        public double[] getSampleVariances() {
            return Arrays.copyOf(sampleVariances, sampleVariances.length);
        }
    }
}
