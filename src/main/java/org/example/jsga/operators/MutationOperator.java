package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

/**
 * Performs mutation on a given population using a bitwise mutation strategy,
 * with optional adaptive mutation.
 * This extends GAucsd's mutate.c logic.
 */
public class MutationOperator {

    private final double baseMutationRate;
    private final int chromosomeLength;
    private final int populationSize;
    private long muNext = 0;
    private boolean firstCall = true;

    private boolean adaptiveEnabled;
    private double adaptiveFactor = 1.5; // Multiplier for adaptive adjustment

    public MutationOperator(double baseMutationRate, int chromosomeLength, int populationSize) {
        this(baseMutationRate, chromosomeLength, populationSize, false);
    }

    public MutationOperator(double baseMutationRate, int chromosomeLength, int populationSize, boolean adaptiveEnabled) {
        this.baseMutationRate = baseMutationRate;
        this.chromosomeLength = chromosomeLength;
        this.populationSize = populationSize;
        this.adaptiveEnabled = adaptiveEnabled;
    }

    public void mutate(Population population, int generation, int maxGenerations) {
        double mutationRate = baseMutationRate;

        if (adaptiveEnabled) {
            // Adjust mutation rate based on progress through generations
            double progress = (double) generation / maxGenerations;
            mutationRate = baseMutationRate * (1 + adaptiveFactor * progress);
        }

        long totalBits = (long) populationSize * chromosomeLength;

        if (firstCall) {
            muNext = 0;
            firstCall = false;
        }

        if (mutationRate <= 0.0) return;

        while (muNext < totalBits) {
            int individualIndex = (int) (muNext / chromosomeLength);
            int bitIndex = (int) (muNext % chromosomeLength);

            Individual individual = population.get(individualIndex);
            individual.flipBit(bitIndex);
            individual.setNeedsEvaluation(true);

            if (mutationRate < 1.0) {
                double r;
                do {
                    r = RandomUtils.rand();
                } while (r == 0.0);

                muNext += (long) Math.ceil(Math.log(r) / Math.log(1.0 - mutationRate));
            } else {
                muNext += 1;
            }
        }

        muNext -= totalBits;
    }

    // Optional: Reset for new runs
    public void reset() {
        muNext = 0;
        firstCall = true;
    }
}
