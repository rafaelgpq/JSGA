package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Performs selection based on Baker's stochastic universal sampling algorithm.
 */
public class SelectionOperator {

    private final Random random = new Random();
    private final double worstFitness;

    public SelectionOperator(double worstFitness) {
        this.worstFitness = worstFitness;
    }

    public void select(Population from, Population to) {
        int popSize = from.size();
        double sum = 0;
        int validCount = 0;

        // Compute denominator for scaling
        for (int i = 0; i < popSize; i++) {
            double perf = from.get(i).getFitness();
            if (perf < worstFitness) {
                sum += perf;
                validCount++;
            }
        }

        double factor = popSize / (worstFitness * validCount - sum);

        List<Integer> sampleIndices = new ArrayList<>();
        double ptr = random.nextDouble();
        double acc = 0;

        for (int i = 0; i < popSize; i++) {
            double perf = from.get(i).getFitness();
            double expected = perf < worstFitness ? (worstFitness - perf) * factor : 0.0;
            acc += expected;
            while (acc > ptr) {
                sampleIndices.add(i);
                ptr += 1.0;
            }
        }

        if (sampleIndices.size() != popSize) {
            throw new IllegalStateException("Select: internal scaling error");
        }

        // Shuffle selected indices
        Collections.shuffle(sampleIndices, random);

        // Copy selected individuals to new population
        for (int i = 0; i < popSize; i++) {
            Individual selected = from.get(sampleIndices.get(i));
            Individual clone = selected.clone();
            clone.setNeedsEvaluation(false);
            to.get(i).setGene(clone.getGene());
            to.get(i).setFitness(clone.getFitness());
            to.get(i).setNeedsEvaluation(clone.needsEvaluation());
        }
    }
}

// NOTE: Assumes Individual has clone(), getFitness(), setFitness(), and gene methods.
