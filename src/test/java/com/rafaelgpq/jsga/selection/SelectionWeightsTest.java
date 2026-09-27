package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectionWeightsTest {

    @Test
    void validatesSourcePopulationAndFitness() {
        assertThatThrownBy(() -> SelectionWeights.forMinimization(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        assertThatThrownBy(() -> SelectionWeights.forMinimization(PopulationFactory.create(0, 8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        Population withNullIndividual = PopulationFactory.create(1, 8);
        withNullIndividual.set(0, null);
        assertThatThrownBy(() -> SelectionWeights.forMinimization(withNullIndividual))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
    }

    @Test
    void producesNormalizedWeightsForZeroTiedAndExtremeFitnessValues() {
        Population zeroFitness = populationWithFitness(0.0, 0.0);
        assertThat(SelectionWeights.forMinimization(zeroFitness)).containsExactly(0.5, 0.5);

        Population tiedFitness = populationWithFitness(-3.0, -3.0, -3.0);
        assertThat(SelectionWeights.forMinimization(tiedFitness)).containsExactly(1.0 / 3.0,
                1.0 / 3.0, 1.0 / 3.0);

        Population extremeFitness = populationWithFitness(-Double.MAX_VALUE, 0.0, Double.MAX_VALUE);
        double[] weights = SelectionWeights.forMinimization(extremeFitness);
        assertThat(weights[0]).isGreaterThan(weights[1]);
        assertThat(weights[1]).isGreaterThan(weights[2]);
        assertThat(java.util.Arrays.stream(weights).allMatch(Double::isFinite)).isTrue();
        assertThat(weights[0] + weights[1] + weights[2]).isCloseTo(1.0,
                org.assertj.core.data.Offset.offset(1e-12));
    }

    private static Population populationWithFitness(double... values) {
        Population population = PopulationFactory.create(values.length, 8);
        for (int i = 0; i < values.length; i++) {
            population.get(i).setFitness(values[i]);
        }
        return population;
    }
}
