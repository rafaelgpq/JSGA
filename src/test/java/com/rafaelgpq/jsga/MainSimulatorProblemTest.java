package com.rafaelgpq.jsga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import com.rafaelgpq.jsga.checkpoint.CheckpointReader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class MainSimulatorProblemTest {

    @TempDir
    Path tempDir;

    @Test
    void simulatorUsesConfiguredParameterizedProblemAndDpe() throws Exception {
        Path dpeLog = tempDir.resolve("dpe.log");
        Properties properties = baseProperties();
        properties.setProperty("gene.length", "8");
        properties.setProperty("initialization.genes", "00000000,11111111,01010101,10101010");
        properties.setProperty("problem", "com.rafaelgpq.jsga.problem.SphereProblem");
        properties.setProperty("dpe.enabled", "true");
        properties.setProperty("dpe.positions", "4,8");
        properties.setProperty("dpe.factors", "0.1,0.1");
        properties.setProperty("dpe.bases", "-1,-1");
        properties.setProperty("dpe.frequency", "1");
        properties.setProperty("dpe.few.threshold", "2");
        properties.setProperty("dpe.gray", "true");
        properties.setProperty("dpe.log", dpeLog.toString());
        Path config = writeConfig(properties);

        String output = runSimulator(config);

        assertThat(output).contains("Generation 0 population:").contains("Generation 1 population:");
        assertThat(Files.readString(dpeLog)).contains("1").contains("0.05");
    }

    @Test
    void simulatorRejectsMismatchedProblemAndDpeModes() throws Exception {
        Properties properties = baseProperties();
        properties.setProperty("problem", "com.rafaelgpq.jsga.problem.SphereProblem");
        Path config = writeConfig(properties);

        assertThatThrownBy(() -> MainSimulator.main(new String[]{config.toString()}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dpe.enabled=true");
    }

    @Test
    void simulatorWritesCheckpointAndResumesAtSavedGeneration() throws Exception {
        Path checkpoint = tempDir.resolve("run.chk");
        Properties initial = baseProperties();
        initial.setProperty("checkpoint.enabled", "true");
        initial.setProperty("checkpoint.file", checkpoint.toString());
        initial.setProperty("checkpoint.interval", "1");
        runSimulator(writeConfig(initial));

        assertThat(new CheckpointReader().readCheckpointState(checkpoint.toString(), 13, 4)
                .getGeneration()).isEqualTo(1);
        assertThat(new CheckpointReader().readCheckpointState(checkpoint.toString(), 13, 4)
                .getEvaluationCount()).isEqualTo(4L);
        assertThat(new CheckpointReader().readCheckpointState(checkpoint.toString(), 13, 4)
                .getRandomState()).hasSize(4);

        Properties resumed = baseProperties();
        resumed.setProperty("max.generations", "3");
        resumed.setProperty("restart.enabled", "true");
        resumed.setProperty("restart.file", checkpoint.toString());
        resumed.setProperty("checkpoint.enabled", "true");
        resumed.setProperty("checkpoint.file", checkpoint.toString());
        String output = runSimulator(writeConfig(resumed));

        assertThat(output).contains("Generation 1 population:").contains("Generation 2 population:");
        assertThat(output).doesNotContain("Generation 0 population:");
        assertThat(new CheckpointReader().readCheckpointState(checkpoint.toString(), 13, 4)
                .getGeneration()).isEqualTo(2);
    }

    @Test
    void checkpointRestartReplaysTheSameNextGenerationAsAnUninterruptedRun() throws Exception {
        Path checkpoint = tempDir.resolve("replay.chk");
        Properties firstPart = baseProperties();
        firstPart.setProperty("max.generations", "2");
        firstPart.setProperty("mutation.rate", "0.2");
        firstPart.setProperty("crossover.rate", "1");
        firstPart.setProperty("checkpoint.enabled", "true");
        firstPart.setProperty("checkpoint.file", checkpoint.toString());
        runSimulator(writeConfig(firstPart));

        Properties resumed = baseProperties();
        resumed.setProperty("max.generations", "3");
        resumed.setProperty("mutation.rate", "0.2");
        resumed.setProperty("crossover.rate", "1");
        resumed.setProperty("restart.enabled", "true");
        resumed.setProperty("restart.file", checkpoint.toString());
        String resumedOutput = runSimulator(writeConfig(resumed));

        Properties uninterrupted = baseProperties();
        uninterrupted.setProperty("max.generations", "3");
        uninterrupted.setProperty("mutation.rate", "0.2");
        uninterrupted.setProperty("crossover.rate", "1");
        String uninterruptedOutput = runSimulator(writeConfig(uninterrupted));

        assertThat(generationBlock(resumedOutput, 2))
                .isEqualTo(generationBlock(uninterruptedOutput, 2));
    }

    private static String generationBlock(String output, int generation) {
        String start = "Generation " + generation + " population:";
        int startIndex = output.indexOf(start);
        if (startIndex < 0) return "";
        int endIndex = output.indexOf("Generation " + (generation + 1) + " population:", startIndex);
        if (endIndex < 0) {
            // The run-level "best solutions" summary is printed once after the loop ends and
            // is not part of any single generation's population block, so exclude it here.
            int summaryIndex = output.indexOf("BEST SOLUTIONS FOUND", startIndex);
            endIndex = summaryIndex < 0 ? output.length() : summaryIndex;
        }
        return output.substring(startIndex, endIndex);
    }

    private Properties baseProperties() {
        Properties properties = new Properties();
        properties.setProperty("population.size", "4");
        properties.setProperty("gene.length", "13");
        properties.setProperty("max.generations", "2");
        properties.setProperty("seed", "193");
        properties.setProperty("initialization.type", "manual");
        properties.setProperty("initialization.genes",
                "0000000000000,1111111111111,0101010101010,1010101010101");
        properties.setProperty("selection.type", "rank");
        properties.setProperty("crossover.rate", "0");
        properties.setProperty("mutation.rate", "0");
        properties.setProperty("elitism.enabled", "false");
        properties.setProperty("convergence.enabled", "false");
        properties.setProperty("termination.done.enabled", "false");
        return properties;
    }

    private Path writeConfig(Properties properties) throws Exception {
        Path config = tempDir.resolve("problem.properties");
        try (java.io.OutputStream output = Files.newOutputStream(config)) {
            properties.store(output, null);
        }
        return config;
    }

    private String runSimulator(Path config) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            MainSimulator.main(new String[]{config.toString()});
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
