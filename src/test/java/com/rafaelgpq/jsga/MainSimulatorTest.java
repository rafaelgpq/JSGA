package com.rafaelgpq.jsga;

import org.assertj.core.util.Lists;
import com.rafaelgpq.jsga.makers.SimpleIndividualMaker;
import com.rafaelgpq.jsga.makers.SimplePopulationMaker;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.SimpleIndividual;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class MainSimulatorTest {

    @TempDir
    Path tempDir;

    @Test
    void getBestIndex_ShouldReturnIndexOfLowestFitness() {
        List<SimpleIndividual> individuals = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("1111111111", 10d),
                SimpleIndividualMaker.makeSimpleIndividual("1001001101", 5d),
                SimpleIndividualMaker.makeSimpleIndividual("0100100100", 3d)
        );
        Population population = SimplePopulationMaker.makeSimplePopulation(individuals);
        assertThat(MainSimulator.getBestIndex(population)).isEqualTo(2);
    }

    @Test
    void countOnes_ShouldReturnNegativeCountOfOnes() {
        byte[] gene = {(byte) 0b11110000, (byte) 0b00000011};
        assertThat(MainSimulator.countOnes(gene)).isEqualTo(-6);
    }

    @Test
    void mainRunsFromTheSuppliedConfigurationAndHonorsTheGenerationCap() throws Exception {
        Path tempProps = tempDir.resolve("custom.properties");
        try (PrintWriter writer = new PrintWriter(tempProps.toFile())) {
            writer.println("population.size=2");
            writer.println("gene.length=8");
            writer.println("max.generations=2");
            writer.println("mutation.rate=0");
            writer.println("crossover.rate=0.9");
            writer.println("elitism.enabled=true");
            writer.println("seed=42");
            writer.println("initialization.type=manual");
            writer.println("initialization.genes=00000000,11111111");
            writer.println("trace=true");
        }

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(outContent, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MainSimulator.main(new String[]{tempProps.toString()});
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertThat(output)
                .contains("Population Size = 2")
                .contains("Gene Length = 8")
                .contains("Initialization  = manual")
                .contains("[0] 00000000")
                .contains("[1] 11111111")
                .contains("Gen 0, Evaluations 2")
                .contains("Gen 1, Evaluations 2")
                .contains("Random Seed     = 42")
                .doesNotContain("Gen 2,")
                .doesNotContain("Population Size = 50");
    }

    @Test
    void mainStopsAtConfiguredObjectiveEvaluationLimit() throws Exception {
        Path tempProps = tempDir.resolve("trial-limit.properties");
        try (PrintWriter writer = new PrintWriter(tempProps.toFile())) {
            writer.println("population.size=2");
            writer.println("gene.length=8");
            writer.println("max.generations=5");
            writer.println("mutation.rate=0");
            writer.println("crossover.rate=0");
            writer.println("elitism.enabled=false");
            writer.println("selection.type=rank");
            writer.println("initialization.type=manual");
            writer.println("initialization.genes=00000000,11111111");
            writer.println("termination.trial_limit=2");
            writer.println("termination.stagnation.generations=0");
            writer.println("convergence.enabled=false");
            writer.println("trace=true");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MainSimulator.main(new String[]{tempProps.toString()});
        } finally {
            System.setOut(originalOut);
        }

        String outputText = output.toString(StandardCharsets.UTF_8);
        assertThat(outputText)
                .contains("Gen 0, Evaluations 2")
                .contains("Done termination condition met at generation 0")
                .doesNotContain("Gen 1,");
    }

    @Test
    void mainStopsAfterConfiguredStagnationAndAllowsDisablingEarlyTermination() throws Exception {
        Path stoppingConfig = tempDir.resolve("stagnation.properties");
        writeTerminationProperties(stoppingConfig, true, 2);
        String stoppingOutput = runAndCapture(stoppingConfig);
        assertThat(stoppingOutput)
                .contains("Done termination condition met at generation 2")
                .doesNotContain("Gen 3,");

        Path nonStoppingConfig = tempDir.resolve("termination-disabled.properties");
        writeTerminationProperties(nonStoppingConfig, false, 2);
        String nonStoppingOutput = runAndCapture(nonStoppingConfig);
        assertThat(nonStoppingOutput)
                .doesNotContain("Done termination condition met")
                .contains("Gen 4, Evaluations 2")
                .doesNotContain("Gen 5,");
    }

    @Test
    void mainRejectsInvalidConfigurationBeforePrintingOrStartingTheRun() throws Exception {
        Path tempProps = tempDir.resolve("invalid.properties");
        try (PrintWriter writer = new PrintWriter(tempProps.toFile())) {
            writer.println("population.size=0");
        }

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(outContent, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            assertThatThrownBy(() -> MainSimulator.main(new String[]{tempProps.toString()}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("population.size");
        } finally {
            System.setOut(originalOut);
        }
        assertThat(outContent.toByteArray()).isEmpty();
    }

    @Test
    void mainRejectsMoreThanOneConfigurationPath() {
        assertThatThrownBy(() -> MainSimulator.main(new String[]{"one.properties", "two.properties"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usage");
    }

    @Test
    void getBestIndexRejectsEmptyAndNullPopulationsAndNonFiniteFitness() {
        Population emptyPopulation = SimplePopulationMaker.makeSimplePopulation(List.of());
        assertThatThrownBy(() -> MainSimulator.getBestIndex(emptyPopulation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty population");
        assertThatThrownBy(() -> MainSimulator.getBestIndex(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population");

        List<SimpleIndividual> invalidIndividuals = List.of(
                SimpleIndividualMaker.makeSimpleIndividual("00000000", Double.NaN));
        assertThatThrownBy(() -> MainSimulator.getBestIndex(
                SimplePopulationMaker.makeSimplePopulation(invalidIndividuals)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
    }

    @Test
    void shouldHandleSingleIndividualPopulation() {
        List<SimpleIndividual> singleIndividual = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("10101010", 7d)
        );
        Population population = SimplePopulationMaker.makeSimplePopulation(singleIndividual);
        assertThat(MainSimulator.getBestIndex(population)).isZero();
    }

    @Test
    void getBestIndexShouldSelectTheLowestOfNegativeFitnessValues() {
        List<SimpleIndividual> individualsWithNegativeFitness = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("11110000", -2d),
                SimpleIndividualMaker.makeSimpleIndividual("00001111", -5d)

        );
        Population population = SimplePopulationMaker.makeSimplePopulation(individualsWithNegativeFitness);
        assertThat(MainSimulator.getBestIndex(population)).isEqualTo(1);
    }

    @Test
    void countOnesRejectsNullGene() {
        assertThatThrownBy(() -> MainSimulator.countOnes(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene");
    }

    private void writeTerminationProperties(Path path, boolean enabled, int stagnationLimit) throws Exception {
        try (PrintWriter writer = new PrintWriter(path.toFile())) {
            writer.println("population.size=2");
            writer.println("gene.length=8");
            writer.println("max.generations=5");
            writer.println("mutation.rate=0");
            writer.println("crossover.rate=0");
            writer.println("elitism.enabled=false");
            writer.println("selection.type=rank");
            writer.println("initialization.type=manual");
            writer.println("initialization.genes=00000000,00000000");
            writer.println("termination.done.enabled=" + enabled);
            writer.println("termination.trial_limit=0");
            writer.println("termination.stagnation.generations=" + stagnationLimit);
            writer.println("convergence.enabled=false");
            writer.println("trace=true");
        }
    }

    private String runAndCapture(Path configPath) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MainSimulator.main(new String[]{configPath.toString()});
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
