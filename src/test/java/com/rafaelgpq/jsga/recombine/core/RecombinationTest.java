package com.rafaelgpq.jsga.recombine.core;

import com.rafaelgpq.jsga.core.Recombination;
import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.recombine.crossover.OnePointCrossover;
import com.rafaelgpq.jsga.recombine.mutation.BitFlipMutation;
import com.rafaelgpq.jsga.util.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class RecombinationTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(17L);
    }

    @Test
    void configuredOperatorsInvalidateChangedOffspringAndPreserveParents() {
        Population population = PopulationFactory.create(3, 9);
        setIndividual(population.get(0), new byte[]{0, 0});
        setIndividual(population.get(1), new byte[]{(byte) 0xff, 1});
        setIndividual(population.get(2), new byte[]{0x55, 1});
        Individual[] originalIndividuals = population.getAll().toArray(new Individual[0]);
        byte[][] originalGenes = new byte[population.size()][];
        for (int i = 0; i < population.size(); i++) {
            originalGenes[i] = population.get(i).getGene().clone();
        }

        Recombination recombination = new Recombination();
        recombination.configure((parent1, parent2) -> {
            parent1.flipBit(0);
            parent2.flipBit(0);
            return new Individual[]{parent1, parent2};
        }, 1.0, new BitFlipMutation(0.0, 9));
        recombination.apply(population, 0, 10);

        assertThat(population.getAll()).doesNotContain(originalIndividuals);
        for (int i = 0; i < population.size(); i++) {
            assertThat(originalIndividuals[i].getGene()).containsExactly(originalGenes[i]);
            assertThat(population.get(i).getGene()[1] & 0b11111110).isZero();
        }
        assertThat(population.getAll().stream().filter(Individual::needsEvaluation).count()).isEqualTo(2);
    }

    @Test
    void requiresConfigurationAndRejectsInvalidOperatorSettings() {
        Recombination unconfigured = new Recombination();
        assertThatThrownBy(() -> unconfigured.apply(PopulationFactory.create(1, 8), 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("configured");
        assertThatThrownBy(() -> unconfigured.apply(null, 0, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Population");

        assertThatThrownBy(() -> unconfigured.configure(null, 0.5, individual -> individual))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Crossover");
        assertThatThrownBy(() -> unconfigured.configure(new OnePointCrossover(8), Double.POSITIVE_INFINITY,
                individual -> individual))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Crossover rate");

        Recombination configured = new Recombination();
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        AtomicInteger retainedConfigurationCalls = new AtomicInteger();
        AtomicInteger rejectedConfigurationCalls = new AtomicInteger();
        configured.configure((parent1, parent2) -> {
            retainedConfigurationCalls.incrementAndGet();
            return new Individual[]{parent1.clone(), parent2.clone()};
        }, 1.0, Individual::clone);
        assertThatThrownBy(() -> configured.configure((parent1, parent2) -> {
            rejectedConfigurationCalls.incrementAndGet();
            return new Individual[]{parent1.clone(), parent2.clone()};
        }, 1.0, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Mutation");
        configured.apply(population, 0, 1);
        assertThat(retainedConfigurationCalls).hasValue(1);
        assertThat(rejectedConfigurationCalls).hasValue(0);
    }

    @Test
    void zeroCrossoverRateDoesNotInvokeCrossoverOrInvalidateUnchangedIndividuals() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        AtomicInteger calls = new AtomicInteger();
        Recombination recombination = new Recombination();
        recombination.configure((parent1, parent2) -> {
            calls.incrementAndGet();
            return new Individual[]{parent1.clone(), parent2.clone()};
        }, 0.0, individual -> individual.clone());

        recombination.apply(population, 0, 1);

        assertThat(calls).hasValue(0);
        assertThat(population.get(0).getGene()).containsExactly((byte) 1);
        assertThat(population.get(1).getGene()).containsExactly((byte) 2);
        assertThat(population.get(0).needsEvaluation()).isFalse();
        assertThat(population.get(1).needsEvaluation()).isFalse();
    }

    @Test
    void invalidReconfigurationLeavesThePreviousValidConfigurationIntact() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        AtomicInteger originalCalls = new AtomicInteger();
        AtomicInteger replacementCalls = new AtomicInteger();
        Recombination recombination = new Recombination();
        recombination.configure((parent1, parent2) -> {
                    originalCalls.incrementAndGet();
                    return new Individual[]{parent1.clone(), parent2.clone()};
                }, 1.0, Individual::clone);

        assertThatThrownBy(() -> recombination.configure((parent1, parent2) -> {
            replacementCalls.incrementAndGet();
            return new Individual[]{parent1.clone(), parent2.clone()};
        }, 1.1, Individual::clone))
                .isInstanceOf(IllegalArgumentException.class);

        recombination.apply(population, 0, 1);

        assertThat(originalCalls).hasValue(1);
        assertThat(replacementCalls).hasValue(0);
        assertThat(population.get(0).getGene()).containsExactly((byte) 1);
        assertThat(population.get(1).getGene()).containsExactly((byte) 2);
    }

    @Test
    void rejectsMalformedCrossoverResultsBeforeReplacingTheParentPair() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        Individual first = population.get(0);
        Individual second = population.get(1);
        Recombination recombination = new Recombination();
        recombination.configure((parent1, parent2) -> new Individual[]{parent1.clone()},
                1.0, Individual::clone);

        assertThatThrownBy(() -> recombination.apply(population, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly two");
        assertThat(population.get(0)).isSameAs(first);
        assertThat(population.get(1)).isSameAs(second);
    }

    @Test
    void rejectsNullOffspringAndMalformedPopulationBeforeChangingIt() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        Individual first = population.get(0);
        Individual second = population.get(1);
        Recombination nullOffspring = new Recombination();
        nullOffspring.configure((parent1, parent2) -> new Individual[]{parent1.clone(), null},
                1.0, Individual::clone);

        assertThatThrownBy(() -> nullOffspring.apply(population, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be null");
        assertThat(population.get(0)).isSameAs(first);
        assertThat(population.get(1)).isSameAs(second);

        population.set(1, PopulationFactory.create(1, 9).get(0));
        population.get(1).setGene(new byte[]{0, 2});
        AtomicInteger crossoverCalls = new AtomicInteger();
        Recombination malformedPopulation = new Recombination();
        malformedPopulation.configure((parent1, parent2) -> {
            crossoverCalls.incrementAndGet();
            return new Individual[]{parent1.clone(), parent2.clone()};
        }, 1.0, Individual::clone);

        assertThatThrownBy(() -> malformedPopulation.apply(population, 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("matching gene lengths");
        assertThat(crossoverCalls).hasValue(0);
        assertThat(population.get(0)).isSameAs(first);
    }

    @Test
    void rejectsMalformedOffspringGenomeAndNullMutationResultWithoutReplacingSourceIndividuals() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        Individual first = population.get(0);
        Individual second = population.get(1);
        Recombination malformedCrossover = new Recombination();
        malformedCrossover.configure((parent1, parent2) -> new Individual[]{
                parent1.clone(), PopulationFactory.create(1, 9).get(0)}, 1.0, Individual::clone);

        assertThatThrownBy(() -> malformedCrossover.apply(population, 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
        assertThat(population.get(0)).isSameAs(first);
        assertThat(population.get(1)).isSameAs(second);

        Recombination nullMutation = new Recombination();
        nullMutation.configure((parent1, parent2) -> new Individual[]{
                parent1.clone(), parent2.clone()}, 0.0, individual -> null);
        assertThatThrownBy(() -> nullMutation.apply(population, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mutation offspring");
        assertThat(population.get(0)).isSameAs(first);
        assertThat(population.get(1)).isSameAs(second);
    }

    @Test
    void protectsPopulationFromInPlaceMutationImplementations() {
        Population population = PopulationFactory.create(2, 8);
        setIndividual(population.get(0), new byte[]{1});
        setIndividual(population.get(1), new byte[]{2});
        Individual first = population.get(0);
        Individual second = population.get(1);
        Recombination recombination = new Recombination();
        recombination.configure((parent1, parent2) -> new Individual[]{parent1.clone(), parent2.clone()},
                0.0, individual -> {
                    individual.flipBit(0);
                    return individual;
                });

        recombination.apply(population, 0, 1);

        assertThat(first.getGene()).containsExactly((byte) 1);
        assertThat(second.getGene()).containsExactly((byte) 2);
        assertThat(population.get(0).getGene()).containsExactly((byte) 0);
        assertThat(population.get(1).getGene()).containsExactly((byte) 3);
        assertThat(population.getAll()).doesNotContain(first, second);
        assertThat(population.get(0).needsEvaluation()).isTrue();
        assertThat(population.get(1).needsEvaluation()).isTrue();
    }

    private static void setIndividual(Individual individual, byte[] gene) {
        individual.setGene(gene);
        individual.setFitness(0.0);
        individual.setNeedsEvaluation(false);
    }
}
