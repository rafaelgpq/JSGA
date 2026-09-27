package com.rafaelgpq.jsga.evaluation;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.track.StatisticsTracker;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class FitnessEvaluatorTest {

    @Test
    void evaluatesOnlyDirtyIndividualsAndReturnsTheNumberOfObjectiveCalls() {
        Population population = PopulationFactory.create(3, 8);
        population.get(1).setNeedsEvaluation(false);
        AtomicInteger calls = new AtomicInteger();

        int evaluated = new FitnessEvaluator(gene -> {
            calls.incrementAndGet();
            return (double) Integer.bitCount(gene[0]);
        }, false, 0.0).evaluate(population);

        assertThat(evaluated).isEqualTo(2);
        assertThat(calls).hasValue(2);
        assertThat(population.get(0).needsEvaluation()).isFalse();
        assertThat(population.get(1).needsEvaluation()).isFalse();
        assertThat(population.get(2).needsEvaluation()).isFalse();
    }

    @Test
    void countsAndReportsEverySuccessfulEvaluationToStatistics() {
        Population population = PopulationFactory.create(2, 8);
        StatisticsTracker stats = mock(StatisticsTracker.class);
        org.mockito.Mockito.when(stats.getBest()).thenReturn(1.0);
        org.mockito.Mockito.when(stats.shouldSaveBest()).thenReturn(true, false);
        org.mockito.Mockito.when(stats.shouldDump()).thenReturn(false, true);

        int evaluated = new FitnessEvaluator(gene -> 1.0, stats).evaluate(population);

        assertThat(evaluated).isEqualTo(2);
        verify(stats, times(2)).incrementTrials();
        verify(stats, times(2)).updateBest(1.0);
        verify(stats, times(2)).accumulateOnSum(1.0);
        verify(stats, times(2)).accumulateOffSum(1.0);
        verify(stats, times(2)).shouldSaveBest();
        verify(stats).saveBest(population.get(0));
        verify(stats, times(2)).shouldDump();
        verify(stats).dumpCheckpoint();
    }

    @Test
    void returnsZeroForEmptyPopulationAndRejectsNullPopulation() {
        FitnessEvaluator evaluator = new FitnessEvaluator(gene -> 0.0, false, 0.0);

        assertThat(evaluator.evaluate(PopulationFactory.create(0, 8))).isZero();
        assertThatThrownBy(() -> evaluator.evaluate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Population");
    }

    @Test
    void rejectsNonFiniteFitnessAndLeavesIndividualDirty() {
        Population population = PopulationFactory.create(1, 8);
        FitnessEvaluator evaluator = new FitnessEvaluator(gene -> Double.NaN, false, 0.0);

        assertThatThrownBy(() -> evaluator.evaluate(population))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThat(population.get(0).needsEvaluation()).isTrue();
    }

    @Test
    void validatesFitnessSharingRadius() {
        assertThatThrownBy(() -> new FitnessEvaluator(gene -> 0.0, true, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Niche radius");
        assertThatThrownBy(() -> new FitnessEvaluator((FitnessEvaluator.FitnessFunction) null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void fitnessSharingRecalculatesEveryAdjustedScoreFromCachedRawFitness() {
        Population population = PopulationFactory.create(3, 8);
        population.get(0).setGene(new byte[]{0});
        population.get(1).setGene(new byte[]{0});
        population.get(2).setGene(new byte[]{1});
        AtomicInteger calls = new AtomicInteger();
        FitnessEvaluator evaluator = new FitnessEvaluator(gene -> {
            calls.incrementAndGet();
            return gene[0] == 0 ? -8.0 : -4.0;
        }, true, 1.0);

        assertThat(evaluator.evaluate(population)).isEqualTo(3);
        assertThat(population.get(0).getFitness()).isEqualTo(-6.0);
        assertThat(population.get(1).getFitness()).isEqualTo(-6.0);
        assertThat(population.get(2).getFitness()).isEqualTo(-4.0);

        population.get(2).setGene(new byte[]{0});
        population.get(2).setNeedsEvaluation(true);
        assertThat(evaluator.evaluate(population)).isEqualTo(1);
        assertThat(calls).hasValue(4);
        assertThat(population.get(0).getFitness()).isEqualTo(-8.0);
        assertThat(population.get(1).getFitness()).isEqualTo(-8.0);
        assertThat(population.get(2).getFitness()).isEqualTo(-8.0);
        assertThat(population.get(0).getRawFitness()).isEqualTo(-8.0);
        assertThat(population.get(1).getRawFitness()).isEqualTo(-8.0);
        assertThat(population.get(2).getRawFitness()).isEqualTo(-8.0);
    }
}
