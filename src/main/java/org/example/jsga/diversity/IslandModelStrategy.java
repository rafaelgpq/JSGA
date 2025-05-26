package org.example.jsga.diversity;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Diversity strategy that implements the Island Model.
 */
public class IslandModelStrategy implements DiversityStrategy {
    private final int numIslands;
    private final int migrationInterval;
    private final int migrationSize;
    private final int geneLength;
    private int generationCounter = 0;
    private final List<Population> islands;

    public IslandModelStrategy(int numIslands, int migrationInterval, int migrationSize, int geneLength, int popSize) {
        this.numIslands = numIslands;
        this.migrationInterval = migrationInterval;
        this.migrationSize = migrationSize;
        this.geneLength = geneLength;

        islands = new ArrayList<>();
        // Properly initialize islands with empty mutable populations
        for (int i = 0; i < numIslands; i++) {
            islands.add(PopulationFactory.create(0, geneLength));
        }
    }

    @Override
    public void apply(Population nextGen, int generation) {
        generationCounter++;

        // Clear islands before re-adding individuals
        for (Population island : islands) {
            island.setIndividuals(new ArrayList<>());
        }

        // Distribute nextGen individuals across islands
        List<Individual> individuals = nextGen.getAll();
        int index = 0;
        for (Individual ind : individuals) {
            islands.get(index % numIslands).getAll().add(ind.clone());
            index++;
        }

        // Perform migration at intervals
        if (generationCounter % migrationInterval == 0) {
            migrate();
        }

        // Merge islands back into nextGen
        List<Individual> combined = new ArrayList<>();
        for (Population island : islands) {
            combined.addAll(island.getAll());
        }
        nextGen.setIndividuals(combined);
    }

    private void migrate() {
        for (int i = 0; i < numIslands; i++) {
            Population source = islands.get(i);
            Population destination = islands.get((i + 1) % numIslands);

            List<Individual> bestMigrants = new ArrayList<>(source.getAll());
            bestMigrants.sort((a, b) -> Double.compare(a.getFitness(), b.getFitness()));
            List<Individual> migrants = bestMigrants.subList(0, Math.min(migrationSize, bestMigrants.size()));

            List<Individual> destIndividuals = destination.getAll();
            destIndividuals.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            for (int j = 0; j < Math.min(migrationSize, destIndividuals.size()); j++) {
                destIndividuals.set(j, migrants.get(j).clone());
            }
        }
        System.out.println("[JSGA][IslandModel] Migration performed.");
    }
}
