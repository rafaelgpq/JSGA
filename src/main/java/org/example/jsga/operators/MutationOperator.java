package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.Random;

/**
 * Performs mutation on a given population using a bitwise mutation strategy.
 * This is a direct translation of GAucsd's mutate.c.
 */
public class MutationOperator {

    private final double mutationRate;
    private final int chromosomeLength;
    private final int populationSize;
    private long muNext = 0;
    private boolean firstCall = true;

    private final Random random = new Random();

    public MutationOperator(double mutationRate, int chromosomeLength, int populationSize) {
        this.mutationRate = mutationRate;
        this.chromosomeLength = chromosomeLength;
        this.populationSize = populationSize;
    }

    public void mutate(Population population) {
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
                    r = random.nextDouble();
                } while (r == 0.0);

                muNext += (long) Math.ceil(Math.log(r) / Math.log(1.0 - mutationRate));
            } else {
                muNext += 1;
            }
        }

        muNext -= totalBits;
    }

    // You may wish to reset muNext or seed the random generator in tests
}