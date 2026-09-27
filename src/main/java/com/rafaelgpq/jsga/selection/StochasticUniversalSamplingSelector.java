package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.RandomUtils;

/**
 * Implements Stochastic Universal Sampling (SUS) with linear scaling selection.
 */
public class StochasticUniversalSamplingSelector implements Selection {

    @Override
    public void select(Population from, Population to) {
        if (SelectionSupport.isEmptyTarget(from, to)) return;
        double[] probabilities = SelectionWeights.forMinimization(from);
        double pointer = RandomUtils.nextDouble() / to.size();
        double step = 1.0 / to.size();
        double cumulative = probabilities[0];
        int sourceIndex = 0;

        for (int targetIndex = 0; targetIndex < to.size(); targetIndex++) {
            double selectionPoint = pointer + targetIndex * step;
            while (selectionPoint >= cumulative && sourceIndex < probabilities.length - 1) {
                cumulative += probabilities[++sourceIndex];
            }
            SelectionSupport.copySelected(from.get(sourceIndex), to, targetIndex);
        }
    }
}
