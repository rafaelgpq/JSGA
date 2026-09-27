package com.rafaelgpq.jsga.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SimplePopulationTest {

    @Test
    void bestIndividualIsTheMinimumFitnessRegardlessOfPopulationOrder() {
        Population population = PopulationFactory.create(3, 8);
        population.get(0).setFitness(2.0);
        population.get(1).setFitness(-6.0);
        population.get(2).setFitness(-1.0);

        assertThat(population.getBestIndividual()).isSameAs(population.get(1));
    }

    @Test
    void bestIndividualRejectsAnEmptyPopulation() {
        assertThatThrownBy(() -> PopulationFactory.create(0, 8).getBestIndividual())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void mutatesAndExposesTheBackingIndividualsListAndMarksThemDirty() {
        Population population = PopulationFactory.create(2, 8);
        population.get(0).setNeedsEvaluation(false);
        population.get(1).setNeedsEvaluation(false);
        population.markAllForEvaluation();
        assertThat(population.getAll()).allMatch(individual -> individual.needsEvaluation());

        Individual replacement = PopulationFactory.create(1, 8).get(0);
        population.set(0, replacement);
        assertThat(population.get(0)).isSameAs(replacement);
        population.getAll().remove(1);
        assertThat(population.size()).isEqualTo(1);
    }

    @Test
    void swapsBackingStorageWithAnotherSimplePopulationAndRejectsOtherImplementations() {
        Population first = PopulationFactory.create(1, 8);
        Population second = PopulationFactory.create(2, 8);
        Individual originalFirst = first.get(0);
        Individual originalSecond = second.get(0);

        first.swapWith(second);

        assertThat(first.size()).isEqualTo(2);
        assertThat(first.get(0)).isSameAs(originalSecond);
        assertThat(second.size()).isEqualTo(1);
        assertThat(second.get(0)).isSameAs(originalFirst);
        assertThatThrownBy(() -> first.swapWith(mock(Population.class)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("type mismatch");
    }

    @Test
    void setIndividualsCopiesTheSuppliedListAndAllowsEmptyPopulation() {
        Population population = PopulationFactory.create(1, 8);
        List<Individual> individuals = new ArrayList<>();
        individuals.add(PopulationFactory.create(1, 8).get(0));

        population.setIndividuals(individuals);
        individuals.clear();
        assertThat(population.size()).isEqualTo(1);

        population.setIndividuals(List.of());
        assertThat(population.size()).isZero();
    }
}
