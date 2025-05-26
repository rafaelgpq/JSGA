package org.example.jsga.model;

import java.util.ArrayList;
import java.util.List;

public class PopulationFactory {
    public static Population create(int size, int geneLength) {
        List<Individual> individuals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            byte[] gene = new byte[(geneLength + 7) / 8];
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
