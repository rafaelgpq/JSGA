package org.example.jsga.checkpoint;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.model.SimpleIndividual;
import org.example.jsga.model.SimplePopulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a checkpoint file and restores population state.
 */
public class CheckpointReader {

    public Population readCheckpoint(String path, int geneLength, int populationSize) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            List<Individual> individuals = new ArrayList<>();

            // Skip to the "Population:" section
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("Population:")) break;
            }

            // Read individuals
            for (int i = 0; i < populationSize && (line = reader.readLine()) != null; ) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                String bitString = parts[0];
                double fitness = Double.parseDouble(parts[1]);
                boolean needsEval = parts.length > 2 && parts[2].equals("1");

                byte[] gene = packBits(bitString);
                SimpleIndividual individual = new SimpleIndividual(gene, geneLength);
                individual.setGene(gene);
                individual.setGeneLength(geneLength);
                individual.setFitness(fitness);
                individual.setNeedsEvaluation(needsEval);

                individuals.add(individual);
                i++;
            }

            return new SimplePopulation(individuals);
        }
    }

    private byte[] packBits(String bitString) {
        int byteLen = (bitString.length() + 7) / 8;
        byte[] packed = new byte[byteLen];
        for (int i = 0; i < bitString.length(); i++) {
            if (bitString.charAt(i) == '1') {
                packed[i / 8] |= (1 << (i % 8));
            }
        }
        return packed;
    }
}
