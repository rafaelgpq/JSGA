package com.rafaelgpq.jsga.checkpoint;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.SimpleIndividual;
import com.rafaelgpq.jsga.model.SimplePopulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the population from a JSGA checkpoint file.
 */
public class CheckpointReader {

    public Population readCheckpoint(String path, int geneLength, int populationSize) throws IOException {
        return readCheckpointState(path, geneLength, populationSize).getPopulation();
    }

    public CheckpointState readCheckpointState(String path, int geneLength, int populationSize) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Checkpoint path must not be empty.");
        }
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Population size must be greater than zero.");
        }

        try (BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            String line;
            boolean foundPopulation = false;
            Integer generation = null;
            long evaluationCount = -1L;
            long[] randomState = null;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("Generation:")) {
                    try {
                        generation = Integer.parseInt(trimmed.substring("Generation:".length()).trim());
                    } catch (NumberFormatException exception) {
                        throw new IOException("Checkpoint contains an invalid generation number.", exception);
                    }
                    if (generation < 0) {
                        throw new IOException("Checkpoint generation must not be negative.");
                    }
                } else if (trimmed.startsWith("Evaluations:")) {
                    try {
                        evaluationCount = Long.parseLong(trimmed.substring("Evaluations:".length()).trim());
                    } catch (NumberFormatException exception) {
                        throw new IOException("Checkpoint contains an invalid evaluation count.", exception);
                    }
                    if (evaluationCount < 0) {
                        throw new IOException("Checkpoint evaluation count must not be negative.");
                    }
                } else if ("Random Snapshot:".equals(trimmed)) {
                    String stateLine = reader.readLine();
                    randomState = parseRandomState(stateLine);
                }
                if ("Population:".equals(trimmed)) {
                    foundPopulation = true;
                    break;
                }
            }
            if (!foundPopulation) {
                throw new IOException("Checkpoint does not contain a Population section: " + path);
            }

            List<Individual> individuals = new ArrayList<>(populationSize);
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (!individuals.isEmpty()) break;
                    continue;
                }
                if (line.endsWith(":")) break;
                if (individuals.size() == populationSize) {
                    throw new IOException("Checkpoint contains more individuals than the configured population size.");
                }
                individuals.add(parseIndividual(line, geneLength, individuals.size()));
            }
            if (individuals.size() != populationSize) {
                throw new IOException("Checkpoint contains " + individuals.size()
                        + " individuals; expected " + populationSize + ".");
            }
            if (generation == null) {
                throw new IOException("Checkpoint does not contain a Generation field: " + path);
            }
            return new CheckpointState(generation, new SimplePopulation(individuals),
                    evaluationCount, randomState);
        }
    }

    private static long[] parseRandomState(String line) throws IOException {
        if (line == null) throw new IOException("Checkpoint random snapshot is missing.");
        String[] values = line.trim().split("\\s+");
        if (values.length != 4) {
            throw new IOException("Checkpoint random snapshot must contain exactly four state values.");
        }
        long[] state = new long[4];
        try {
            for (int i = 0; i < state.length; i++) state[i] = Long.parseLong(values[i]);
        } catch (NumberFormatException exception) {
            throw new IOException("Checkpoint contains an invalid random snapshot.", exception);
        }
        if (state[1] < 0 || (state[2] != 0 && state[2] != 1)
                || !Double.isFinite(Double.longBitsToDouble(state[3]))) {
            throw new IOException("Checkpoint random snapshot is invalid.");
        }
        return state;
    }

    private static Individual parseIndividual(String line, int geneLength, int index) throws IOException {
        String[] parts = line.split("\\s+");
        if (parts.length != 3 && parts.length != 4) {
            throw new IOException("Invalid checkpoint individual at index " + index + ".");
        }
        String bitString = parts[0];
        if (bitString.length() != geneLength || !bitString.matches("[01]+")) {
            throw new IOException("Invalid genome at checkpoint individual index " + index
                    + "; expected " + geneLength + " binary bits.");
        }

        double fitness;
        double rawFitness;
        int needsEvaluation;
        try {
            fitness = Double.parseDouble(parts[1]);
            needsEvaluation = Integer.parseInt(parts[2]);
            rawFitness = parts.length == 4 ? Double.parseDouble(parts[3]) : fitness;
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid numeric state at checkpoint individual index " + index + ".", exception);
        }
        if (!Double.isFinite(fitness) || !Double.isFinite(rawFitness)
                || (needsEvaluation != 0 && needsEvaluation != 1)) {
            throw new IOException("Invalid fitness or evaluation state at checkpoint individual index " + index + ".");
        }

        byte[] gene = packBits(bitString);
        SimpleIndividual individual = new SimpleIndividual(gene, geneLength);
        individual.setFitness(fitness);
        individual.setRawFitness(rawFitness);
        individual.setNeedsEvaluation(needsEvaluation == 1);
        return individual;
    }

    private static byte[] packBits(String bitString) {
        byte[] packed = new byte[(bitString.length() + Byte.SIZE - 1) / Byte.SIZE];
        for (int i = 0; i < bitString.length(); i++) {
            if (bitString.charAt(i) == '1') {
                packed[i / Byte.SIZE] |= (byte) (1 << (i % Byte.SIZE));
            }
        }
        return packed;
    }
}
