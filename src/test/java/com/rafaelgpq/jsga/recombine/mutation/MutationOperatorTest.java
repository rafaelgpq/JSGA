package com.rafaelgpq.jsga.recombine.mutation;

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
class MutationOperatorTest {

    @BeforeEach
    void seedRandomGenerator() {
        RandomUtils.initialize(71L);
    }

    @Test
    void bitFlipAtRateOneFlipsEveryLogicalBitAndKeepsPaddingClear() {
        Individual parent = individual(13, new byte[]{0, 0});

        Individual mutated = new BitFlipMutation(1.0, 13).mutate(parent);

        assertThat(mutated.getGene()).containsExactly((byte) 0xff, (byte) 0x1f);
        assertThat(mutated.needsEvaluation()).isTrue();
        assertThat(parent.getGene()).containsExactly((byte) 0, (byte) 0);
        assertThat(parent.needsEvaluation()).isFalse();
        assertThat(mutated.getGene()).isNotSameAs(parent.getGene());
    }

    @Test
    void adaptiveBitFlipCapsTheAdjustedRateAtOne() {
        Individual parent = individual(13, new byte[]{0, 0});
        RandomUtils.initialize(7L);

        Individual mutated = new AdaptiveBitFlipMutation(1.0, 13, true).mutate(parent);

        assertThat(mutated.getGene()).containsExactly((byte) 0xff, (byte) 0x1f);
        assertThat(mutated.needsEvaluation()).isTrue();
    }

    @Test
    void disabledAdaptiveFlagUsesExactlyTheBaseBitFlipRate() {
        Individual parent = individual(13, new byte[]{0, 0});
        RandomUtils.initialize(32L);
        byte[] basicResult = new BitFlipMutation(0.35, 13).mutate(parent).getGene().clone();
        RandomUtils.initialize(32L);
        byte[] nonAdaptiveResult = new AdaptiveBitFlipMutation(0.35, 13, false).mutate(parent).getGene();

        assertThat(nonAdaptiveResult).containsExactly(basicResult);
    }

    @Test
    void bitFlipMutationUsesTheConfiguredIntermediatePerBitProbability() {
        int geneLength = 4096;
        Individual parent = individual(geneLength, new byte[geneLength / 8]);
        RandomUtils.initialize(817L);

        Individual mutated = new BitFlipMutation(0.25, geneLength).mutate(parent);

        int flippedBits = validBitCount(mutated);
        assertThat(flippedBits).isBetween(900, 1148);
    }

    @Test
    void zeroMutationRateReturnsAnIndependentCleanCloneForEveryMutationType() {
        Individual parent = individual(13, new byte[]{0x55, 0x05});
        List<Mutation> mutations = List.of(
                new BitFlipMutation(0.0, 13),
                new AdaptiveBitFlipMutation(0.0, 13, true),
                new SwapMutation(0.0, 13),
                new GaussianMutation(0.0, 13));

        for (Mutation mutation : mutations) {
            Individual result = mutation.mutate(parent);
            assertThat(result).isNotSameAs(parent);
            assertThat(result.getGene()).containsExactly(parent.getGene());
            assertThat(result.getGene()).isNotSameAs(parent.getGene());
            assertThat(result.needsEvaluation()).isFalse();
        }
    }

    @Test
    void swapMutationPreservesTheNumberOfSetBitsAndSourceGenome() {
        Individual parent = individual(13, new byte[]{0x55, 0x05});
        byte[] original = parent.getGene().clone();

        Individual mutated = new SwapMutation(1.0, 13).mutate(parent);

        assertThat(validBitCount(mutated)).isEqualTo(validBitCount(parent));
        assertThat(mutated.getGene()[1] & 0b11100000).isZero();
        assertThat(parent.getGene()).containsExactly(original);
        assertThat(mutated.needsEvaluation()).isEqualTo(!java.util.Arrays.equals(original, mutated.getGene()));
    }

    @Test
    void swapMutationSelectsDistinctLociWhenAValidSwapIsPossible() {
        Individual parent = individual(2, new byte[]{1});

        for (long seed = 0; seed < 20; seed++) {
            RandomUtils.initialize(seed);
            Individual mutated = new SwapMutation(1.0, 2).mutate(parent);

            assertThat(mutated.getGene()).containsExactly((byte) 2);
            assertThat(mutated.needsEvaluation()).isTrue();
        }
    }

    @Test
    void swapMutationDoesNotAttemptAnImpossibleOneLocusSwap() {
        Individual parent = individual(1, new byte[]{1});
        RandomUtils.initialize(4L);

        Individual mutated = new SwapMutation(1.0, 1).mutate(parent);

        assertThat(mutated.getGene()).containsExactly((byte) 1);
        assertThat(mutated.needsEvaluation()).isFalse();
    }

