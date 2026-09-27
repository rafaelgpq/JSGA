package com.rafaelgpq.jsga.init;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.EncodeUtils;
import com.rafaelgpq.jsga.util.GeneStringParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes a population using predefined gene strings (e.g., for testing or controlled experiments).
 */
public class ManualInitializer implements PopulationInitializer {

    private final List<String> geneStrings;

    /**
     * Constructs a manual initializer with a list of binary strings.
     * Each string must consist only of 0s and 1s.
     */
    public ManualInitializer(List<String> geneStrings) {
        if (geneStrings == null || geneStrings.isEmpty()) {
            throw new IllegalArgumentException("At least one gene string is required.");
        }
        this.geneStrings = new ArrayList<>(geneStrings);
        for (String geneString : this.geneStrings) {
            if (geneString == null || !GeneStringParser.isValidGeneString(geneString)) {
                throw new IllegalArgumentException("[ManualInitializer] Invalid gene string: " + geneString);
            }
        }
    }

    @Override
    public void initialize(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Population must not be null.");
        }
        if (population.size() == 0) {
            throw new IllegalArgumentException("Cannot initialize an empty population.");
        }
        int geneLength = population.get(0).getGeneLength();
        if (geneLength <= 0) {
            throw new IllegalArgumentException("Gene length must be greater than zero.");
        }
        int full = geneLength / 8;
        int slop = geneLength % 8;

        for (String gene : geneStrings) {
            if (gene.length() != geneLength) {
                throw new IllegalArgumentException("[ManualInitializer] Expected gene length " + geneLength
                        + " but got " + gene.length() + ": " + gene);
            }
        }

        for (int i = 0; i < population.size(); i++) {
            String binary = geneStrings.get(i % geneStrings.size());

            boolean[] unpacked = GeneStringParser.parseBinaryString(binary);
            byte[] packed = EncodeUtils.pack(unpacked, full, slop);

            Individual individual = population.get(i);
            individual.setGene(packed);
            individual.setNeedsEvaluation(true);
        }
    }
}
