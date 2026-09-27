package com.rafaelgpq.jsga.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * Appends per-generation performance rows to a GA output file, mirroring the
 * nine-column layout GAucsd's {@code measure.c} writes via the {@code OUT_F2}
 * format ({@code Gen Trials Lost Conv Bias Online Offline Best Average}).
 * The resulting file can be summarized with {@link ReportGenerator}.
 */
public final class GaOutputWriter {

    private GaOutputWriter() {
    }

    /**
     * Creates (or truncates) the report file so a new run starts with a clean slate,
     * matching how GAucsd recreates its Outfile at the start of each run.
     */
    public static void initialize(String path) throws IOException {
        Path file = requirePath(path);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            // Intentionally empty: truncates any pre-existing report file.
        }
    }

    /**
     * Appends one report row for the given generation.
     */
    public static void appendGeneration(String path, int generation, long trials, int lost, int converged,
                                         double bias, double online, double offline, double best,
                                         double average) throws IOException {
        Path file = requirePath(path);
        if (generation < 0) {
            throw new IllegalArgumentException("Generation must not be negative.");
        }
        if (trials < 0) {
            throw new IllegalArgumentException("Trials must not be negative.");
        }
        if (!Double.isFinite(bias) || !Double.isFinite(online) || !Double.isFinite(offline)
                || !Double.isFinite(best) || !Double.isFinite(average)) {
            throw new IllegalArgumentException("Report values must be finite.");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
            writer.write(String.format(Locale.ROOT, "%d %d %d %d %.5f %.5e %.5e % .5e % .5e",
                    generation, trials, lost, converged, bias, online, offline, best, average));
            writer.newLine();
        }
    }

    private static Path requirePath(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Report output path must not be empty.");
        }
        Path file = Path.of(path);
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        return file;
    }
}
