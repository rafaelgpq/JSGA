package com.rafaelgpq.jsga.model;

/**
 * Represents an individual in the genetic population.
 */
public interface Individual {
    byte[] getGene();
    void setGene(byte[] gene);

    double getFitness();
    void setFitness(double value);

    double getRawFitness();
    void setRawFitness(double value);

    boolean needsEvaluation();
    void setNeedsEvaluation(boolean value);

    int getGeneLength();
    void flipBit(int bitIndex);

    Individual clone();
}
