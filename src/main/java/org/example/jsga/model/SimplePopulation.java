package org.example.jsga.model;

import java.util.ArrayList;
import java.util.List;

public class SimplePopulation implements Population {
    private List<Individual> individuals;

    public SimplePopulation(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals); // Defensive copy
    }

    public SimplePopulation() {
        this.individuals = new ArrayList<>(); // Initialize as mutable list
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
    public void markAllForEvaluation() {
        for (Individual ind : individuals) {
            ind.setNeedsEvaluation(true);
        }
    }

    @Override
    public void swapWith(Population other) {
        List<Individual> temp = this.individuals;
        this.individuals = ((SimplePopulation) other).individuals;
        ((SimplePopulation) other).individuals = temp;
    }

    @Override
    public List<Individual> getAll() {
        return individuals;
    }

    @Override
    public void setIndividuals(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals);
    }
}
