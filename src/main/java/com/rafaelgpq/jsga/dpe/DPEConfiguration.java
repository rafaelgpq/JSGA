package com.rafaelgpq.jsga.dpe;

import com.rafaelgpq.jsga.config.FlagConfigurator;

/**
 * Validated DPE segment boundaries and parameter scaling from runtime settings.
 */
public final class DPEConfiguration {

    private final int[] positions;
    private final double[] factors;
    private final double[] bases;

    private DPEConfiguration(int[] positions, double[] factors, double[] bases) {
        this.positions = positions;
        this.factors = factors;
        this.bases = bases;
    }

    public static DPEConfiguration from(FlagConfigurator config, int geneLength) {
        int[] positions = parseInts(config.getString("dpe.positions", ""), "dpe.positions");
        double[] factors = parseDoubles(config.getString("dpe.factors", ""), "dpe.factors");
        double[] bases = parseDoubles(config.getString("dpe.bases", ""), "dpe.bases");
        if (positions.length == 0 || factors.length != positions.length || bases.length != positions.length) {
            throw new IllegalArgumentException(
                    "DPE positions, factors, and bases must have the same non-zero number of entries.");
        }

        int previousEndpoint = 0;
        for (int i = 0; i < positions.length; i++) {
            long endpointLong = Math.abs((long) positions[i]);
            if (endpointLong <= previousEndpoint || endpointLong > geneLength) {
                throw new IllegalArgumentException("Each absolute DPE position must increase and fit within gene.length.");
            }
            if (positions[i] > 0 && endpointLong - previousEndpoint < 2) {
                throw new IllegalArgumentException("DPE-enabled parameter segments must contain at least two bits.");
            }
            if (factors[i] <= 0.0) {
                throw new IllegalArgumentException("DPE factors must be greater than zero.");
            }
            previousEndpoint = (int) endpointLong;
        }
        return new DPEConfiguration(positions, factors, bases);
    }

    public DPEngine createEngine(int frequency, int fewThreshold, int populationSize, String logFile) {
        return new DPEngine(positions, factors, bases, new double[positions.length * 2],
                positions.length, frequency, fewThreshold, populationSize, logFile);
    }

    private static int[] parseInts(String value, String key) {
        String[] entries = value.split(",", -1);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration '" + key + "' is required when DPE is enabled.");
        }
        int[] parsed = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
            try {
                parsed[i] = Integer.parseInt(entries[i].trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid integer in configuration '" + key + "'.", exception);
            }
        }
        return parsed;
    }

    private static double[] parseDoubles(String value, String key) {
        String[] entries = value.split(",", -1);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration '" + key + "' is required when DPE is enabled.");
        }
        double[] parsed = new double[entries.length];
        for (int i = 0; i < entries.length; i++) {
            try {
                parsed[i] = Double.parseDouble(entries[i].trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid number in configuration '" + key + "'.", exception);
            }
            if (!Double.isFinite(parsed[i])) {
                throw new IllegalArgumentException("Values in configuration '" + key + "' must be finite.");
            }
        }
        return parsed;
    }
}
