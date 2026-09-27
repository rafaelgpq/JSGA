package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class FitnessWeightedSelectorTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(81L);
    }

    @Test
    void rouletteSelectionHandlesNegativeFitnessAndFavorsMinimum() {
        Population parents = parentsWithFitness(-10.0, -2.0);
        Population children = PopulationFactory.create(500, 8);

        new RouletteSelector().select(parents, children);

        assertThat(children.getAll()).allMatch(individual ->
                individual.getFitness() == -10.0 || individual.getFitness() == -2.0);
        assertThat(children.getAll().stream().filter(individual -> individual.getFitness() == -10.0).count())
                .isGreaterThan(490);
        assertThat(children.get(0)).isNotSameAs(parents.get(0));
    }

    @Test
    void susSelectionHandlesNegativeFitnessAndFavorsMinimum() {
        Population parents = parentsWithFitness(-10.0, -2.0);
        Population children = PopulationFactory.create(500, 8);

        new StochasticUniversalSamplingSelector().select(parents, children);

        assertThat(children.getAll()).allMatch(individual ->
                individual.getFitness() == -10.0 || individual.getFitness() == -2.0);
        assertThat(children.getAll().stream().filter(individual -> individual.getFitness() == -10.0).count())
                .isGreaterThan(490);
    }

    @Test
    void weightedSelectorsSampleUniformlyWhenFitnessesAreTied() {
        Population parents = parentsWithFitness(-3.0, -3.0);
        Population children = PopulationFactory.create(20, 8);

        new RouletteSelector().select(parents, children);
        assertThat(children.getAll()).extracting(individual -> individual.getGene()[0])
                .containsOnly((byte) 1, (byte) 2);

        RandomUtils.initialize(81L);
        new StochasticUniversalSamplingSelector().select(parents, children);
        assertThat(children.getAll()).extracting(individual -> individual.getGene()[0])
                .containsOnly((byte) 1, (byte) 2);
        assertThat(children.getAll().stream().filter(individual -> individual.getGene()[0] == 1).count())
                .isEqualTo(10);
    }

    @Test
    void rankAndTournamentSelectionPreferTheMinimumFitness() {
        Population parents = parentsWithFitness(-10.0, -2.0);
        Population children = PopulationFactory.create(500, 8);

        new RankSelector().select(parents, children);
        assertThat(children.getAll().stream().filter(individual -> individual.getFitness() == -10.0).count())
                .isBetween(300L, 375L);

        children = PopulationFactory.create(100, 8);
        new TournamentSelector(1.0).select(parents, children);
        assertThat(children.getAll()).allMatch(individual ->
                individual.getFitness() == -10.0);
    }

    @Test
    void everySelectorRejectsInvalidPoolsAndCopiesSelections() {
        List<Selection> selectors = List.of(new TournamentSelector(0.5), new RankSelector(),
                new RouletteSelector(), new StochasticUniversalSamplingSelector());
        Population valid = parentsWithFitness(-10.0, -2.0);

        for (Selection selector : selectors) {
            Population destination = PopulationFactory.create(2, 8);
            selector.select(valid, destination);
            assertThat(destination.getAll()).allMatch(individual ->
                    individual != valid.get(0) && individual != valid.get(1));
            assertThatThrownBy(() -> selector.select(valid, valid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("different instances");
            assertThatThrownBy(() -> selector.select(PopulationFactory.create(0, 8),
                    PopulationFactory.create(1, 8)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
            selector.select(valid, PopulationFactory.create(0, 8));

            Population nonFiniteFitness = parentsWithFitness(0.0, Double.POSITIVE_INFINITY);
            assertThatThrownBy(() -> selector.select(nonFiniteFitness, destination))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("finite");
        }

        assertThatThrownBy(() -> new TournamentSelector(0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TournamentSelector(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TournamentSelector(1.01))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SelectionFactory.create("unknown", 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported selection");
        assertThatThrownBy(() -> SelectionFactory.create("tournament", 1.01))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyOrNonFiniteSourceFitness() {
        assertThatThrownBy(() -> new RouletteSelector().select(
                PopulationFactory.create(0, 8), PopulationFactory.create(1, 8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        assertThatThrownBy(() -> new StochasticUniversalSamplingSelector().select(
                PopulationFactory.create(0, 8), PopulationFactory.create(1, 8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");

        Population invalid = parentsWithFitness(Double.NaN, 1.0);
        assertThatThrownBy(() -> new RouletteSelector().select(invalid, PopulationFactory.create(1, 8)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() -> new RouletteSelector().select(invalid, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target");
    }

    private static Population parentsWithFitness(double firstFitness, double secondFitness) {
        Population population = PopulationFactory.create(2, 8);
        population.get(0).setGene(new byte[]{1});
        population.get(0).setFitness(firstFitness);
        population.get(0).setNeedsEvaluation(false);
        population.get(1).setGene(new byte[]{2});
        population.get(1).setFitness(secondFitness);
        population.get(1).setNeedsEvaluation(false);
        return population;
    }
}
