package com.rafaelgpq.jsga.recombine.crossover;

import com.rafaelgpq.jsga.model.Individual;
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
class CrossoverOperatorTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(19L);
    }

    @Test
    void crossoverTypesExchangeOnlyLogicalLociAndDoNotModifyParents() {
        int geneLength = 13;
        Population parents = PopulationFactory.create(2, geneLength);
        setIndividual(parents.get(0), new byte[]{0, 0});
        setIndividual(parents.get(1), new byte[]{(byte) 0xff, (byte) 0xff});
        byte[] originalFirst = parents.get(0).getGene().clone();
        byte[] originalSecond = parents.get(1).getGene().clone();

        List<String> crossoverTypes = List.of("onepoint", "twopoint", "uniform");
        for (String crossoverType : crossoverTypes) {
            RandomUtils.initialize(19L);
            Individual[] children = CrossoverFactory.create(crossoverType, geneLength)
                    .crossover(parents.get(0), parents.get(1));

            assertThat(children).hasSize(2);
            assertThat(children[0].getGene()).hasSize(2);
            assertThat(children[1].getGene()).hasSize(2);
            assertThat(children[0].getGene()[1] & 0b11100000).isZero();
            assertThat(children[1].getGene()[1] & 0b11100000).isZero();
            assertThat(children[0].getGene()).isNotSameAs(parents.get(0).getGene());
            assertThat(children[1].getGene()).isNotSameAs(parents.get(1).getGene());
            assertThat(children[0].needsEvaluation()).isTrue();
            assertThat(children[1].needsEvaluation()).isTrue();
            for (int bit = 0; bit < geneLength; bit++) {
                assertThat(bitValue(children[0], bit) + bitValue(children[1], bit)).isEqualTo(1);
            }
        }

        assertThat(parents.get(0).getGene()).containsExactly(originalFirst);
        assertThat(parents.get(1).getGene()).containsExactly(originalSecond);
        assertThat(parents.get(0).needsEvaluation()).isFalse();
        assertThat(parents.get(1).needsEvaluation()).isFalse();
    }

    @Test
    void onePointAndTwoPointCrossoverPreserveTheirExpectedSegmentShapes() {
        int geneLength = 13;
        Population parents = PopulationFactory.create(2, geneLength);
        setIndividual(parents.get(0), new byte[]{0, 0});
        setIndividual(parents.get(1), new byte[]{(byte) 0xff, 0x1f});

        RandomUtils.initialize(54L);
        Individual[] onePointChildren = new OnePointCrossover(geneLength)
                .crossover(parents.get(0), parents.get(1));
        int transition = 1;
        while (transition < geneLength && bitValue(onePointChildren[0], transition) == 0) {
            transition++;
        }
        assertThat(transition).isBetween(1, geneLength - 1);
        for (int bit = 0; bit < geneLength; bit++) {
            assertThat(bitValue(onePointChildren[0], bit)).isEqualTo(bit < transition ? 0 : 1);
        }

        RandomUtils.initialize(54L);
        Individual[] twoPointChildren = new TwoPointCrossover(geneLength)
                .crossover(parents.get(0), parents.get(1));
        int start = 0;
        while (start < geneLength && bitValue(twoPointChildren[0], start) == 0) {
            start++;
        }
        int end = start;
        while (end < geneLength && bitValue(twoPointChildren[0], end) == 1) {
            end++;
        }
        assertThat(start).isLessThan(geneLength);
        assertThat(end).isGreaterThan(start);
        for (int bit = end; bit < geneLength; bit++) {
            assertThat(bitValue(twoPointChildren[0], bit)).isZero();
        }
    }

    @Test
    void noOpOnePointCrossoverReturnsIndependentCleanClonesForSingleBitGenes() {
        Population parents = PopulationFactory.create(2, 1);
        setIndividual(parents.get(0), new byte[]{1});
        setIndividual(parents.get(1), new byte[]{1});

        Individual[] children = new OnePointCrossover(1)
                .crossover(parents.get(0), parents.get(1));

        assertThat(children[0]).isNotSameAs(parents.get(0));
        assertThat(children[1]).isNotSameAs(parents.get(1));
        assertThat(children[0].getGene()).containsExactly((byte) 1);
        assertThat(children[1].getGene()).containsExactly((byte) 1);
        assertThat(children[0].needsEvaluation()).isFalse();
        assertThat(children[1].needsEvaluation()).isFalse();
    }

    @Test
    void operatorsClearUnusedParentPaddingAndInvalidateFitnessWhenNormalizationChangesBytes() {
        Population parents = PopulationFactory.create(2, 9);
        setIndividual(parents.get(0), new byte[]{0, (byte) 0xfe});
        setIndividual(parents.get(1), new byte[]{0, (byte) 0xfe});

        Individual[] children = new OnePointCrossover(9)
                .crossover(parents.get(0), parents.get(1));

        assertThat(children[0].getGene()).containsExactly((byte) 0, (byte) 0);
        assertThat(children[1].getGene()).containsExactly((byte) 0, (byte) 0);
        assertThat(children[0].needsEvaluation()).isTrue();
        assertThat(children[1].needsEvaluation()).isTrue();
    }

    @Test
    void crossoverTypesHandleByteAndCutBoundariesAcrossGenomeLengths() {
        for (int geneLength = 1; geneLength <= 17; geneLength++) {
            Population parents = PopulationFactory.create(2, geneLength);
            byte[] allOnes = new byte[geneLength / 8 + (geneLength % 8 == 0 ? 0 : 1)];
            java.util.Arrays.fill(allOnes, (byte) 0xff);
            int remainder = geneLength % 8;
            if (remainder != 0) {
                allOnes[allOnes.length - 1] = (byte) ((1 << remainder) - 1);
            }
            setIndividual(parents.get(0), new byte[allOnes.length]);
            setIndividual(parents.get(1), allOnes);

            for (String crossoverType : List.of("onepoint", "twopoint", "uniform")) {
                RandomUtils.initialize(geneLength);
                Individual[] children = CrossoverFactory.create(crossoverType, geneLength)
                        .crossover(parents.get(0), parents.get(1));

                assertThat(children[0].getGene()).hasSize(allOnes.length);
                assertThat(children[1].getGene()).hasSize(allOnes.length);
                for (int bit = 0; bit < geneLength; bit++) {
                    assertThat(bitValue(children[0], bit) + bitValue(children[1], bit)).isEqualTo(1);
                }
                if (remainder != 0) {
                    int paddingMask = 0xff << remainder;
                    assertThat(children[0].getGene()[allOnes.length - 1] & paddingMask).isZero();
                    assertThat(children[1].getGene()[allOnes.length - 1] & paddingMask).isZero();
                }
            }
        }
    }

    @Test
    void crossoverRejectsInvalidLengthsNullParentsAndUnsupportedTypes() {
        assertThatThrownBy(() -> new OnePointCrossover(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TwoPointCrossover(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UniformCrossover(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CrossoverFactory.create("pmx", 8))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported crossover");
        assertThatThrownBy(() -> CrossoverFactory.create("onepoint", 0))
                .isInstanceOf(IllegalArgumentException.class);

        Population wrongLength = PopulationFactory.create(1, 8);
        assertThatThrownBy(() -> new OnePointCrossover(9).crossover(wrongLength.get(0), wrongLength.get(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
        assertThatThrownBy(() -> new OnePointCrossover(8).crossover(
                wrongLength.get(0), PopulationFactory.create(1, 9).get(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
        assertThatThrownBy(() -> new OnePointCrossover(8).crossover(null, wrongLength.get(0)))
                .isInstanceOf(NullPointerException.class);

        Individual malformedGene = wrongLength.get(0);
        malformedGene.setGene(new byte[0]);
        assertThatThrownBy(() -> new OnePointCrossover(8).crossover(malformedGene, wrongLength.get(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 1 bytes");
        malformedGene.setGene(new byte[]{1, 0});
        assertThatThrownBy(() -> new OnePointCrossover(8).crossover(malformedGene, wrongLength.get(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 1 bytes");
    }

    private static int bitValue(Individual individual, int bit) {
        return (individual.getGene()[bit / 8] >>> (bit % 8)) & 1;
    }

    private static void setIndividual(Individual individual, byte[] gene) {
        individual.setGene(gene);
        individual.setFitness(0.0);
        individual.setNeedsEvaluation(false);
    }
}
