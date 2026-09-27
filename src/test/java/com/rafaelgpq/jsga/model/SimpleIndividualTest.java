package com.rafaelgpq.jsga.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleIndividualTest {

    @Test
    void storesFieldsFlipsLogicalBitsAndClonesAllState() {
        SimpleIndividual individual = new SimpleIndividual(new byte[]{0b00000101, 0b00000001}, 9);
        individual.setFitness(-3.5);
        individual.setRawFitness(-4.0);
        individual.setNeedsEvaluation(false);

        individual.flipBit(1);
        individual.flipBit(8);

        assertThat(individual.getGene()).containsExactly((byte) 0b00000111, (byte) 0);
        assertThat(individual.getGeneLength()).isEqualTo(9);
        assertThat(individual.toString()).contains("chromosome: |111000000|").contains("geneLength=9");
        Individual clone = individual.clone();
        assertThat(clone).isNotSameAs(individual);
        assertThat(clone.getGene()).containsExactly(individual.getGene());
        assertThat(clone.getGene()).isNotSameAs(individual.getGene());
        assertThat(clone.getFitness()).isEqualTo(-3.5);
        assertThat(clone.getRawFitness()).isEqualTo(-4.0);
        assertThat(clone.needsEvaluation()).isFalse();
        clone.getGene()[0] = 0;
        assertThat(individual.getGene()[0]).isEqualTo((byte) 0b00000111);
    }

    @Test
    void emptyIndividualHasEmptyGenomeAndSettersUpdateState() {
        SimpleIndividual individual = new SimpleIndividual();
        assertThat(individual.getGene()).isEmpty();
        assertThat(individual.getGeneLength()).isZero();
        assertThat(individual.needsEvaluation()).isTrue();
        individual.setGene(new byte[]{1});
        individual.setGeneLength(1);
        individual.setNeedsEvaluation(false);
        assertThat(individual.getGene()).containsExactly((byte) 1);
        assertThat(individual.getGeneLength()).isEqualTo(1);
        assertThat(individual.needsEvaluation()).isFalse();
    }

    @Test
    void rejectsNegativeAndOutOfRangeLogicalBitIndices() {
        SimpleIndividual individual = new SimpleIndividual(new byte[]{0}, 8);
        assertThatThrownBy(() -> individual.flipBit(8)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> individual.flipBit(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> new SimpleIndividual(new byte[]{0}, 7).flipBit(7))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }
}
