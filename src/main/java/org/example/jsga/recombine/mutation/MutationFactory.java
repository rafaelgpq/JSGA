package org.example.jsga.recombine.mutation;

public class MutationFactory {
    public static Mutation create(String mutationType, double mutationRate, int geneLength, int populationSize, boolean adaptive) {
        switch (mutationType.toLowerCase()) {
            case "bitflip":
                return new BitFlipMutation(mutationRate, geneLength, adaptive);
            case "swap":
                return new SwapMutation(mutationRate, geneLength);
            case "gaussian":
                return new GaussianMutation(mutationRate, geneLength);
            case "adaptivebitflip":
                return new AdaptiveBitFlipMutation(mutationRate, geneLength, adaptive);
            default:
                System.out.println("[JSGA][Warning] Unknown mutation type: " + mutationType + ". Defaulting to BitFlipMutation.");
                return new BitFlipMutation(mutationRate, geneLength, adaptive);
        }
    }
}
