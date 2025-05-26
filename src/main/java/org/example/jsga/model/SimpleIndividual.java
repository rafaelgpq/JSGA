package org.example.jsga.model;

import java.util.Arrays;

/**
 * Basic implementation of the Individual interface using a packed byte array for genes.
 */
public class SimpleIndividual implements Individual {

    private byte[] gene;
    private int geneLength;
    private double fitness = 0.0;
    private boolean needsEvaluation = true;

    public SimpleIndividual(byte[] gene, int geneLength) {
        this.gene = gene;
        this.geneLength = geneLength;
    }

    public SimpleIndividual() {
        this(new byte[0], 0);
    }

    @Override
    public byte[] getGene() {
        return gene;
    }

    @Override
    public void setGene(byte[] gene) {
        this.gene = gene;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void setFitness(double value) {
        this.fitness = value;
    }

    @Override
    public boolean needsEvaluation() {
        return needsEvaluation;
    }

    @Override
    public void setNeedsEvaluation(boolean value) {
        this.needsEvaluation = value;
    }

    @Override
    public int getGeneLength() {
        return geneLength;
    }

    public void setGeneLength(int length) {
        this.geneLength = length;
    }

    @Override
    public void flipBit(int index) {
        int byteIndex = index / 8;
        int bitIndex = index % 8;
        gene[byteIndex] ^= (1 << bitIndex);
    }

    @Override
    public Individual clone() {
        byte[] clonedGene = Arrays.copyOf(gene, gene.length);
        SimpleIndividual copy = new SimpleIndividual(clonedGene, geneLength);
        copy.setFitness(this.fitness);
        copy.setNeedsEvaluation(this.needsEvaluation);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int totalBits = 0;
        for (byte b : gene) {
            for (int i = 0; i < 8 && totalBits < geneLength; i++) {
                sb.append((b >> i & 1) == 1 ? '1' : '0');
                totalBits++;
            }
        }
        return "[RGP][SimpleIndividual][toString()] chromosome: |" + sb + "| geneLength=" + geneLength;
    }


}
