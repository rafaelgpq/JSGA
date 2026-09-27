package com.rafaelgpq.jsga.util;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.Individual;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility methods to calculate population statistics for convergence checks.
 */
public class MeasureUtils {

    /**
     * Calculates the bias (dominance of the most common genotype) in the population.
     * @param population The population to analyze.
     * @return A bias value between 0.0 (no bias) and 1.0 (full bias).
     */
    public static double calculateGeneticBias(Population population) {
        Map<String, Long> genotypeCounts = population.getAll().stream()
                .collect(Collectors.groupingBy(ind -> Arrays.toString(ind.getGene()), Collectors.counting()));

        long maxCount = genotypeCounts.values().stream().max(Long::compareTo).orElse(0L);
        return (double) maxCount / population.size();
    }

    /**
     * Calculates the standard deviation (sigma) of fitness values in the population.
     * @param population The population to analyze.
     * @return The standard deviation of fitness.
     */
    public static double calculateSigma(Population population) {
        List<Individual> individuals = population.getAll();

        if (individuals.isEmpty()) {
            return 0.0;  // Avoid division by zero
        }

        double mean = individuals.stream()
                .mapToDouble(Individual::getFitness)
                .average()
                .orElse(0.0);

        double variance = individuals.stream()
                .mapToDouble(i -> Math.pow(i.getFitness() - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
