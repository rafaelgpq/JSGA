package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;

import java.util.List;
import java.util.Objects;

/**
 * Adjusts minimization fitness using Hamming-distance niche sharing.
 */
public class FitnessSharingStrategy implements DiversityStrategy {
    private final double nicheRadius;

    public FitnessSharingStrategy(double nicheRadius) {
        if (!Double.isFinite(nicheRadius) || nicheRadius <= 0.0) {
            throw new IllegalArgumentException("Niche radius must be finite and greater than zero.");
        }
        this.nicheRadius = nicheRadius;
    }

    @Override
    public void apply(Population nextGen, int generation) {
        Objects.requireNonNull(nextGen, "Population must not be null.");
        List<Individual> individuals = nextGen.getAll();
        if (individuals.isEmpty()) {
            throw new IllegalArgumentException("Fitness sharing requires a non-empty population.");
        }
        int geneLength = individuals.get(0).getGeneLength();
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        for (int i = 0; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            if (individual == null || individual.getGeneLength() != geneLength
                    || individual.getGene() == null || individual.getGene().length != (geneLength + 7) / 8
                    || !Double.isFinite(individual.getRawFitness())) {
                throw new IllegalArgumentException("Population individual at index " + i + " is invalid.");
            }
        }

        double[] adjustedFitness = new double[individuals.size()];
        double maximumFitness = Double.NEGATIVE_INFINITY;
        double fitnessScale = 0.0;
        double minimumFitness = Double.POSITIVE_INFINITY;
        for (Individual individual : individuals) {
            maximumFitness = Math.max(maximumFitness, individual.getRawFitness());
            minimumFitness = Math.min(minimumFitness, individual.getRawFitness());
            fitnessScale = Math.max(fitnessScale, Math.abs(individual.getRawFitness()));
        }
        double scaledMaximum = fitnessScale == 0.0 ? 0.0 : maximumFitness / fitnessScale;
        double scaledMinimum = fitnessScale == 0.0 ? 0.0 : minimumFitness / fitnessScale;
        for (int i = 0; i < individuals.size(); i++) {
            Individual individual = individuals.get(i);
            double sharingSum = 0.0;
            for (Individual other : individuals) {
                int distance = hammingDistance(individual, other, geneLength);
                if (distance < nicheRadius) {
                    sharingSum += 1.0 - distance / nicheRadius;
                }
            }
            double scaledFitness = fitnessScale == 0.0 ? 0.0 : individual.getRawFitness() / fitnessScale;
            double adjustedScaledFitness = scaledMaximum
                    - (scaledMaximum - scaledFitness) / sharingSum;
            adjustedScaledFitness = Math.max(scaledMinimum, Math.min(scaledMaximum, adjustedScaledFitness));
            adjustedFitness[i] = adjustedScaledFitness * fitnessScale;
            if (fitnessScale == 0.0) adjustedFitness[i] = 0.0;
            if (!Double.isFinite(adjustedFitness[i])) {
                throw new IllegalArgumentException("Adjusted fitness values must be finite.");
            }
        }
        for (int i = 0; i < individuals.size(); i++) {
            individuals.get(i).setFitness(adjustedFitness[i]);
        }
    }

    private int hammingDistance(Individual first, Individual second, int geneLength) {
        int distance = 0;
        for (int bit = 0; bit < geneLength; bit++) {
            if (((first.getGene()[bit / 8] ^ second.getGene()[bit / 8]) & (1 << (bit % 8))) != 0) {
                distance++;
            }
        }
        return distance;
    }
}
