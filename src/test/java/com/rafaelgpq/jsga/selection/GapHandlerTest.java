package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class GapHandlerTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(901L);
    }

    @Test
    void partialGapCombinesSelectedOffspringWithUniqueClonedSurvivors() {
        Population parents = populationWithGenes(5, 0);
        Population selected = populationWithGenes(5, 20);

        int offspringCount = new GapHandler().applyGap(parents, selected, 0.4);

        assertThat(offspringCount).isEqualTo(2);
        assertThat(selected.size()).isEqualTo(5);
        Set<Byte> selectedGenes = new HashSet<>();
        Set<Byte> survivorGenes = new HashSet<>();
        for (int i = 0; i < offspringCount; i++) {
            selectedGenes.add(selected.get(i).getGene()[0]);
            assertThat(selected.get(i)).isNotSameAs(parents.get(i));
        }
        for (int i = offspringCount; i < selected.size(); i++) {
            survivorGenes.add(selected.get(i).getGene()[0]);
            assertThat(selected.get(i)).isNotSameAs(parents.get(i - offspringCount));
        }
        assertThat(selectedGenes).allMatch(gene -> gene >= 20 && gene < 25);
        assertThat(survivorGenes).hasSize(3).allMatch(gene -> gene >= 0 && gene < 5);
    }

    @Test
    void zeroAndFullGapHaveExpectedReplacementCounts() {
        Population parents = populationWithGenes(3, 0);
        Population selected = populationWithGenes(3, 20);
        byte[][] originalParentGenes = parentGenes(parents);

        assertThat(new GapHandler().applyGap(parents, selected, 0.0)).isZero();
        assertThat(selected.getAll()).extracting(individual -> individual.getGene()[0])
                .containsExactlyInAnyOrder(
                        originalParentGenes[0][0], originalParentGenes[1][0], originalParentGenes[2][0]);

        selected = populationWithGenes(3, 20);
        assertThat(new GapHandler().applyGap(parents, selected, 1.0)).isEqualTo(3);
        assertThat(selected.getAll()).extracting(individual -> individual.getGene()[0])
                .containsExactly((byte) 20, (byte) 21, (byte) 22);
    }

    @Test
    void fractionalGapTruncatesLikeGAucsdAndSupportsSingletonPopulations() {
        Population parents = populationWithGenes(3, 0);
        Population selected = populationWithGenes(3, 20);
        assertThat(new GapHandler().applyGap(parents, selected, 0.01)).isZero();
        assertThat(new GapHandler().applyGap(parents, selected, 0.34)).isEqualTo(1);

        Population singletonParent = populationWithGenes(1, 0);
        Population singletonSelected = populationWithGenes(1, 20);
        assertThat(new GapHandler().applyGap(singletonParent, singletonSelected, 0.5)).isZero();
        assertThat(singletonSelected.get(0).getGene()[0]).isEqualTo((byte) 0);
        singletonSelected = populationWithGenes(1, 20);
        assertThat(new GapHandler().applyGap(singletonParent, singletonSelected, 1.0)).isEqualTo(1);
        assertThat(singletonSelected.get(0).getGene()[0]).isEqualTo((byte) 20);
    }

    @Test
    void rejectsInvalidGapInputsAndMalformedLegacySamples() {
        GapHandler handler = new GapHandler();
        Population parents = populationWithGenes(2, 0);
        Population selected = populationWithGenes(2, 10);

        for (double invalidGap : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -0.01, 1.01}) {
            assertThatThrownBy(() -> handler.applyGap(parents, selected, invalidGap))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Gap size");
        }
        assertThatThrownBy(() -> handler.applyGap(null, selected, 0.5))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.applyGap(parents, parents, 0.5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> handler.applyGap(parents, PopulationFactory.create(1, 8), 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("equal sizes");
        assertThatThrownBy(() -> handler.applyGap(PopulationFactory.create(0, 8),
                PopulationFactory.create(0, 8), 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty");
        Population mismatchedGenomeLength = PopulationFactory.create(2, 7);
        assertThatThrownBy(() -> handler.applyGap(parents, mismatchedGenomeLength, 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genome");
        Population nullMember = populationWithGenes(2, 0);
        nullMember.set(1, null);
        assertThatThrownBy(() -> handler.applyGap(nullMember, populationWithGenes(2, 10), 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid genome or fitness");
        Population nonFiniteFitness = populationWithGenes(2, 0);
        nonFiniteFitness.get(1).setRawFitness(Double.NaN);
        assertThatThrownBy(() -> handler.applyGap(nonFiniteFitness, populationWithGenes(2, 10), 0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid genome or fitness");
        assertThatThrownBy(() -> handler.applyGap(new int[1], 0.5, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least populationSize");
        assertThatThrownBy(() -> handler.applyGap(new int[2], 0.5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void legacySampleGapRetainsSelectedPrefixAndFillsTheRemainingSlotsWithSurvivors() {
        int[] sample = {10, 11, 12, 13, 14, 99};

        new GapHandler().applyGap(sample, 0.4, 5);

        assertThat(java.util.Arrays.stream(sample, 0, 2)).allMatch(value -> value >= 10 && value <= 14);
        assertThat(java.util.Arrays.stream(sample, 2, 5)).allMatch(value -> value >= 0 && value < 5);
        assertThat(sample[5]).isEqualTo(99);
    }

    @Test
    void legacySampleGapSupportsZeroAndFullReplacementAndValidatesArguments() {
        GapHandler handler = new GapHandler();
        int[] untouchedTail = {7, 8, 9, 10};
        handler.applyGap(untouchedTail, 0.0, 3);
        assertThat(java.util.Arrays.stream(untouchedTail, 0, 3)).allMatch(value -> value >= 0 && value < 3);
        assertThat(untouchedTail[3]).isEqualTo(10);

        int[] fullReplacement = {7, 8, 9};
        handler.applyGap(fullReplacement, 1.0, 3);
        assertThat(fullReplacement).containsExactlyInAnyOrder(7, 8, 9);

        assertThatThrownBy(() -> handler.applyGap(null, 0.5, 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.applyGap(new int[1], -0.1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gap size");
        assertThatThrownBy(() -> handler.applyGap(new int[1], 0.5, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    private static Population populationWithGenes(int size, int firstGene) {
        Population population = PopulationFactory.create(size, 8);
        for (int i = 0; i < size; i++) {
            population.get(i).setGene(new byte[]{(byte) (firstGene + i)});
            population.get(i).setFitness(-i);
            population.get(i).setNeedsEvaluation(false);
        }
        return population;
    }

    private static byte[][] parentGenes(Population population) {
        byte[][] genes = new byte[population.size()][];
        for (int i = 0; i < population.size(); i++) {
            genes[i] = population.get(i).getGene().clone();
        }
        return genes;
    }
}
