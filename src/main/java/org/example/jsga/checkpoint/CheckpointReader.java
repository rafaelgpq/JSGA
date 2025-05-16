package org.example.jsga.checkpoint;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

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

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("Population:")) break;
            }

            for (int i = 0; i < populationSize && (line = reader.readLine()) != null; i++) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                String bitString = parts[0];
                double fitness = Double.parseDouble(parts[1]);
                boolean needsEval = parts.length > 2 && parts[2].equals("1");

                byte[] gene = packBits(bitString);
                Individual individual = new SimpleIndividual(gene, geneLength);
                individual.setFitness(fitness);
                individual.setNeedsEvaluation(needsEval);
                individuals.add(individual);
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

    // Simple implementation placeholders below:
    public static class SimpleIndividual implements Individual {
        private byte[] gene;
        private final int length;
        private double fitness;
        private boolean needsEvaluation = true;

        public SimpleIndividual(byte[] gene, int length) {
            this.gene = gene;
            this.length = length;
        }

        @Override public byte[] getGene() { return gene; }
        @Override public void setGene(byte[] gene) { this.gene = gene; }
        @Override public double getFitness() { return fitness; }
        @Override public void setFitness(double value) { this.fitness = value; }
        @Override public boolean needsEvaluation() { return needsEvaluation; }
        @Override public void setNeedsEvaluation(boolean value) { this.needsEvaluation = value; }
        @Override public int getGeneLength() { return length; }
        @Override public void flipBit(int index) {
            int byteIdx = index / 8, bitIdx = index % 8;
            gene[byteIdx] ^= (1 << bitIdx);
        }
        @Override public Individual clone() {
            byte[] clonedGene = gene.clone();
            SimpleIndividual clone = new SimpleIndividual(clonedGene, length);
            clone.setFitness(fitness);
            clone.setNeedsEvaluation(needsEvaluation);
            return clone;
        }
    }

    public static class SimplePopulation implements Population {
        private final List<Individual> individuals;

        public SimplePopulation(List<Individual> individuals) {
            this.individuals = individuals;
        }

        @Override public int size() { return individuals.size(); }
        @Override public Individual get(int index) { return individuals.get(index); }
        @Override public List<Individual> getAll() { return individuals; }

        @Override
        public void markAllForEvaluation() {
            for (Individual ind : individuals) ind.setNeedsEvaluation(true);
        }

        @Override
        public void swapWith(Population other) {
            if (other instanceof SimplePopulation) {
                List<Individual> tmp = new ArrayList<>(this.individuals);
                this.individuals.clear();
                this.individuals.addAll(((SimplePopulation) other).individuals);
                ((SimplePopulation) other).individuals.clear();
                ((SimplePopulation) other).individuals.addAll(tmp);
            }
        }
    }
}
