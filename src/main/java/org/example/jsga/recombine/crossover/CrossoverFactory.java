package org.example.jsga.recombine.crossover;

public class CrossoverFactory {
    public static Crossover create(String crossoverType, int geneLength) {
        switch (crossoverType.toLowerCase()) {
            case "onepoint":
                return new OnePointCrossover(geneLength);
            case "twopoint":
                return new TwoPointCrossover(geneLength);
            case "uniform":
                return new UniformCrossover(geneLength);
            // Placeholder for future: PartiallyMatchedCrossover, OrderCrossover, etc.
            default:
                System.out.println("[JSGA][Warning] Unknown crossover type: " + crossoverType + ". Defaulting to OnePointCrossover.");
                return new OnePointCrossover(geneLength);
        }
    }
}
