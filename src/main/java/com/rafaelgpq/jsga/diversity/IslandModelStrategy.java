package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Partitions a generation into islands and periodically migrates elites in a ring.
 */
public class IslandModelStrategy implements DiversityStrategy {
    private final int numIslands;
    private final int migrationInterval;
    private final int migrationSize;
    private final int geneLength;
    private final int populationSize;

    public IslandModelStrategy(int numIslands, int migrationInterval, int migrationSize, int geneLength, int popSize) {
        if (numIslands <= 0 || migrationInterval <= 0 || migrationSize < 0
                || geneLength <= 0 || popSize <= 0) {
            throw new IllegalArgumentException("Island settings must have positive sizes and a non-negative migration size.");
        }
        if (migrationSize > popSize) {
            throw new IllegalArgumentException("Migration size must not exceed population size.");
        }
        this.numIslands = numIslands;
        this.migrationInterval = migrationInterval;
        this.migrationSize = migrationSize;
        this.geneLength = geneLength;
        this.populationSize = popSize;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        Objects.requireNonNull(nextGen, "Population must not be null.");
        if (generation < 0) {
            throw new IllegalArgumentException("Generation must not be negative.");
        }
        if (nextGen.size() != populationSize) {
            throw new IllegalArgumentException("Population size does not match the configured island model.");
        }

        List<List<Individual>> islands = new ArrayList<>(numIslands);
        for (int i = 0; i < numIslands; i++) {
            islands.add(new ArrayList<>());
        }
        for (int i = 0; i < populationSize; i++) {
            Individual individual = nextGen.get(i);
            if (individual == null || individual.getGeneLength() != geneLength
                    || individual.getGene() == null
                    || individual.getGene().length != (geneLength + 7) / 8
                    || !Double.isFinite(individual.getFitness())) {
                throw new IllegalArgumentException("Population individual at index " + i + " is invalid.");
            }
            islands.get(i % numIslands).add(individual.clone());
        }

        if ((generation + 1) % migrationInterval == 0 && migrationSize > 0 && numIslands > 1) {
            migrate(islands);
        }

        List<Individual> merged = new ArrayList<>(populationSize);
        for (List<Individual> island : islands) {
            merged.addAll(island);
        }
        if (merged.size() != populationSize) {
            throw new IllegalStateException("Island partitioning changed the population size.");
        }
        nextGen.setIndividuals(merged);
    }

    private void migrate(List<List<Individual>> islands) {
        List<List<Individual>> migrants = new ArrayList<>(numIslands);
        for (List<Individual> island : islands) {
            List<Individual> best = new ArrayList<>(island);
            best.sort(Comparator.comparingDouble(Individual::getFitness));
            migrants.add(new ArrayList<>(best.subList(0, Math.min(migrationSize, best.size()))));
        }

        for (int sourceIndex = 0; sourceIndex < numIslands; sourceIndex++) {
            List<Individual> destination = islands.get((sourceIndex + 1) % numIslands);
            List<Integer> worstIndices = new ArrayList<>(destination.size());
            for (int i = 0; i < destination.size(); i++) {
                worstIndices.add(i);
            }
            worstIndices.sort((left, right) -> Double.compare(
                    destination.get(right).getFitness(), destination.get(left).getFitness()));
            List<Individual> incoming = migrants.get(sourceIndex);
            int count = Math.min(incoming.size(), worstIndices.size());
            for (int i = 0; i < count; i++) {
                destination.set(worstIndices.get(i), incoming.get(i).clone());
            }
        }
    }
}
