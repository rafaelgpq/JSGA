package com.rafaelgpq.jsga.elitism;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Ensures the best individual survives into the new generation.
 */
public class ElitismHandler {

    private final boolean enabled;

    public ElitismHandler(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void apply(Population oldPopulation, Population newPopulation) {
        if (!enabled) return;
        Objects.requireNonNull(oldPopulation, "Old population must not be null.");
        Objects.requireNonNull(newPopulation, "New population must not be null.");
        if (oldPopulation.size() == 0 || newPopulation.size() == 0) {
            throw new IllegalArgumentException("Elitism requires non-empty old and new populations.");
        }

        Individual elite = oldPopulation.getBestIndividual();
        byte[] eliteGene = elite.getGene();

        int matchingEliteIndex = -1;
        for (int i = 0; i < newPopulation.size(); i++) {
            if (Arrays.equals(newPopulation.get(i).getGene(), eliteGene)) {
                matchingEliteIndex = i;
                break;
            }
        }

        if (matchingEliteIndex >= 0) {
            if (newPopulation.get(matchingEliteIndex).getFitness() > elite.getFitness()) {
                newPopulation.set(matchingEliteIndex, copyElite(elite));
            }
            return;
        }

        int worstIndex = IntStream.range(0, newPopulation.size())
                .boxed().max(Comparator.comparingDouble(index -> newPopulation.get(index).getFitness()))
                .orElseThrow(() -> new IllegalStateException("New population is unexpectedly empty."));
        newPopulation.set(worstIndex, copyElite(elite));
        System.out.printf("[Elitist] Injected elite individual with fitness: %e\n", elite.getFitness());
    }

    private Individual copyElite(Individual elite) {
        Individual eliteCopy = elite.clone();
        eliteCopy.setGene(Arrays.copyOf(elite.getGene(), elite.getGene().length));
        eliteCopy.setNeedsEvaluation(false);
        return eliteCopy;
    }
}
