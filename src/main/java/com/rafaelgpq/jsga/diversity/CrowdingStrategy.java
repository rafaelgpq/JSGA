package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Replaces members too close to the population's bitwise-majority genome.
 */
public class CrowdingStrategy implements DiversityStrategy {
    private final int geneLength;
    private final double similarityThreshold;

    public CrowdingStrategy(int geneLength, double similarityThreshold) {
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        if (!Double.isFinite(similarityThreshold) || similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("Similarity threshold must be finite and between 0 and 1.");
        }
        this.geneLength = geneLength;
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        Objects.requireNonNull(nextGen, "Population must not be null.");
        if (nextGen.size() == 0 || similarityThreshold == 0.0) {
            return;
        }

        List<Individual> individuals = new ArrayList<>(nextGen.getAll());
        for (int i = 0; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            if (individual == null || individual.getGeneLength() != geneLength
                    || individual.getGene() == null
                    || individual.getGene().length != (geneLength + 7) / 8) {
                throw new IllegalArgumentException("Population individual at index " + i + " is invalid.");
            }
        }

        byte[] consensus = computeConsensus(individuals);
        boolean[] replace = new boolean[individuals.size()];
        int replacementCount = 0;
        int bestIndex = 0;
        for (int i = 0; i < individuals.size(); i++) {
            if (!Double.isFinite(individuals.get(i).getFitness())) {
                throw new IllegalArgumentException("Population fitness values must be finite.");
            }
            if (individuals.get(i).getFitness() < individuals.get(bestIndex).getFitness()) {
                bestIndex = i;
            }
            replace[i] = hammingDistance(individuals.get(i).getGene(), consensus)
                    < similarityThreshold * geneLength;
            if (replace[i]) {
                replacementCount++;
            }
        }
        if (replacementCount == individuals.size()) {
            replace[bestIndex] = false;
        }
        for (int i = 0; i < replace.length; i++) {
            if (replace[i]) {
                nextGen.set(i, randomIndividual());
            }
        }
    }

    private byte[] computeConsensus(List<Individual> individuals) {
        byte[] consensus = new byte[(geneLength + 7) / 8];
        for (int bit = 0; bit < geneLength; bit++) {
            int oneCount = 0;
            for (Individual individual : individuals) {
                if ((individual.getGene()[bit / 8] & (1 << (bit % 8))) != 0) {
                    oneCount++;
                }
            }
            if (oneCount > individuals.size() / 2) {
                consensus[bit / 8] |= (byte) (1 << (bit % 8));
            }
        }
        return consensus;
    }

    private int hammingDistance(byte[] gene, byte[] consensus) {
        int distance = 0;
        for (int bit = 0; bit < geneLength; bit++) {
            if (((gene[bit / 8] ^ consensus[bit / 8]) & (1 << (bit % 8))) != 0) {
                distance++;
            }
        }
        return distance;
    }

    private Individual randomIndividual() {
        Individual individual = PopulationFactory.create(1, geneLength).get(0);
        byte[] gene = individual.getGene();
        for (int i = 0; i < gene.length; i++) {
            gene[i] = (byte) RandomUtils.nextInt(256);
        }
        int usedBits = geneLength % Byte.SIZE;
        if (usedBits != 0) {
            gene[gene.length - 1] &= (byte) ((1 << usedBits) - 1);
        }
        individual.setNeedsEvaluation(true);
        return individual;
    }
}
