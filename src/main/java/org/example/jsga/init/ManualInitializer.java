package org.example.jsga.init;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.EncodeUtils;
import org.example.jsga.util.GeneStringParser;

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
        this.geneStrings = geneStrings;
    }

    @Override
    public void initialize(Population population) {
        int geneLength = population.get(0).getGeneLength();
        int full = geneLength / 8;
        int slop = geneLength % 8;

        for (int i = 0; i < population.size(); i++) {
            String binary = geneStrings.get(i % geneStrings.size());

            if (!GeneStringParser.isValidGeneString(binary)) {
                throw new IllegalArgumentException("[ManualInitializer] Invalid gene string: " + binary);
            }
            if (binary.length() != geneLength) {
                throw new IllegalArgumentException("[ManualInitializer] Expected gene length " + geneLength
                        + " but got " + binary.length() + ": " + binary);
            }

            boolean[] unpacked = GeneStringParser.parseBinaryString(binary);
            byte[] packed = EncodeUtils.pack(unpacked, full, slop);

            Individual individual = population.get(i);
            individual.setGene(packed);
            individual.setNeedsEvaluation(true);
        }
    }
}
