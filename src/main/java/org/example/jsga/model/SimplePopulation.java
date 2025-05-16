package org.example.jsga.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic implementation of the Population interface using a List of Individuals.
 */
public class SimplePopulation implements Population {

    private final List<Individual> individuals;

    public SimplePopulation(int size, int geneLength) {
        this.individuals = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            individuals.add(new SimpleIndividual(new byte[(geneLength + 7) / 8], geneLength));
        }
    }

    public SimplePopulation(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals);
    }

    @Override
    public int size() {
        return individuals.size();
    }

    @Override
    public Individual get(int index) {
        return individuals.get(index);
    }

    @Override
    public List<Individual> getAll() {
        return Collections.unmodifiableList(individuals);
    }

    @Override
    public void markAllForEvaluation() {
        for (Individual ind : individuals) {
            ind.setNeedsEvaluation(true);
        }
    }

    @Override
    public void swapWith(Population other) {
        if (other instanceof SimplePopulation) {
            SimplePopulation that = (SimplePopulation) other;
            List<Individual> tmp = new ArrayList<>(this.individuals);
            this.individuals.clear();
            this.individuals.addAll(that.individuals);
            that.individuals.clear();
            that.individuals.addAll(tmp);
        }
    }
}

