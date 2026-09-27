package com.rafaelgpq.jsga.measure;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
class MeasureEngineTest {

    @Test
    void implicitTrialCountsAndNegativeWindowUseTheSigmaFormula() {
        MeasureEngine measure = new MeasureEngine(1.0, -1, false, false, false);
        measure.update(populationWithFitness(2.0, 4.0));

        assertThat(measure.getTrials()).isEqualTo(2);
        assertThat(measure.getGeneration()).isEqualTo(1);
        assertThat(measure.getWorst()).isEqualTo(3.0 + Math.sqrt(2.0));
        assertThatThrownBy(() -> new MeasureEngine(Double.NaN, 0, false, false, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tracksMinimizationMetricsAndCumulativeObjectiveEvaluationCount() {
        MeasureEngine measure = new MeasureEngine(-1.0, 0, false, true, true);
        Population first = PopulationFactory.create(2, 8);
        first.get(0).setFitness(-4.0);
        first.get(1).setFitness(-2.0);
        measure.update(first, 2);

        Population second = PopulationFactory.create(2, 8);
        second.get(0).setFitness(-5.0);
        second.get(1).setFitness(-1.0);
        measure.update(second, 5);

        assertThat(measure.getTrials()).isEqualTo(5);
        assertThat(measure.getGeneration()).isEqualTo(2);
        assertThat(measure.getOnline()).isEqualTo(-3.0);
        assertThat(measure.getOffline()).isEqualTo(-4.5);
        assertThat(measure.getWorst()).isEqualTo(-1.0);
        assertThat(measure.getAverage()).isEqualTo(-3.0);
    }

    @Test
    void handlesSingletonAndRejectsInvalidUpdates() {
        MeasureEngine measure = new MeasureEngine(0.0, 0, false, false, false);
        Population singleton = PopulationFactory.create(1, 1);
        singleton.get(0).setFitness(-2.0);
        measure.update(singleton, 1);
        assertThat(measure.getWorst()).isEqualTo(-2.0);

        assertThatThrownBy(() -> measure.update(PopulationFactory.create(0, 1), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty population");
        assertThatThrownBy(() -> measure.update(singleton, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not decrease");
    }

    @Test
    void positiveWindowUsesOnlyObservedNegativeFitnessValues() {
        MeasureEngine measure = new MeasureEngine(1.0, 2, false, false, false);
        Population first = populationWithFitness(-8.0, -6.0);
        measure.update(first, 2);
        assertThat(measure.getWorst()).isEqualTo(-6.0);

        measure.update(populationWithFitness(-5.0, -4.0), 4);
        assertThat(measure.getWorst()).isEqualTo(-4.0);

        measure.update(populationWithFitness(-3.0, -2.0), 6);
        assertThat(measure.getWorst()).isEqualTo(-2.0);
    }

    @Test
    void rejectsInvalidWindowsAndDoesNotCommitAnInvalidObservation() {
        assertThatThrownBy(() -> new MeasureEngine(0.0, -2, false, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Window size");

        MeasureEngine measure = new MeasureEngine(0.0, 1, false, false, false);
        Population invalid = populationWithFitness(-1.0, Double.NaN);
        assertThatThrownBy(() -> measure.update(invalid, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThat(measure.getGeneration()).isZero();
        assertThat(measure.getTrials()).isZero();
    }

    @Test
    void rejectsNullIndividualsOverflowAndGenerationCounterExhaustion() throws Exception {
        MeasureEngine measure = new MeasureEngine(0.0, 0, false, false, false);
        assertThatThrownBy(() -> measure.update(null)).isInstanceOf(IllegalArgumentException.class);
        Population nullIndividual = PopulationFactory.create(1, 8);
        nullIndividual.set(0, null);
        assertThatThrownBy(() -> measure.update(nullIndividual, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");

        assertThatThrownBy(() -> measure.update(populationWithFitness(-Double.MAX_VALUE, Double.MAX_VALUE), 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite numeric range");
        assertThat(measure.getGeneration()).isZero();

        Field generation = MeasureEngine.class.getDeclaredField("generation");
        generation.setAccessible(true);
        generation.setInt(measure, Integer.MAX_VALUE);
        assertThatThrownBy(() -> measure.update(populationWithFitness(0.0, 1.0), 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overflow");
    }

    @Test
    void traceHonorsOnlineAndOfflineOutputFlags() {
        MeasureEngine measure = new MeasureEngine(0.0, 0, true, true, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            measure.update(populationWithFitness(-4.0, -2.0), 2);
        } finally {
            System.setOut(original);
        }
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Gen 0, Evaluations 2", "Online:")
                .doesNotContain("Offline:");
    }

    @Test
    void traceCanReportBothOnlineAndOfflineMetrics() {
        MeasureEngine measure = new MeasureEngine(0.0, 0, true, true, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            measure.update(populationWithFitness(-4.0, -2.0), 2);
        } finally {
            System.setOut(original);
        }
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Online:", "Offline:");
    }

    private static Population populationWithFitness(double first, double second) {
        Population population = PopulationFactory.create(2, 8);
        population.get(0).setFitness(first);
        population.get(1).setFitness(second);
        return population;
    }
}
