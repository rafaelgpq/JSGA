package com.rafaelgpq.jsga.track;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BestSetManagerTest {

    @Test
    void retainsBestUniqueGenotypesInAscendingFitnessOrder() {
        BestSetManager manager = new BestSetManager(2, false);
        Individual first = individual(1, 5.0);
        Individual second = individual(2, -3.0);
        Individual third = individual(3, 1.0);

        manager.trySave(first, 0, 2);
        manager.trySave(individual(1, 5.0), 1, 4);
        manager.trySave(second, 2, 6);
        manager.trySave(third, 3, 8);

        assertThat(manager.getBestSet()).hasSize(2);
        assertThat(manager.getBestSet()).extracting(entry -> entry.individual.getFitness())
                .containsExactly(-3.0, 1.0);
        assertThat(manager.getBestSet().get(0).generation).isEqualTo(2);
        assertThatThrownBy(() -> manager.getBestSet().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void supportsDuplicatesOnlyWhenRequestedAndValidatesInputs() {
        BestSetManager duplicates = new BestSetManager(2, true);
        duplicates.trySave(individual(1, 1.0), 0, 0);
        duplicates.trySave(individual(1, 1.0), 1, 1);
        assertThat(duplicates.getBestSet()).hasSize(2);

        assertThatThrownBy(() -> new BestSetManager(0, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> duplicates.trySave(null, 0, 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> duplicates.trySave(individual(2, Double.NaN), 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> duplicates.trySave(individual(2, 0.0), -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Individual individual(int gene, double fitness) {
        Individual individual = PopulationFactory.create(1, 8).get(0);
        individual.setGene(new byte[]{(byte) gene});
        individual.setFitness(fitness);
        individual.setNeedsEvaluation(false);
        return individual;
    }
}
