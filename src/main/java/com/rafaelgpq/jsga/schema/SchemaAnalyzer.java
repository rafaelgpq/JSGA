package com.rafaelgpq.jsga.schema;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Analyzes how many individuals match a predefined schema.
 */
public class SchemaAnalyzer {

    private final char[] schema;
    private final double worstFitness;
    private final Path outputFile;
    private int lastCount;
    private int lastGeneration = -1;
    private boolean hasPriorMatchCount;

    public SchemaAnalyzer(String schemaFile, double worstFitness) throws IOException {
        this(schemaFile, schemaFile + ".analysis", worstFitness);
    }

    public SchemaAnalyzer(String schemaFile, String outputFile, double worstFitness) throws IOException {
        Objects.requireNonNull(schemaFile, "Schema file must not be null.");
        Objects.requireNonNull(outputFile, "Schema output file must not be null.");
        if (!Double.isFinite(worstFitness)) {
            throw new IllegalArgumentException("Worst fitness must be finite.");
        }
        Path schemaPath = Path.of(schemaFile).toAbsolutePath().normalize();
        this.outputFile = Path.of(outputFile).toAbsolutePath().normalize();
        if (schemaPath.equals(this.outputFile)) {
            throw new IllegalArgumentException("Schema output must not overwrite the schema input file.");
        }
        this.worstFitness = worstFitness;
        this.schema = loadSchema(schemaPath);
        initializeOutput();
    }

    private char[] loadSchema(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new IllegalArgumentException("Schema file must contain a non-empty schema.");
            }
            char[] parsed = line.trim().toCharArray();
            for (char locus : parsed) {
                if (locus != '0' && locus != '1' && locus != '#') {
                    throw new IllegalArgumentException("Schema may contain only '0', '1', and '#'.");
                }
            }
            return parsed;
        }
    }

    private void initializeOutput() throws IOException {
        Path parent = outputFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write(new String(schema));
            writer.newLine();
            writer.write("Gen  Count  Incr  Expct  SchemaAve   PopAve");
            writer.newLine();
        }
    }

    public void analyze(Population population, int generation) throws IOException {
        Objects.requireNonNull(population, "Population must not be null.");
        if (generation < 0 || generation <= lastGeneration) {
            throw new IllegalArgumentException("Generation must be non-negative and strictly increasing.");
        }
        int popSize = population.size();
        if (popSize == 0) {
            throw new IllegalArgumentException("Cannot analyze an empty population.");
        }
        int count = 0;
        double schemaFitnessSum = 0.0;
        double schemaRelativeFitness = 0.0;
        double totalRelativeFitness = 0.0;

        for (int i = 0; i < popSize; i++) {
            Individual ind = Objects.requireNonNull(population.get(i),
                    "Population individual at index " + i + " must not be null.");
            if (ind.getGeneLength() != schema.length || ind.getGene() == null
                    || ind.getGene().length != (schema.length + 7) / 8) {
                throw new IllegalArgumentException("Individual at index " + i
                        + " does not match schema length " + schema.length + ".");
            }
            double fitness = ind.getFitness();
            if (!Double.isFinite(fitness) || fitness > worstFitness) {
                throw new IllegalArgumentException("Fitness values must be finite and no greater than worstFitness.");
            }
            boolean matches = true;
            for (int j = 0; j < schema.length; j++) {
                if (schema[j] == '#') continue;
                if (((ind.getGene()[j / 8] & (1 << (j % 8))) != 0) != (schema[j] == '1')) {
                    matches = false;
                    break;
                }
            }
            double relativeFitness = worstFitness - fitness;
            totalRelativeFitness += relativeFitness;
            if (matches) {
                count++;
                schemaFitnessSum += fitness;
                schemaRelativeFitness += relativeFitness;
            }
        }

        double expected = totalRelativeFitness > 0.0
                ? schemaRelativeFitness * popSize / totalRelativeFitness : count;
        double schemaAverage = count == 0 ? Double.NaN : schemaFitnessSum / count;
        double increment = hasPriorMatchCount ? (double) count / lastCount : count > 0 ? 1.0 : 0.0;
        if (count > 0) {
            lastCount = count;
            hasPriorMatchCount = true;
        }
        lastGeneration = generation;

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(String.format(java.util.Locale.ROOT, "%4d  %5d  %5.3f  %6.3f  %10.3e  %10.3e",
                    generation, count, increment, expected, schemaAverage, totalRelativeFitness / popSize));
            writer.newLine();
        }
    }

    public Path getOutputFile() {
        return outputFile;
    }
}
