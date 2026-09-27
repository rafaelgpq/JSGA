package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.util.Objects;

final class SelectionSupport {

    private SelectionSupport() {
    }

    static boolean isEmptyTarget(Population from, Population to) {
        Objects.requireNonNull(from, "Source population must not be null.");
        Objects.requireNonNull(to, "Target population must not be null.");
        if (from == to) {
            throw new IllegalArgumentException("Source and target populations must be different instances.");
        }
        if (to.size() == 0) {
            return true;
        }
        if (from.size() == 0) {
            throw new IllegalArgumentException("Source population must not be empty.");
        }
        for (int i = 0; i < from.size(); i++) {
            Individual individual = from.get(i);
            if (individual == null || !Double.isFinite(individual.getFitness())) {
                throw new IllegalArgumentException("Selection requires non-null individuals with finite fitness.");
            }
        }
        return false;
    }

    static void copySelected(Individual individual, Population target, int targetIndex) {
        target.set(targetIndex, individual.clone());
    }
}
