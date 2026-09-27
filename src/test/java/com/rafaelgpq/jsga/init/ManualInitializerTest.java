package com.rafaelgpq.jsga.init;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.DebugUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManualInitializerTest {

    @Test
    void initializesConfiguredGenes() {
        Population population = PopulationFactory.create(3, 5);

        new ManualInitializer(List.of("10101", "01010")).initialize(population);

        assertThat(DebugUtils.toBinaryString(population.get(0).getGene(), 5)).isEqualTo("10101");
        assertThat(DebugUtils.toBinaryString(population.get(1).getGene(), 5)).isEqualTo("01010");
        assertThat(DebugUtils.toBinaryString(population.get(2).getGene(), 5)).isEqualTo("10101");
        assertThat(population.get(0).needsEvaluation()).isTrue();
    }

    @Test
    void rejectsAnEmptyGeneListAndMismatchedPopulationLength() {
        assertThatThrownBy(() -> new ManualInitializer(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);

        Population population = PopulationFactory.create(1, 5);
        assertThatThrownBy(() -> new ManualInitializer(List.of("101")).initialize(population))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected gene length 5");
    }

    @Test
    void rejectsNullAndMalformedGenesAndDoesNotPartiallyModifyPopulation() {
        assertThatThrownBy(() -> new ManualInitializer(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ManualInitializer(List.of("10x01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid gene");

        Population population = PopulationFactory.create(2, 5);
        String firstBefore = DebugUtils.toBinaryString(population.get(0).getGene(), 5);
        assertThatThrownBy(() -> new ManualInitializer(List.of("11111", "101"))
                .initialize(population))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected gene length 5");
        assertThat(DebugUtils.toBinaryString(population.get(0).getGene(), 5)).isEqualTo(firstBefore);
    }

    @Test
    void rejectsNullAndEmptyPopulations() {
        ManualInitializer initializer = new ManualInitializer(List.of("10101"));
        assertThatThrownBy(() -> initializer.initialize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population");
        assertThatThrownBy(() -> initializer.initialize(PopulationFactory.create(0, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty population");
    }
}
