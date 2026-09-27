package com.rafaelgpq.jsga.recombine;

import com.rafaelgpq.jsga.model.Individual;

import java.util.Arrays;
import java.util.Objects;

/**
 * Shared validation and offspring handling for binary genetic operators.
 */
public final class GeneticOperatorSupport {

    private GeneticOperatorSupport() {
    }

    public static int requireGeneLength(int geneLength) {
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        return geneLength;
    }

    public static double requireProbability(String name, double probability) {
        if (!Double.isFinite(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(name + " must be finite and between 0 and 1.");
        }
        return probability;
    }

    public static byte[] copyGene(Individual individual, int expectedGeneLength) {
        Objects.requireNonNull(individual, "Individual must not be null.");
        requireGeneLength(expectedGeneLength);
        if (individual.getGeneLength() != expectedGeneLength) {
            throw new IllegalArgumentException("Individual gene length " + individual.getGeneLength()
                    + " does not match configured gene length " + expectedGeneLength + ".");
        }
        byte[] gene = individual.getGene();
        if (gene == null) {
            throw new IllegalArgumentException("Individual gene must not be null.");
        }
        int expectedBytes = expectedGeneLength / 8 + (expectedGeneLength % 8 == 0 ? 0 : 1);
        if (gene.length != expectedBytes) {
            throw new IllegalArgumentException("Individual gene requires " + expectedBytes
                    + " bytes for length " + expectedGeneLength + "; found " + gene.length + ".");
        }
        byte[] copy = Arrays.copyOf(gene, gene.length);
        clearPaddingBits(copy, expectedGeneLength);
        return copy;
    }

    public static Individual offspring(Individual parent, byte[] gene) {
        Individual child = parent.clone();
        byte[] childGene = Arrays.copyOf(gene, gene.length);
        child.setGene(childGene);
        child.setNeedsEvaluation(parent.needsEvaluation() || !Arrays.equals(parent.getGene(), childGene));
        return child;
    }

    public static void clearPaddingBits(byte[] gene, int geneLength) {
        int usedBitsInLastByte = geneLength % 8;
        if (usedBitsInLastByte != 0) {
            gene[gene.length - 1] &= (byte) ((1 << usedBitsInLastByte) - 1);
        }
    }
}
