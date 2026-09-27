package com.rafaelgpq.jsga.elitism;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
class ElitismHandlerTest {

    @Test
    void preservesLowestFitnessIndividualRegardlessOfItsPositionAndReplacesWorstChild() {
        Population parents = PopulationFactory.create(2, 8);
        setIndividual(parents.get(0), 4, 10.0);
        setIndividual(parents.get(1), 9, -3.0);
        Population children = PopulationFactory.create(2, 8);
        setIndividual(children.get(0), 1, -1.0);
        setIndividual(children.get(1), 2, 7.0);

        new ElitismHandler(true).apply(parents, children);

        assertThat(children.get(1).getFitness()).isEqualTo(-3.0);
        assertThat(children.get(1).getGene()).containsExactly((byte) 9);
        assertThat(children.get(1).needsEvaluation()).isFalse();
        assertThat(children.get(1)).isNotSameAs(parents.get(1));
    }

    @Test
    void doesNotReplaceExistingEliteOrModifyPopulationWhenDisabled() {
        Population parents = PopulationFactory.create(1, 8);
        setIndividual(parents.get(0), 9, -3.0);
        Population hasElite = PopulationFactory.create(1, 8);
        setIndividual(hasElite.get(0), 9, -3.0);
        new ElitismHandler(true).apply(parents, hasElite);
        assertThat(hasElite.get(0).getFitness()).isEqualTo(-3.0);

        Population children = PopulationFactory.create(1, 8);
        setIndividual(children.get(0), 1, 1.0);
        new ElitismHandler(false).apply(parents, children);
        assertThat(children.get(0).getGene()).containsExactly((byte) 1);
        assertThat(children.get(0).getFitness()).isEqualTo(1.0);
    }

    @Test
    void restoresEliteFitnessWhenMatchingGenomeHasWorseAdjustedFitness() {
        Population parents = PopulationFactory.create(1, 8);
        setIndividual(parents.get(0), 9, -3.0);
        Population children = PopulationFactory.create(1, 8);
        setIndividual(children.get(0), 9, -1.0);

        new ElitismHandler(true).apply(parents, children);

        assertThat(children.get(0).getFitness()).isEqualTo(-3.0);
        assertThat(children.get(0).getGene()).containsExactly((byte) 9);
        assertThat(children.get(0)).isNotSameAs(parents.get(0));
    }

    @Test
    void rejectsNullAndEmptyPopulationsWhenEnabled() {
        ElitismHandler handler = new ElitismHandler(true);
        assertThatThrownBy(() -> handler.apply(null, PopulationFactory.create(1, 8)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.apply(PopulationFactory.create(0, 8), PopulationFactory.create(1, 8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> handler.apply(PopulationFactory.create(1, 8), PopulationFactory.create(0, 8)))
                .isInstanceOf(IllegalArgumentException.class);

        new ElitismHandler(false).apply(null, null);
    }

    private static void setIndividual(Individual individual, int gene, double fitness) {
        individual.setGene(new byte[]{(byte) gene});
        individual.setFitness(fitness);
        individual.setNeedsEvaluation(false);
    }
}
