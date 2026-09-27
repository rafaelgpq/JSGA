package com.rafaelgpq.jsga.problem;

import com.rafaelgpq.jsga.util.DecodeUtils;

/**
 * Minimization objective that returns the negative number of logical one bits.
 */
public class MaxOnesProblem implements Problem {

    @Override
    public double evaluate(byte[] gene, int geneLength) {
        if (gene == null || geneLength <= 0) {
            throw new IllegalArgumentException("Genome must be non-null and have positive length.");
        }
        boolean[] bits = DecodeUtils.unpack(gene, geneLength);
        int ones = 0;
        for (boolean bit : bits) {
            if (bit) ones++;
        }
        return -ones;
    }
}
