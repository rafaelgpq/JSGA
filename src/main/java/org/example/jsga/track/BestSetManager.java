package org.example.jsga.track;

import org.example.jsga.model.Individual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages a ranked archive of best-performing individuals.
 */
public class BestSetManager {

    private final int savesize;
    private final boolean allowDuplicates;
    private final List<TrackedIndividual> bestSet = new ArrayList<>();

    public BestSetManager(int savesize, boolean allowDuplicates) {
        this.savesize = savesize;
        this.allowDuplicates = allowDuplicates;
    }

    public void trySave(Individual candidate, int generation, long trialCount) {
        if (!allowDuplicates) {
            for (TrackedIndividual t : bestSet) {
                if (sameGene(candidate, t.individual)) return;
            }
        }

        if (bestSet.size() < savesize) {
            bestSet.add(new TrackedIndividual(candidate.clone(), generation, trialCount));
        } else {
            TrackedIndividual worst = bestSet.stream()
                    .max(Comparator.comparingDouble(t -> t.individual.getFitness()))
                    .orElse(null);
            if (worst != null && candidate.getFitness() < worst.individual.getFitness()) {
                bestSet.remove(worst);
                bestSet.add(new TrackedIndividual(candidate.clone(), generation, trialCount));
            }
        }
    }

    public List<TrackedIndividual> getBestSet() {
        return bestSet;
    }

    private boolean sameGene(Individual a, Individual b) {
        byte[] g1 = a.getGene();
        byte[] g2 = b.getGene();
        if (g1.length != g2.length) return false;
        for (int i = 0; i < g1.length; i++) {
            if (g1[i] != g2[i]) return false;
        }
        return true;
    }

    public static class TrackedIndividual {
        public final Individual individual;
        public final int generation;
        public final long trialNumber;

        public TrackedIndividual(Individual individual, int generation, long trialNumber) {
            this.individual = individual;
            this.generation = generation;
            this.trialNumber = trialNumber;
        }
    }
}
