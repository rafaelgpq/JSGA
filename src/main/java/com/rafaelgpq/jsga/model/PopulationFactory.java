package com.rafaelgpq.jsga.model;

import java.util.ArrayList;
import java.util.List;

public class PopulationFactory {
    public static Population create(int size, int geneLength) {
        if (size < 0) {
            throw new IllegalArgumentException("Population size must not be negative.");
        }
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        List<Individual> individuals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            byte[] gene = new byte[geneLength / 8 + (geneLength % 8 == 0 ? 0 : 1)];
            individuals.add(new SimpleIndividual(gene, geneLength));
        }
        return new SimplePopulation(individuals);
    }

    public static Population copy(Population original) {
        List<Individual> clones = new ArrayList<>(original.size());
        for (Individual ind : original.getAll()) {
            clones.add(ind.clone());
        }
        return new SimplePopulation(clones);
    }
}
