package com.rafaelgpq.jsga.problem;

/**
 * A minimization objective evaluated from a packed logical-bit genome.
 */
public interface Problem {
    double evaluate(byte[] gene, int geneLength);
}
