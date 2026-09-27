package com.rafaelgpq.jsga.diversity;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class DiversityStrategyTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(271L);
    }

    @Test
    void randomImmigrantsReplaceDistinctIndividualsWithDirtyValidGenomes() {
        Population population = population(5, 13);
        Individual[] oldIndividuals = population.getAll().toArray(new Individual[0]);

        new RandomImmigrantsStrategy(3, 13).apply(population, 0);

        int replaced = 0;
        for (int i = 0; i < population.size(); i++) {
            if (population.get(i).needsEvaluation()) {
                replaced++;
                assertThat(population.get(i)).isNotSameAs(oldIndividuals[i]);
                assertThat(population.get(i).getGene()).hasSize(2);
                assertThat(population.get(i).getGene()[1] & 0b11100000).isZero();
            } else {
                assertThat(population.get(i)).isSameAs(oldIndividuals[i]);
            }
        }
        assertThat(replaced).isEqualTo(3);
    }

    @Test
    void crowdingReplacesConsensusClonesButPreservesAtLeastTheBestIndividual() {
        Population population = population(4, 9);
        set(population.get(0), new byte[]{0, 0}, -1.0);
        set(population.get(1), new byte[]{0, 0}, -2.0);
        set(population.get(2), new byte[]{0, 0}, -3.0);
        set(population.get(3), new byte[]{0, 0}, -4.0);
        Individual best = population.get(3);

        new CrowdingStrategy(9, 0.3).apply(population, 0);

        assertThat(population.getAll()).contains(best);
        assertThat(population.getAll().stream().filter(Individual::needsEvaluation).count()).isEqualTo(3);
        assertThat(population.getAll()).allMatch(individual ->
                (individual.getGene()[1] & 0b11111110) == 0);
    }

    @Test
    void islandMigrationMovesElitesSimultaneouslyAndPreservesPopulationSize() {
        Population population = population(4, 8);
        set(population.get(0), new byte[]{1}, -10.0);
        set(population.get(1), new byte[]{2}, -9.0);
        set(population.get(2), new byte[]{3}, -1.0);
        set(population.get(3), new byte[]{4}, 0.0);

        new IslandModelStrategy(2, 1, 1, 8, 4).apply(population, 0);

        assertThat(population.size()).isEqualTo(4);
        assertThat(population.getAll()).extracting(individual -> individual.getGene()[0])
                .contains((byte) 1, (byte) 2);
        assertThat(population.getAll().stream().filter(individual ->
                individual.getGene()[0] == 1 || individual.getGene()[0] == 2).count()).isEqualTo(4);
    }

    @Test
    void islandModelValidatesConfigurationAndPopulationShape() {
        assertThatThrownBy(() -> new IslandModelStrategy(0, 1, 0, 8, 4))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IslandModelStrategy(2, 1, 5, 8, 4))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CrowdingStrategy(8, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(3, 8).apply(population(2, 8), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not exceed");
        assertThatThrownBy(() -> new IslandModelStrategy(2, 1, 1, 8, 4)
                .apply(population(3, 8), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population size");
    }

    @Test
    void fitnessSharingUsesRawFitnessAndCanBeRepeatedWithoutCompounding() {
        Population population = population(3, 8);
        setRaw(population.get(0), new byte[]{0}, -8.0);
        setRaw(population.get(1), new byte[]{0}, -8.0);
        setRaw(population.get(2), new byte[]{1}, -4.0);

        FitnessSharingStrategy sharing = new FitnessSharingStrategy(1.0);
        sharing.apply(population, 0);
        assertThat(population.get(0).getFitness()).isEqualTo(-6.0);
        assertThat(population.get(1).getFitness()).isEqualTo(-6.0);
        assertThat(population.get(2).getFitness()).isEqualTo(-4.0);
        sharing.apply(population, 1);
        assertThat(population.get(0).getFitness()).isEqualTo(-6.0);
        assertThat(population.get(2).getFitness()).isEqualTo(-4.0);
    }

    @Test
    void fitnessSharingPenalizesCrowdedMinimizationNichesForPositiveAndNegativeObjectives() {
        Population positive = population(3, 8);
        setRaw(positive.get(0), new byte[]{0}, 2.0);
        setRaw(positive.get(1), new byte[]{0}, 2.0);
        setRaw(positive.get(2), new byte[]{1}, 8.0);
        new FitnessSharingStrategy(1.0).apply(positive, 0);

        assertThat(positive.get(0).getFitness()).isEqualTo(5.0);
        assertThat(positive.get(1).getFitness()).isEqualTo(5.0);
        assertThat(positive.get(2).getFitness()).isEqualTo(8.0);

        Population extreme = population(2, 8);
        setRaw(extreme.get(0), new byte[]{0}, -Double.MAX_VALUE);
        setRaw(extreme.get(1), new byte[]{0}, Double.MAX_VALUE);
        new FitnessSharingStrategy(1.0).apply(extreme, 0);
        assertThat(extreme.get(0).getFitness()).isFinite();
        assertThat(extreme.get(1).getFitness()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void diversityStrategiesRejectInvalidPopulations() {
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(1, 8).apply(null, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CrowdingStrategy(8, 0.3).apply(null, 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0)
                .apply(PopulationFactory.create(0, 8), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty");
    }

    @Test
    void crowdingHandlesEmptyAndDisabledCasesAndValidatesGenomeAndFitness() {
        Population empty = PopulationFactory.create(0, 8);
        new CrowdingStrategy(8, 0.5).apply(empty, 0);
        Population unchanged = population(2, 8);
        Individual first = unchanged.get(0);
        new CrowdingStrategy(8, 0.0).apply(unchanged, 0);
        assertThat(unchanged.get(0)).isSameAs(first);

        assertThatThrownBy(() -> new CrowdingStrategy(0, 0.5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CrowdingStrategy(8, -0.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CrowdingStrategy(8, 1.1))
                .isInstanceOf(IllegalArgumentException.class);
        Population malformed = population(1, 8);
        malformed.get(0).setGene(new byte[0]);
        assertThatThrownBy(() -> new CrowdingStrategy(8, 0.5).apply(malformed, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
        Population nonFinite = population(1, 8);
        nonFinite.get(0).setFitness(Double.NaN);
        assertThatThrownBy(() -> new CrowdingStrategy(8, 0.5).apply(nonFinite, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        Population nullMember = population(2, 8);
        nullMember.set(1, null);
        assertThatThrownBy(() -> new CrowdingStrategy(8, 0.5).apply(nullMember, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");

        Population tied = population(2, 8);
        set(tied.get(0), new byte[]{0}, -1);
        set(tied.get(1), new byte[]{0}, -1);
        new CrowdingStrategy(8, 1.0).apply(tied, 0);
        assertThat(tied.getAll()).hasSize(2);
        assertThat(tied.getAll().stream().filter(Individual::needsEvaluation).count()).isEqualTo(1);
    }

    @Test
    void immigrantsAllowZeroReplacementsAndRejectMalformedPopulation() {
        Population unchanged = population(2, 8);
        Individual original = unchanged.get(0);
        new RandomImmigrantsStrategy(0, 8).apply(unchanged, 0);
        assertThat(unchanged.get(0)).isSameAs(original);
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(-1, 8))
                .isInstanceOf(IllegalArgumentException.class);
        Population malformed = population(1, 8);
        ((com.rafaelgpq.jsga.model.SimpleIndividual) malformed.get(0)).setGeneLength(7);
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(0, 8).apply(malformed, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gene length");
        Population nullMember = population(1, 8);
        nullMember.set(0, null);
        assertThatThrownBy(() -> new RandomImmigrantsStrategy(0, 8).apply(nullMember, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gene length");
    }

    @Test
    void fitnessSharingHandlesZeroFitnessAndRejectsInvalidConfigurationAndGenomes() {
        assertThatThrownBy(() -> new FitnessSharingStrategy(0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FitnessSharingStrategy(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0).apply(null, 0))
                .isInstanceOf(NullPointerException.class);
        Population zeroFitness = population(1, 8);
        setRaw(zeroFitness.get(0), new byte[]{0}, 0.0);
        new FitnessSharingStrategy(1.0).apply(zeroFitness, 0);
        assertThat(zeroFitness.get(0).getFitness()).isZero();

        Population invalidGenome = population(1, 8);
        invalidGenome.get(0).setGene(new byte[0]);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0).apply(invalidGenome, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
        Population invalidRawFitness = population(1, 8);
        invalidRawFitness.get(0).setRawFitness(Double.NaN);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0).apply(invalidRawFitness, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
        Population invalidLength = population(1, 8);
        ((com.rafaelgpq.jsga.model.SimpleIndividual) invalidLength.get(0)).setGeneLength(0);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0).apply(invalidLength, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene length");
        Population nullMember = population(2, 8);
        nullMember.set(1, null);
        assertThatThrownBy(() -> new FitnessSharingStrategy(1.0).apply(nullMember, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void islandModelSkipsMigrationWhenNotDueDisabledOrOnlyOneIsland() {
        Population population = population(4, 8);
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setGene(new byte[]{(byte) i});
            population.get(i).setFitness(i);
        }
        new IslandModelStrategy(2, 2, 1, 8, 4).apply(population, 0);
        new IslandModelStrategy(2, 1, 0, 8, 4).apply(population, 0);
        new IslandModelStrategy(1, 1, 1, 8, 4).apply(population, 0);
        assertThat(population.size()).isEqualTo(4);
        assertThatThrownBy(() -> new IslandModelStrategy(1, 1, 0, 8, 1).apply(population, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population size");
        assertThatThrownBy(() -> new IslandModelStrategy(1, 1, 0, 8, 4).apply(null, 0))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new IslandModelStrategy(1, 1, 0, 8, 4).apply(population, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Generation");
        assertThatThrownBy(() -> new IslandModelStrategy(1, 0, 0, 8, 4))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IslandModelStrategy(1, 1, -1, 8, 4))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IslandModelStrategy(1, 1, 0, 0, 4))
                .isInstanceOf(IllegalArgumentException.class);

        Population malformed = population(4, 8);
        malformed.get(2).setFitness(Double.NEGATIVE_INFINITY);
        assertThatThrownBy(() -> new IslandModelStrategy(2, 1, 1, 8, 4).apply(malformed, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
        Population nullMember = population(4, 8);
        nullMember.set(2, null);
        assertThatThrownBy(() -> new IslandModelStrategy(2, 1, 1, 8, 4).apply(nullMember, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    private static Population population(int size, int geneLength) {
        Population population = PopulationFactory.create(size, geneLength);
        for (int i = 0; i < size; i++) {
            population.get(i).setNeedsEvaluation(false);
        }
        return population;
    }

    private static void set(Individual individual, byte[] gene, double fitness) {
        individual.setGene(gene);
        individual.setFitness(fitness);
        individual.setRawFitness(fitness);
        individual.setNeedsEvaluation(false);
    }

    private static void setRaw(Individual individual, byte[] gene, double rawFitness) {
        individual.setGene(gene);
        individual.setRawFitness(rawFitness);
        individual.setFitness(rawFitness);
        individual.setNeedsEvaluation(false);
    }
}
