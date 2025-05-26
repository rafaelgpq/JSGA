package org.example.jsga.selection;

import org.example.jsga.util.RandomUtils;

/**
 * Handles population replacement when Gapsize < 1.0.
 */
public class GapHandler {

    /**
     * Adjusts the sample array based on the gap size.
     * @param sample array of indices of selected individuals
     * @param gapsize fraction of population to be replaced
     * @param popSize total population size
     */
    public void applyGap(int[] sample, double gapsize, int popSize) {
        // Shuffle sample[]
        for (int i = 0; i < popSize; i++) {
            int j = i + RandomUtils.randint(popSize - i);
            int temp = sample[i];
            sample[i] = sample[j];
            sample[j] = temp;
        }

        // Generate uniform survivor indices
        int[] survivors = new int[popSize];
        for (int i = 0; i < popSize; i++) {
            survivors[i] = i;
        }
        for (int i = 0; i < popSize; i++) {
            int j = i + RandomUtils.randint(popSize - i);
            int temp = survivors[i];
            survivors[i] = survivors[j];
            survivors[j] = temp;
        }

        // Fill in tail end of sample[] with survivor indices
        for (int i = (int) (gapsize * popSize); i < popSize; i++) {
            sample[i] = survivors[i];
        }
    }
}
