package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.Arrays;

/**
 * Ensures the best individual survives into the new generation.
 */
public class ElitistOperator {

    public void apply(Population oldPopulation, Population newPopulation, int bestIndex) {
        Individual elite = oldPopulation.get(bestIndex);
        byte[] eliteGene = elite.getGene();

        boolean found = false;
        for (int i = 0; i < newPopulation.size(); i++) {
            if (Arrays.equals(newPopulation.get(i).getGene(), eliteGene)) {
                found = true;
                break;
            }
        }

        if (!found) {
            Individual last = newPopulation.get(newPopulation.size() - 1);
            last.setGene(Arrays.copyOf(eliteGene, eliteGene.length));
            last.setFitness(elite.getFitness());
            last.setNeedsEvaluation(false);

            System.out.printf("[Elitist] Injected elite individual with fitness: %e\n", elite.getFitness());
        }
    }
}
