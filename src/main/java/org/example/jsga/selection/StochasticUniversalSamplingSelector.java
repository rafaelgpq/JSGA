package org.example.jsga.selection;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements Stochastic Universal Sampling (SUS) with linear scaling selection.
 */
public class StochasticUniversalSamplingSelector implements Selection {

    private final double epsilon = 1e-6;  // Small constant to ensure stability

    @Override
    public void select(Population from, Population to) {
        int popSize = from.size();
        double sum = 0;
        int validCount = 0;

        // Dynamically determine worstFitness based on population
        double maxFitness = from.getAll().stream().mapToDouble(Individual::getFitness).max().orElse(1.0);
        double worstFitness = maxFitness + epsilon;

        // Compute denominator for scaling
        for (int i = 0; i < popSize; i++) {
            double perf = from.get(i).getFitness();
            if (perf < worstFitness) {
                sum += perf;
                validCount++;
            }
        }

        double denominator = worstFitness * validCount - sum;

        // Fail-safe check
        if (validCount == 0 || denominator == 0.0) {
            throw new IllegalStateException("SUS Selection: Invalid scaling denominator. " +
                    "Check population diversity or adjust fitness evaluation.");
        }

        double factor = popSize / denominator;

        List<Integer> sampleIndices = new ArrayList<>();
        double ptr = RandomUtils.nextDouble();
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
            throw new IllegalStateException("SUS Selection: internal scaling error. Sample size mismatch.");
        }

        Collections.shuffle(sampleIndices, RandomUtils.getInstance());

        for (int i = 0; i < popSize; i++) {
            Individual selected = from.get(sampleIndices.get(i)).clone();
            selected.setNeedsEvaluation(false);
            to.set(i, selected);
        }
    }
}
