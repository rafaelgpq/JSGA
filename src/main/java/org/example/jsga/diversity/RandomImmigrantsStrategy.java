package org.example.jsga.diversity;

import org.example.jsga.model.Population;
import org.example.jsga.model.PopulationFactory;
import org.example.jsga.model.Individual;
import org.example.jsga.util.RandomUtils;

import java.util.List;

/**
 * Diversity strategy that injects random individuals into the next generation.
 */
public class RandomImmigrantsStrategy implements DiversityStrategy {
    private final int count;
    private final int geneLength;

    public RandomImmigrantsStrategy(int count, int geneLength) {
        this.count = count;
        this.geneLength = geneLength;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        List<Individual> individuals = nextGen.getAll();
        for (int i = 0; i < count; i++) {
            int index = RandomUtils.nextInt(individuals.size());
            Individual randomIndividual = PopulationFactory.create(geneLength, geneLength).get(0); // Creating one random individual
            individuals.set(index, randomIndividual);
            System.out.println("[JSGA][Diversity][RandomImmigrants] Injected at index " + index + " in generation " + generation);
        }
        nextGen.setIndividuals(individuals);
    }
}
