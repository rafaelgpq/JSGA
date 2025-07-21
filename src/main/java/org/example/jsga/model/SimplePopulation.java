package org.example.jsga.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimplePopulation implements Population {
    private List<Individual> individuals;

    public SimplePopulation(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals);
    }

    public SimplePopulation() {
        this.individuals = new ArrayList<>();
    }

    @Override
    public int size() { return individuals.size(); }

    @Override
    public Individual get(int index) { return individuals.get(index); }

    @Override
    public void set(int index, Individual individual) {
        individuals.set(index, individual);
    }

    @Override
    public void markAllForEvaluation() {
        for (Individual ind : individuals) ind.setNeedsEvaluation(true);
    }

    @Override
    public void swapWith(Population other) {
        if (other instanceof SimplePopulation) {
            List<Individual> temp = this.individuals;
            this.individuals = ((SimplePopulation) other).individuals;
            ((SimplePopulation) other).individuals = temp;
        } else {
            throw new IllegalArgumentException("Population type mismatch during swap.");
        }
    }

    @Override
    public List<Individual> getAll() { return individuals; }

    @Override
    public void setIndividuals(List<Individual> individuals) {
        this.individuals = new ArrayList<>(individuals);
    }

    @Override
    public Individual getBestIndividual() {
        return individuals.stream()
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElseThrow(() -> new IllegalStateException("Population is empty!"));
    }
}
