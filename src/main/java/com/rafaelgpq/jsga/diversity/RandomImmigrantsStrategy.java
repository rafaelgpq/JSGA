package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Diversity strategy that injects random individuals into the next generation.
 */
public class RandomImmigrantsStrategy implements DiversityStrategy {
    private final int count;
    private final int geneLength;

    public RandomImmigrantsStrategy(int count, int geneLength) {
        if (count < 0) {
            throw new IllegalArgumentException("Immigrant count must not be negative.");
        }
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        this.count = count;
        this.geneLength = geneLength;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        if (nextGen == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        List<Individual> individuals = nextGen.getAll();
        if (count > individuals.size()) {
            throw new IllegalArgumentException("Immigrant count must not exceed population size.");
        }
        List<Integer> indices = new ArrayList<>(individuals.size());
        for (int i = 0; i < individuals.size(); i++) {
            if (individuals.get(i) == null || individuals.get(i).getGeneLength() != geneLength) {
                throw new IllegalArgumentException("Population individuals must match the configured gene length.");
            }
            indices.add(i);
        }
        for (int i = indices.size() - 1; i > 0; i--) {
            int selected = RandomUtils.nextInt(i + 1);
            int temp = indices.get(i);
            indices.set(i, indices.get(selected));
            indices.set(selected, temp);
        }
        for (int i = 0; i < count; i++) {
            int index = indices.get(i);
            Individual randomIndividual = PopulationFactory.create(1, geneLength).get(0);
            byte[] gene = randomIndividual.getGene();
            for (int byteIndex = 0; byteIndex < gene.length; byteIndex++) {
                gene[byteIndex] = (byte) RandomUtils.nextInt(256);
            }
            int usedBits = geneLength % Byte.SIZE;
            if (usedBits != 0) {
                gene[gene.length - 1] &= (byte) ((1 << usedBits) - 1);
            }
            randomIndividual.setNeedsEvaluation(true);
            individuals.set(index, randomIndividual);
        }
    }
}
