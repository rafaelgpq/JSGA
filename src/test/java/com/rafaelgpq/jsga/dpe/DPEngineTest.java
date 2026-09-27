package com.rafaelgpq.jsga.dpe;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.util.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class DPEngineTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesEveryConfigurationAndRuntimeStateBoundary() throws IOException {
        assertThatThrownBy(() -> new DPEngine(null, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 2, "log"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, null, new double[]{0},
                new double[2], 1, 1, 1, 2, "log"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, null,
                new double[2], 1, 1, 1, 2, "log"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                null, 1, 1, 1, 2, "log"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 0, 1, 1, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 0, 1, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 0, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 0, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 2, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{0}, new double[]{0},
                new double[2], 1, 1, 1, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{Double.NaN},
                new double[2], 1, 1, 1, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[]{-1, 0}, 1, 1, 1, 2, "log"))
                .isInstanceOf(IllegalArgumentException.class);

        DPEngine engine = new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 2, tempDir.resolve("validation.log").toString());
        assertThatThrownBy(() -> engine.apply(null, 0, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> engine.apply(PopulationFactory.create(2, 2), -1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> engine.apply(PopulationFactory.create(2, 2), 0, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> engine.decodeParameters(null, 2, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> engine.decodeParameters(new byte[]{0}, 0, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zoomsRareAllelesAndWritesTheAdjustedRange() throws IOException {
        Population population = PopulationFactory.create(4, 8);
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setGene(new byte[]{0});
            population.get(i).setNeedsEvaluation(false);
        }
        double[] factors = {1.0};
        double[] bases = {0.0};
        Path log = tempDir.resolve("dpe.log");
        DPEngine engine = new DPEngine(new int[]{3}, factors, bases, new double[2],
                1, 1, 2, 4, log.toString());
        RandomUtils.initialize(19);

        engine.apply(population, 2, 17);

        assertThat(engine.getLastZoomGeneration()).isEqualTo(2);
        assertThat(factors[0]).isEqualTo(0.5);
        assertThat(population.getAll()).allMatch(individual -> individual.needsEvaluation());
        assertThat(Files.readString(log)).contains("2").contains("17").contains("0.5");
    }

    @Test
    void rejectsIncompleteConfigurationAndMismatchedPopulations() {
        assertThatThrownBy(() -> new DPEngine(new int[0], new double[0], new double[0],
                new double[0], 1, 1, 1, 2, "dpe.log"))
                .isInstanceOf(IllegalArgumentException.class);

        DPEngine engine = new DPEngine(new int[]{3}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 2, "dpe.log");
        assertThatThrownBy(() -> engine.apply(PopulationFactory.create(1, 8), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population size");
        assertThatThrownBy(() -> engine.apply(PopulationFactory.create(2, 2), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gene length");
    }

    @Test
    void decodesContiguousParameterSegmentsUsingConfiguredScales() {
        DPEngine engine = new DPEngine(new int[]{4, 8},
                new double[]{0.5, 1.0}, new double[]{-1.0, 2.0},
                new double[4], 2, 1, 1, 2, tempDir.resolve("unused.log").toString());

        assertThat(engine.decodeParameters(new byte[]{(byte) 0b11000001}, 8, false))
                .containsExactly(3.0, 5.0);
    }

    @Test
    void disabledSegmentsAreSkippedAndZoomHandlesCrossByteSegments() throws IOException {
        Population population = PopulationFactory.create(4, 10);
        for (int i = 0; i < population.size(); i++) {
            population.get(i).setGene(new byte[]{0, 0});
            population.get(i).setNeedsEvaluation(false);
        }
        DPEngine engine = new DPEngine(new int[]{-4, 10}, new double[]{1, 1},
                new double[]{0, 0}, new double[4], 2, 1, 1, 4,
                tempDir.resolve("cross-byte.log").toString());

        engine.apply(population, 5, 20);

        assertThat(engine.getLastZoomGeneration()).isEqualTo(5);
        assertThat(population.getAll()).allMatch(individual -> individual.needsEvaluation());
        assertThat(Files.readString(tempDir.resolve("cross-byte.log"))).contains("5").contains("20");
    }

    @Test
    void decodingRejectsInvalidSegmentsAndNonFiniteDecodedValues() {
        DPEngine zeroWidth = new DPEngine(new int[]{2, 2}, new double[]{1, 1},
                new double[]{0, 0}, new double[4], 2, 1, 1, 2, "log");
        assertThatThrownBy(() -> zeroWidth.decodeParameters(new byte[]{0}, 2, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fit within");

        DPEngine invalidEndpoint = new DPEngine(new int[]{1}, new double[]{1},
                new double[]{0}, new double[2], 1, 1, 1, 2, "log");
        assertThatThrownBy(() -> invalidEndpoint.decodeParameters(new byte[]{0}, 1, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fit within");

        DPEngine overflowingScale = new DPEngine(new int[]{2}, new double[]{Double.MAX_VALUE},
                new double[]{Double.MAX_VALUE}, new double[2], 1, 1, 1, 2, "log");
        assertThatThrownBy(() -> overflowingScale.decodeParameters(new byte[]{0b00000011}, 2, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not finite");
    }

    @Test
    void zoomsBothRareAlleleDirectionsAndTheAlternateAllele() throws IOException {
        assertZoomAdjusts(new byte[]{0, 0, 0, 0}, 2, 1, 0.0);
        assertZoomAdjusts(new byte[]{3, 3, 3, 1}, 2, 3, 1.0);
        assertZoomAdjusts(new byte[]{3, 3, 2, 0}, 2, 2, 0.5);
    }

    @Test
    void applyRejectsIndividualsWithInvalidConfiguredGenomeShape() throws IOException {
        DPEngine engine = new DPEngine(new int[]{2}, new double[]{1}, new double[]{0},
                new double[2], 1, 1, 1, 2, tempDir.resolve("shape.log").toString());
        Population nullGene = PopulationFactory.create(2, 2);
        nullGene.get(0).setGene(null);
        assertThatThrownBy(() -> engine.apply(nullGene, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genome shape");

        Population zeroLength = PopulationFactory.create(2, 2);
        ((com.rafaelgpq.jsga.model.SimpleIndividual) zeroLength.get(0)).setGeneLength(0);
        assertThatThrownBy(() -> engine.apply(zeroLength, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gene length");
    }

    private void assertZoomAdjusts(byte[] genes, int geneLength, int expectedZoom, double expectedBase)
            throws IOException {
        Population population = PopulationFactory.create(genes.length, geneLength);
        for (int i = 0; i < genes.length; i++) {
            population.get(i).setGene(new byte[]{genes[i]});
            population.get(i).setFitness(i);
            population.get(i).setNeedsEvaluation(false);
        }
        double[] factors = {1.0};
        double[] bases = {0.0};
        DPEngine engine = new DPEngine(new int[]{2}, factors, bases, new double[2],
                1, 1, 2, genes.length,
                tempDir.resolve("zoom-" + expectedZoom + ".log").toString());

        engine.apply(population, 3, 0);

        assertThat(engine.getLastZoomGeneration()).isEqualTo(3);
        assertThat(factors[0]).isEqualTo(0.5);
        assertThat(bases[0]).isEqualTo(expectedBase);
        assertThat(population.getAll()).allMatch(individual -> individual.needsEvaluation());
    }
}
