package com.rafaelgpq.jsga.problem;

/**
 * An objective whose phenotype is a vector of decoded real-valued parameters.
 */
public interface ParameterizedProblem extends Problem {

    double evaluateParameters(double[] parameters);

    @Override
    default double evaluate(byte[] gene, int geneLength) {
        throw new IllegalStateException("A parameterized problem requires DPE parameter decoding.");
    }
}
