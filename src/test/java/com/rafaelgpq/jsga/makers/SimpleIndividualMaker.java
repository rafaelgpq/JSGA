package com.rafaelgpq.jsga.makers;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Maker;
import com.natpryce.makeiteasy.Property;
import com.rafaelgpq.jsga.model.SimpleIndividual;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;

public class SimpleIndividualMaker {
    private static final Property<SimpleIndividual, byte[]> GENE = Property.newProperty();
    private static final Property<SimpleIndividual, Integer> GENE_LENGTH = Property.newProperty();
    private static final Property<SimpleIndividual, Double> FITNESS = Property.newProperty();
    private static final Property<SimpleIndividual, Boolean> NEEDS_EVALUATION = Property.newProperty();

    public static final Instantiator<SimpleIndividual> INSTANTIATOR = lookup -> {
        SimpleIndividual individual = new SimpleIndividual();
        individual.setGene(lookup.valueOf(GENE, (byte[]) null));
        individual.setGeneLength(lookup.valueOf(GENE_LENGTH, (Integer) null));
        individual.setFitness(lookup.valueOf(FITNESS, (Double) null));
        individual.setNeedsEvaluation(lookup.valueOf(NEEDS_EVALUATION, (Boolean) null));
        return individual;
    };

    public static final Maker<SimpleIndividual> VALID_SIMPLE_INDIVIDUAL = a(INSTANTIATOR);

    private SimpleIndividualMaker() {}

    public static SimpleIndividual makeSimpleIndividual(String chromosome, double fitness) {
        return VALID_SIMPLE_INDIVIDUAL.but(
                with(GENE, toByteArray(chromosome)),
                with(GENE_LENGTH, chromosome.length()),
                with(FITNESS, fitness),
                with(NEEDS_EVALUATION, false)
        ).make();
    }

    private static byte[] toByteArray(String binaryString) {
        byte result = 0;
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                result |= (1 << i);
            }
        }
        return new byte[] { result };
    }

    public static SimpleIndividual buildSimpleIndividualByByteArray(byte[] gene) {
        return SimpleIndividualMaker.VALID_SIMPLE_INDIVIDUAL
                .but(with(GENE, gene),
                        with(GENE_LENGTH, gene.length * 8),
                        with(FITNESS, 0.0),
                        with(NEEDS_EVALUATION, false)
                ).make();
    }
}