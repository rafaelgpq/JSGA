package org.example.jsga.schema;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.io.*;

/**
 * Analyzes how many individuals match a predefined schema.
 */
public class SchemaAnalyzer {

    private final char[] schema;
    private final double worstFitness;
    private final File outputFile;
    private boolean firstRun = true;
    private int lastCount = 1;

    public SchemaAnalyzer(String schemaFile, double worstFitness) throws IOException {
        this.worstFitness = worstFitness;
        this.schema = loadSchema(schemaFile);
        this.outputFile = new File(schemaFile);
        initializeOutput();
    }

    private char[] loadSchema(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return reader.readLine().trim().toCharArray();
        }
    }

    private void initializeOutput() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, false))) {
            writer.println(new String(schema));
            writer.println("Gen  Count  Incr  Expct  SchemaAve   PopAve");
        }
    }

    public void analyze(Population population, int generation) throws IOException {
        int popSize = population.size();
        int count = 0;
        double perfSum = 0;
        double rawExpected = 0;
        double totalPerf = 0;

        for (int i = 0; i < popSize; i++) {
            Individual ind = population.get(i);
            boolean[] unpacked = unpack(ind.getGene(), schema.length);

            boolean matches = true;
            for (int j = 0; j < schema.length; j++) {
                if (schema[j] == '#') continue;
                boolean bit = schema[j] == '1';
                if (bit != unpacked[j]) {
                    matches = false;
                    break;
                }
            }

            double perf = ind.getFitness();
            if (perf < worstFitness) {
                totalPerf += (worstFitness - perf);
                if (matches) {
                    count++;
                    perfSum += perf;
                }
            } else if (matches) {
                count++;
                perfSum += perf;
                rawExpected += (worstFitness - perf);
            }
        }

        if (count == 0) return;
        double expected = (worstFitness * count - perfSum - rawExpected) * popSize / totalPerf;
        double schemaAve = perfSum / count;
        double incr = ((double) count) / lastCount;
        lastCount = count;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.printf("%4d  %5d  %5.3f  %6.3f  %10.3e  %10.3e\n",
                    generation, count, incr, expected, schemaAve, totalPerf / popSize);
        }
    }

    private boolean[] unpack(byte[] gene, int length) {
        boolean[] bits = new boolean[length];
        for (int i = 0; i < length; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            bits[i] = (gene[byteIndex] & (1 << bitIndex)) != 0;
        }
        return bits;
    }
}

