package com.rafaelgpq.jsga.checkpoint;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.DecodeUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;

/**
 * Writes a checkpoint file containing the generation and population state.
 */
public class CheckpointWriter {

    public void writeCheckpoint(String path,
                                Population population,
                                int generation,
                                double[] fitnessWindow,
                                long[] randomState,
                                int rngPosition,
                                double[][] dpeState,
                                boolean saveBest) throws IOException {
        writeCheckpoint(path, population, generation, fitnessWindow, randomState,
                rngPosition, dpeState, saveBest, -1L);
    }

    public void writeCheckpoint(String path,
                                Population population,
                                int generation,
                                double[] fitnessWindow,
                                long[] randomState,
                                int rngPosition,
                                double[][] dpeState,
                                boolean saveBest,
                                long evaluationCount) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkpoint path must not be empty.");
        }
        validate(population, generation, fitnessWindow, randomState, rngPosition, dpeState, evaluationCount);

        Path target = Path.of(path).toAbsolutePath().normalize();
        Path fileName = target.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Checkpoint path must identify a file.");
        }
        String prefix = fileName.toString();
        if (prefix.length() < 3) prefix = (prefix + "___").substring(0, 3);
        Path temporary = Files.createTempFile(target.getParent(), prefix, ".tmp");
        try {
            writeCheckpointFile(temporary, population, generation, fitnessWindow,
                    randomState, rngPosition, dpeState, saveBest, evaluationCount);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void writeCheckpointFile(Path path,
                                            Population population,
                                            int generation,
                                            double[] fitnessWindow,
                                            long[] randomState,
                                            int rngPosition,
                                            double[][] dpeState,
                                            boolean saveBest,
                                            long evaluationCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("Generation: " + generation);
            writer.newLine();
            if (evaluationCount >= 0) {
                writer.write("Evaluations: " + evaluationCount);
                writer.newLine();
            }

            if (fitnessWindow != null) {
                writer.newLine();
                writer.write("Window:");
                writer.newLine();
                for (double value : fitnessWindow) {
                    writeNumber(writer, value);
                    writer.write('\t');
                }
                writer.newLine();
            }

            if (randomState != null) {
                writer.newLine();
                boolean isSnapshot = randomState.length == 4 && rngPosition == 0;
                writer.write(isSnapshot ? "Random Snapshot:" : "Random State:");
                writer.newLine();
                for (long value : randomState) {
                    writer.write(Long.toString(value));
                    writer.write('\t');
                }
                if (!isSnapshot) writer.write(Integer.toString(rngPosition));
                writer.newLine();
            }

            if (dpeState != null) {
                writer.newLine();
                writer.write("DPE State:");
                writer.newLine();
                for (double[] row : dpeState) {
                    for (int i = 0; i < row.length; i++) {
                        if (i > 0) writer.write('\t');
                        writeNumber(writer, row[i]);
                    }
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("Population:");
            writer.newLine();
            for (int i = 0; i < population.size(); i++) {
                writeIndividual(writer, population.get(i));
            }

            if (saveBest) {
                writer.newLine();
                writer.write("Best:");
                writer.newLine();
                Individual best = population.getBestIndividual();
                writeIndividual(writer, best);
            }
        }
    }

    private static void validate(Population population,
                                 int generation,
                                 double[] fitnessWindow,
                                 long[] randomState,
                                 int rngPosition,
                                 double[][] dpeState,
                                 long evaluationCount) {
        Objects.requireNonNull(population, "Population must not be null.");
        if (generation < 0) {
            throw new IllegalArgumentException("Generation must not be negative.");
        }
        if (population.size() == 0) {
            throw new IllegalArgumentException("Cannot checkpoint an empty population.");
        }
        if (rngPosition < 0) {
            throw new IllegalArgumentException("RNG position must not be negative.");
        }
        if (evaluationCount < -1L) {
            throw new IllegalArgumentException("Evaluation count must be non-negative or unavailable.");
        }
        if (fitnessWindow != null) {
            for (double value : fitnessWindow) requireFinite(value, "Fitness window values must be finite.");
        }
        if (dpeState != null) {
            for (double[] row : dpeState) {
                if (row == null || row.length != 4) {
                    throw new IllegalArgumentException("Each DPE state row must contain exactly four values.");
                }
                for (double value : row) requireFinite(value, "DPE state values must be finite.");
            }
        }

        Individual firstIndividual = Objects.requireNonNull(population.get(0),
                "Population individual at index 0 must not be null.");
        int geneLength = firstIndividual.getGeneLength();
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Checkpoint gene length must be greater than zero.");
        }
        for (int i = 0; i < population.size(); i++) {
            Individual individual = Objects.requireNonNull(population.get(i),
                    "Population individual at index " + i + " must not be null.");
            if (individual.getGeneLength() != geneLength
                    || individual.getGene() == null
                    || individual.getGene().length != (geneLength + Byte.SIZE - 1) / Byte.SIZE) {
                throw new IllegalArgumentException("Population individual at index " + i
                        + " has an invalid genome shape.");
            }
            requireFinite(individual.getFitness(), "Population fitness values must be finite.");
            requireFinite(individual.getRawFitness(), "Population raw fitness values must be finite.");
        }
    }

    private static void writeIndividual(BufferedWriter writer, Individual individual) throws IOException {
        boolean[] bits = DecodeUtils.unpack(individual.getGene(), individual.getGeneLength());
        for (boolean bit : bits) writer.write(bit ? '1' : '0');
        writer.write(String.format(Locale.ROOT, " %.17g %d %.17g%n",
                individual.getFitness(), individual.needsEvaluation() ? 1 : 0, individual.getRawFitness()));
    }

    private static void writeNumber(BufferedWriter writer, double value) throws IOException {
        writer.write(String.format(Locale.ROOT, "%.17g", value));
    }

    private static void requireFinite(double value, String message) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
