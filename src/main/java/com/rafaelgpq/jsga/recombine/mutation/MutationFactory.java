package com.rafaelgpq.jsga.recombine.mutation;

import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;

import java.util.Locale;
import java.util.Objects;

public class MutationFactory {
    public static Mutation create(String mutationType, double mutationRate, int geneLength, boolean adaptive) {
        GeneticOperatorSupport.requireProbability("Mutation rate", mutationRate);
        GeneticOperatorSupport.requireGeneLength(geneLength);
        String type = Objects.requireNonNull(mutationType, "Mutation type must not be null.")
                .trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "bitflip":
                return new BitFlipMutation(mutationRate, geneLength);
            case "swap":
                return new SwapMutation(mutationRate, geneLength);
            case "gaussian":
                return new GaussianMutation(mutationRate, geneLength);
            case "adaptivebitflip":
                return new AdaptiveBitFlipMutation(mutationRate, geneLength, adaptive);
            default:
                throw new IllegalArgumentException("Unsupported mutation type: '" + mutationType + "'.");
        }
    }
}
