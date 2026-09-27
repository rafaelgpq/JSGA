package com.rafaelgpq.jsga.init;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.model.SimpleIndividual;
import com.rafaelgpq.jsga.util.DebugUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RandomInitializerTest {

    @Test
    void initializationIsRepeatableAndClearsUnusedPaddingBits() {
        Population first = PopulationFactory.create(8, 10);
        Population second = PopulationFactory.create(8, 10);

        new RandomInitializer(17L).initialize(first);
        new RandomInitializer(17L).initialize(second);

        for (int i = 0; i < first.size(); i++) {
            assertThat(DebugUtils.toBinaryString(first.get(i).getGene(), 10))
                    .isEqualTo(DebugUtils.toBinaryString(second.get(i).getGene(), 10));
            assertThat(first.get(i).getGene()[1] & 0xFC).isZero();
            assertThat(first.get(i).needsEvaluation()).isTrue();
        }
    }

    @Test
    void producesExactlyTheRequestedNumberOfBitsForAlignedAndUnalignedLengths() {
        for (int geneLength : new int[]{1, 7, 8, 9, 16, 17}) {
            Population population = PopulationFactory.create(3, geneLength);
            new RandomInitializer(17L).initialize(population);

            for (int i = 0; i < population.size(); i++) {
                assertThat(population.get(i).getGene()).hasSize((geneLength + 7) / 8);
                assertThat(DebugUtils.toBinaryString(population.get(i).getGene(), geneLength))
                        .hasSize(geneLength);
                int slopBits = geneLength % 8;
                if (slopBits != 0) {
                    int paddingMask = 0xFF << slopBits;
                    assertThat(population.get(i).getGene()[
                            population.get(i).getGene().length - 1] & paddingMask).isZero();
                }
            }
        }
    }

    @Test
    void rejectsNullInputsAndInvalidIndividualGeneLength() {
        RandomInitializer initializer = new RandomInitializer(1L);
        assertThatThrownBy(() -> initializer.initialize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population");
        assertThatThrownBy(() -> new RandomInitializer((java.util.Random) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Random");

        Population invalidPopulation = PopulationFactory.create(1, 1);
        ((SimpleIndividual) invalidPopulation.get(0)).setGeneLength(0);
        assertThatThrownBy(() -> initializer.initialize(invalidPopulation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene length");
    }
}