    @Test
    void gaussianMutationProducesValidDirtyOffspringWhenTheSeededPerturbationChangesBits() {
        Individual parent = individual(13, new byte[]{0, 0});
        RandomUtils.initialize(1234L);

        Individual mutated = new GaussianMutation(1.0, 13).mutate(parent);

        assertThat(mutated.getGene()).hasSize(2);
        assertThat(mutated.getGene()[1] & 0b11100000).isZero();
        assertThat(mutated.getGene()).isNotEqualTo(parent.getGene());
        assertThat(mutated.needsEvaluation()).isTrue();
        assertThat(parent.getGene()).containsExactly((byte) 0, (byte) 0);
    }

    @Test
    void mutationFactoriesNormalizeSupportedNamesAndRejectInvalidSettings() {
        for (String type : List.of("bitflip", "adaptivebitflip", "swap", "gaussian")) {
            assertThat(MutationFactory.create(" " + type.toUpperCase() + " ", 0.1, 13, false))
                    .isNotNull();
        }
        assertThatThrownBy(() -> MutationFactory.create("unknown", 0.1, 13, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported mutation");
        assertThatThrownBy(() -> MutationFactory.create("bitflip", Double.NaN, 13, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mutation rate");
        assertThatThrownBy(() -> MutationFactory.create("bitflip", 0.1, 0, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SwapMutation(-0.1, 8))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GaussianMutation(0.1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mutationsRejectMismatchedGeneShapes() {
        Individual wrongLength = PopulationFactory.create(1, 8).get(0);
        List<Mutation> mutations = List.of(
                new BitFlipMutation(0.5, 9),
                new AdaptiveBitFlipMutation(0.5, 9, false),
                new SwapMutation(0.5, 9),
                new GaussianMutation(0.5, 9));

        for (Mutation mutation : mutations) {
            assertThatThrownBy(() -> mutation.mutate(wrongLength))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not match");
        }
    }

    @Test
    void mutationsRejectNullAndIncorrectlyPackedGenes() {
        Individual malformed = individual(8, new byte[]{1});
        List<Mutation> mutations = List.of(
                new BitFlipMutation(0.5, 8),
                new AdaptiveBitFlipMutation(0.5, 8, true),
                new SwapMutation(0.5, 8),
                new GaussianMutation(0.5, 8));

        malformed.setGene(null);
        for (Mutation mutation : mutations) {
            assertThatThrownBy(() -> mutation.mutate(malformed))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("gene must not be null");
        }

        malformed.setGene(new byte[2]);
        for (Mutation mutation : mutations) {
            assertThatThrownBy(() -> mutation.mutate(malformed))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires 1 bytes");
        }
    }

    @Test
    void mutationTypesHandleByteBoundariesWithoutTouchingPadding() {
        for (int geneLength = 1; geneLength <= 17; geneLength++) {
            int byteLength = geneLength / 8 + (geneLength % 8 == 0 ? 0 : 1);
            Individual parent = individual(geneLength, new byte[byteLength]);
            List<Mutation> mutations = List.of(
                    new BitFlipMutation(1.0, geneLength),
                    new AdaptiveBitFlipMutation(1.0, geneLength, true),
                    new SwapMutation(1.0, geneLength),
                    new GaussianMutation(1.0, geneLength));

            for (Mutation mutation : mutations) {
                RandomUtils.initialize(geneLength);
                Individual result = mutation.mutate(parent);
                assertThat(result.getGene()).hasSize(byteLength);
                if (geneLength % 8 != 0) {
                    int paddingMask = 0xff << (geneLength % 8);
                    assertThat(result.getGene()[byteLength - 1] & paddingMask).isZero();
                }
            }
        }
    }

    @Test
    void mutationClearsUnusedPaddingAndMarksTheGenomeDirty() {
        Individual parent = individual(9, new byte[]{0, (byte) 0xfe});

        Individual result = new BitFlipMutation(0.0, 9).mutate(parent);

        assertThat(result.getGene()).containsExactly((byte) 0, (byte) 0);
        assertThat(result.needsEvaluation()).isTrue();
        assertThat(parent.getGene()).containsExactly((byte) 0, (byte) 0xfe);
    }

    private static Individual individual(int geneLength, byte[] gene) {
        Population population = PopulationFactory.create(1, geneLength);
        Individual individual = population.get(0);
        individual.setGene(gene);
        individual.setFitness(-2.0);
        individual.setNeedsEvaluation(false);
        return individual;
    }

    private static int validBitCount(Individual individual) {
        int count = 0;
        for (int bit = 0; bit < individual.getGeneLength(); bit++) {
            count += (individual.getGene()[bit / 8] >>> (bit % 8)) & 1;
        }
        return count;
    }
}
