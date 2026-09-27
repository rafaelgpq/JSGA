package com.rafaelgpq.jsga.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PopulationFactoryTest {

    @Test
    void createsRequestedPopulationAndGeneDimensions() {
        Population population = PopulationFactory.create(3, 9);

        assertThat(population.size()).isEqualTo(3);
        for (int i = 0; i < population.size(); i++) {
            assertThat(population.get(i).getGeneLength()).isEqualTo(9);
            assertThat(population.get(i).getGene()).hasSize(2);
            assertThat(population.get(i).needsEvaluation()).isTrue();
        }
    }

    @Test
    void rejectsNegativePopulationSizeAndNonPositiveGeneLength() {
        assertThatThrownBy(() -> PopulationFactory.create(-1, 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population size");
        assertThatThrownBy(() -> PopulationFactory.create(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene length");
        assertThatThrownBy(() -> PopulationFactory.create(1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene length");
    }

    @Test
    void copiesIndividualsWithoutSharingMutableGenomeState() {
        Population source = PopulationFactory.create(2, 9);
        source.get(0).setGene(new byte[]{1, 1});
        source.get(0).setFitness(-2.0);
        source.get(0).setRawFitness(-2.0);
        source.get(0).setNeedsEvaluation(false);

        Population copy = PopulationFactory.copy(source);
        copy.get(0).setGene(new byte[]{0, 0});
        copy.get(0).setFitness(0.0);

        assertThat(copy).isNotSameAs(source);
        assertThat(copy.get(0)).isNotSameAs(source.get(0));
        assertThat(copy.get(0).getGene()).containsExactly((byte) 0, (byte) 0);
        assertThat(source.get(0).getGene()).containsExactly((byte) 1, (byte) 1);
        assertThat(source.get(0).getFitness()).isEqualTo(-2.0);
        assertThat(source.get(0).needsEvaluation()).isFalse();
    }
}
