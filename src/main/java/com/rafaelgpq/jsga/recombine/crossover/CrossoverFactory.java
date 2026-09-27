package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;

import java.util.Locale;
import java.util.Objects;

public class CrossoverFactory {
    public static Crossover create(String crossoverType, int geneLength) {
        GeneticOperatorSupport.requireGeneLength(geneLength);
        String type = Objects.requireNonNull(crossoverType, "Crossover type must not be null.")
                .trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "onepoint":
                return new OnePointCrossover(geneLength);
            case "twopoint":
                return new TwoPointCrossover(geneLength);
            case "uniform":
                return new UniformCrossover(geneLength);
            default:
                throw new IllegalArgumentException("Unsupported crossover type: '" + crossoverType + "'.");
        }
    }
}
