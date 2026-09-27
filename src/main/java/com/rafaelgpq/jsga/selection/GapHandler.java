package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds a generational target from selected offspring and surviving parents.
 */
public class GapHandler {

    /**
     * Replaces the truncated requested fraction of the population and carries the remainder forward.
     *
     * @return the number of leading target slots containing selected offspring
     */
    public int applyGap(Population parents, Population selected, double gapSize) {
        Objects.requireNonNull(parents, "Parent population must not be null.");
        Objects.requireNonNull(selected, "Selected population must not be null.");
        requireGapSize(gapSize);
        if (parents == selected) {
            throw new IllegalArgumentException("Parent and selected populations must be different instances.");
        }
        if (parents.size() != selected.size()) {
            throw new IllegalArgumentException("Parent and selected populations must have equal sizes.");
        }
        int populationSize = parents.size();
        if (populationSize == 0) {
            throw new IllegalArgumentException("Gap handling requires a non-empty population.");
        }
        int geneLength = validatePopulation(parents, "Parent");
        validatePopulation(selected, "Selected", geneLength);
        if (gapSize == 1.0) {
            for (int i = 0; i < populationSize; i++) {
                selected.set(i, selected.get(i).clone());
            }
            return populationSize;
        }
        int replacementCount = replacementCount(gapSize, populationSize);
        replacementCount = Math.min(replacementCount, populationSize);

        List<Integer> selectedIndices = shuffledIndices(populationSize);
        List<Integer> survivorIndices = shuffledIndices(populationSize);
        List<Individual> nextGeneration = new ArrayList<>(populationSize);
        for (int i = 0; i < replacementCount; i++) {
            nextGeneration.add(selected.get(selectedIndices.get(i)).clone());
        }
        for (int i = 0; i < populationSize - replacementCount; i++) {
            nextGeneration.add(parents.get(survivorIndices.get(i)).clone());
        }
        selected.setIndividuals(nextGeneration);
        return replacementCount;
    }

    /**
     * Replaces the unselected tail of an index sample with a random permutation of survivors.
     */
    public void applyGap(int[] sample, double gapSize, int populationSize) {
        Objects.requireNonNull(sample, "Sample must not be null.");
        requireGapSize(gapSize);
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Population size must be greater than zero.");
        }
        if (sample.length < populationSize) {
            throw new IllegalArgumentException("Sample must contain at least populationSize entries.");
        }
        List<Integer> sampleOrder = shuffledIndices(populationSize);
        int[] selected = new int[populationSize];
        for (int i = 0; i < populationSize; i++) {
            selected[i] = sample[sampleOrder.get(i)];
        }
        List<Integer> survivors = shuffledIndices(populationSize);
        int replacementCount = replacementCount(gapSize, populationSize);
        for (int i = replacementCount; i < populationSize; i++) {
            selected[i] = survivors.get(i);
        }
        System.arraycopy(selected, 0, sample, 0, populationSize);
    }

    private static List<Integer> shuffledIndices(int size) {
        List<Integer> indices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }
        for (int i = size - 1; i > 0; i--) {
            int j = RandomUtils.nextInt(i + 1);
            int value = indices.get(i);
            indices.set(i, indices.get(j));
            indices.set(j, value);
        }
        return indices;
    }

    private static void requireGapSize(double gapSize) {
        if (!Double.isFinite(gapSize) || gapSize < 0.0 || gapSize > 1.0) {
            throw new IllegalArgumentException("Gap size must be finite and between 0 and 1.");
        }
    }

    private static int replacementCount(double gapSize, int populationSize) {
        return (int) (gapSize * populationSize);
    }

    private static int validatePopulation(Population population, String name) {
        Individual first = Objects.requireNonNull(population.get(0), name + " individual at index 0 must not be null.");
        int geneLength = first.getGeneLength();
        if (geneLength <= 0 || first.getGene() == null
                || first.getGene().length != (geneLength + Byte.SIZE - 1) / Byte.SIZE) {
            throw new IllegalArgumentException(name + " population has an invalid genome at index 0.");
        }
        validateIndividual(first, geneLength, name, 0);
        for (int i = 1; i < population.size(); i++) {
            validateIndividual(population.get(i), geneLength, name, i);
        }
        return geneLength;
    }

    private static void validatePopulation(Population population, String name, int expectedGeneLength) {
        for (int i = 0; i < population.size(); i++) {
            validateIndividual(population.get(i), expectedGeneLength, name, i);
        }
    }

    private static void validateIndividual(Individual individual, int geneLength, String name, int index) {
        if (individual == null || individual.getGeneLength() != geneLength || individual.getGene() == null
                || individual.getGene().length != (geneLength + Byte.SIZE - 1) / Byte.SIZE
                || !Double.isFinite(individual.getFitness()) || !Double.isFinite(individual.getRawFitness())) {
            throw new IllegalArgumentException(name + " individual at index " + index
                    + " has invalid genome or fitness state.");
        }
    }
}
