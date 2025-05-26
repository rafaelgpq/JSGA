package org.example.jsga.diversity;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.ArrayList;
import java.util.List;

/**
 * Diversity strategy that implements deterministic crowding.
 * It replaces similar individuals with randomly generated ones to preserve diversity.
 */
public class CrowdingStrategy implements DiversityStrategy {
    private final int geneLength;
    private double similarityThreshold;

    public CrowdingStrategy(int geneLength, double similarityThreshold) {
        this.geneLength = geneLength;
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        List<Individual> individuals = nextGen.getAll();
        List<Individual> newIndividuals = new ArrayList<>(individuals.size());

        for (Individual ind : individuals) {
            if (shouldReplace(ind, nextGen)) {
                Individual randomInd = generateRandomIndividual();
                newIndividuals.add(randomInd);
                System.out.println("[JSGA][Diversity][Crowding] Replaced similar individual with random in generation " + generation);
            } else {
                newIndividuals.add(ind);
            }
        }

        nextGen.setIndividuals(newIndividuals);
    }

    private boolean shouldReplace(Individual ind, Population population) {
        byte[] individualGene = ind.getGene();
        byte[] averageGene = computeAverageGene(population);
        int hammingDistance = computeHammingDistance(individualGene, averageGene);

        // Define threshold: if too similar (less than 30% difference), replace
        double similarityThreshold = 0.3 * ind.getGeneLength();
        boolean replace = hammingDistance < similarityThreshold * ind.getGeneLength();

        if (replace) {
            System.out.println("[JSGA][Crowding] Replacing individual due to low diversity. Hamming distance: " + hammingDistance);
        }

        return replace;
    }

    private byte[] computeAverageGene(Population population) {
        int geneLength = population.get(0).getGeneLength();
        byte[] averageGene = new byte[(geneLength + 7) / 8];
        int totalIndividuals = population.size();

        for (Individual ind : population.getAll()) {
            byte[] gene = ind.getGene();
            for (int i = 0; i < averageGene.length; i++) {
                averageGene[i] |= gene[i]; // OR operation to accumulate bits
            }
        }

        // Convert to majority bits (simple threshold, optional for binary chromosomes)
        for (int i = 0; i < averageGene.length; i++) {
            byte bit = 0;
            for (int j = 0; j < 8; j++) {
                int bitCount = 0;
                for (Individual ind : population.getAll()) {
                    if ((ind.getGene()[i] & (1 << j)) != 0) bitCount++;
                }
                if (bitCount > totalIndividuals / 2) {
                    bit |= (1 << j);
                }
            }
            averageGene[i] = bit;
        }

        return averageGene;
    }

    private int computeHammingDistance(byte[] gene1, byte[] gene2) {
        int distance = 0;
        for (int i = 0; i < gene1.length; i++) {
            byte xor = (byte) (gene1[i] ^ gene2[i]);
            distance += Integer.bitCount(xor & 0xFF);
        }
        return distance;
    }

    private Individual generateRandomIndividual() {
        return org.example.jsga.model.PopulationFactory.create(1, geneLength).getAll().get(0);
    }
}
