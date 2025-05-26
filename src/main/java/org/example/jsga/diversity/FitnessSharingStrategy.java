package org.example.jsga.diversity;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.List;

/**
 * Diversity strategy that adjusts fitness values using Fitness Sharing.
 */
public class FitnessSharingStrategy implements DiversityStrategy {
    private final double nicheRadius;

    public FitnessSharingStrategy(double nicheRadius) {
        this.nicheRadius = nicheRadius;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        List<Individual> individuals = nextGen.getAll();

        for (Individual ind : individuals) {
            double sharingSum = 0.0;
            for (Individual other : individuals) {
                int hammingDist = computeHammingDistance(ind.getGene(), other.getGene());
                sharingSum += sharingFunction(hammingDist);
            }
            if (sharingSum > 0) {
                double adjustedFitness = ind.getFitness() / sharingSum;
                ind.setFitness(adjustedFitness);
                System.out.println("[JSGA][Diversity][FitnessSharing] Adjusted fitness for individual: " + adjustedFitness);
            }
        }
    }

    private double sharingFunction(int distance) {
        double alpha = 1.0; // You can configure this as needed
        if (distance < nicheRadius) {
            return 1.0 - Math.pow(distance / nicheRadius, alpha);
        } else {
            return 0.0;
        }
    }

    private int computeHammingDistance(byte[] gene1, byte[] gene2) {
        int distance = 0;
        for (int i = 0; i < gene1.length; i++) {
            byte xor = (byte) (gene1[i] ^ gene2[i]);
            distance += Integer.bitCount(xor & 0xFF);
        }
        return distance;
    }
}
