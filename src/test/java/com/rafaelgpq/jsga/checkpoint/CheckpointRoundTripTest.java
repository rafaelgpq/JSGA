package com.rafaelgpq.jsga.checkpoint;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckpointRoundTripTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripsPackedGenomesFitnessAndEvaluationState() throws IOException {
        Population population = PopulationFactory.create(2, 13);
        set(population.get(0), new byte[]{(byte) 0b10100101, 0b00010101}, -7.25, -8.0, false);
        set(population.get(1), new byte[]{0b00000011, 0b00000001}, -2.0, -3.5, true);
        Path path = tempDir.resolve("x");

        new CheckpointWriter().writeCheckpoint(path.toString(), population,
                8, new double[]{-5.0, -4.0}, new long[]{12, Long.MIN_VALUE}, 3,
                new double[][]{{1.0, 2.0, 3.0, 4.0}}, true);
        Population restored = new CheckpointReader().readCheckpoint(path.toString(), 13, 2);

        assertThat(restored.size()).isEqualTo(2);
        for (int i = 0; i < 2; i++) {
            assertThat(restored.get(i).getGene()).containsExactly(population.get(i).getGene());
            assertThat(restored.get(i).getFitness()).isEqualTo(population.get(i).getFitness());
            assertThat(restored.get(i).getRawFitness()).isEqualTo(population.get(i).getRawFitness());
            assertThat(restored.get(i).needsEvaluation()).isEqualTo(population.get(i).needsEvaluation());
        }
    }

    @Test
    void readsLegacyThreeFieldPopulationRecords() throws IOException {
        Path path = tempDir.resolve("legacy.txt");
        Files.writeString(path, "Generation: 3\n\nPopulation:\n101 -2.5 0\n");

        Individual restored = new CheckpointReader().readCheckpoint(path.toString(), 3, 1).get(0);

        assertThat(restored.getGene()).containsExactly((byte) 0b00000101);
        assertThat(restored.getRawFitness()).isEqualTo(-2.5);
        assertThat(restored.needsEvaluation()).isFalse();
    }

    @Test
    void roundTripsRandomSnapshotAndEvaluationCount() throws IOException {
        Population population = PopulationFactory.create(1, 2);
        population.get(0).setGene(new byte[]{3});
        population.get(0).setFitness(-2.0);
        population.get(0).setRawFitness(-2.0);
        Path path = tempDir.resolve("snapshot.txt");
        long[] randomState = {123, 456, 1, Double.doubleToLongBits(0.25)};

        new CheckpointWriter().writeCheckpoint(path.toString(), population, 4,
                null, randomState, 0, null, false, 19);
        CheckpointState state = new CheckpointReader().readCheckpointState(path.toString(), 2, 1);

        assertThat(state.getGeneration()).isEqualTo(4);
        assertThat(state.getEvaluationCount()).isEqualTo(19);
        assertThat(state.getRandomState()).containsExactly(randomState);
        assertThat(state.getPopulation().get(0).getGene()).containsExactly((byte) 3);
    }

    @Test
    void rejectsMissingTruncatedAndMalformedPopulationRecords() throws IOException {
        Path missing = tempDir.resolve("missing.txt");
        Files.writeString(missing, "Generation: 0\n");
        assertThatThrownBy(() -> new CheckpointReader().readCheckpoint(missing.toString(), 3, 1))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Population section");

        Path malformed = tempDir.resolve("malformed.txt");
        Files.writeString(malformed, "Population:\n10x -1 0\n");
        assertThatThrownBy(() -> new CheckpointReader().readCheckpoint(malformed.toString(), 3, 1))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid genome");

        Path truncated = tempDir.resolve("truncated.txt");
        Files.writeString(truncated, "Population:\n101 -1 0\n");
        assertThatThrownBy(() -> new CheckpointReader().readCheckpoint(truncated.toString(), 3, 2))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("expected 2");
    }

    @Test
    void rejectsInvalidCheckpointHeadersPopulationAndRandomState() throws IOException {
        CheckpointReader reader = new CheckpointReader();
        assertThatThrownBy(() -> reader.readCheckpoint(" ", 2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reader.readCheckpoint("path", 0, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reader.readCheckpoint("path", 2, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertCheckpointInvalid("Generation: nope\nPopulation:\n01 0 0\n", "invalid generation");
        assertCheckpointInvalid("Generation: -1\nPopulation:\n01 0 0\n", "must not be negative");
        assertCheckpointInvalid("Generation: 1\nEvaluations: nope\nPopulation:\n01 0 0\n",
                "invalid evaluation count");
        assertCheckpointInvalid("Generation: 1\nEvaluations: -1\nPopulation:\n01 0 0\n",
                "evaluation count must not be negative");
        assertCheckpointInvalid("Population:\n01 0 0\n", "Generation field");
        assertCheckpointInvalid("Generation: 1\nPopulation:\n01 0 0\n02 0 0\n",
                "more individuals");
        assertCheckpointInvalid("Generation: 1\nRandom Snapshot:\n1 2\nPopulation:\n01 0 0\n",
                "exactly four");
        assertCheckpointInvalid("Generation: 1\nRandom Snapshot:\na 2 0 0\nPopulation:\n01 0 0\n",
                "invalid random snapshot");
        assertCheckpointInvalid("Generation: 1\nRandom Snapshot:\n1 -1 0 0\nPopulation:\n01 0 0\n",
                "random snapshot is invalid");
        assertCheckpointInvalid("Generation: 1\nRandom Snapshot:\n1 2 2 0\nPopulation:\n01 0 0\n",
                "random snapshot is invalid");
        assertCheckpointInvalid("Generation: 1\nRandom Snapshot:\n1 2 0 9221120237041090560\n"
                + "Population:\n01 0 0\n", "random snapshot is invalid");
        assertCheckpointInvalid("Generation: 1\nPopulation:\n01 0 0 extra value\n",
                "Invalid checkpoint individual");
        assertCheckpointInvalid("Generation: 1\nPopulation:\n01 NaN 0 0\n", "Invalid fitness");
        assertCheckpointInvalid("Generation: 1\nPopulation:\n01 0 2 0\n", "Invalid fitness");
        assertCheckpointInvalid("Generation: 1\nPopulation:\n01 0 nope 0\n", "Invalid numeric state");
    }

    @Test
    void rejectsInvalidCheckpointWriterState() {
        Population population = PopulationFactory.create(1, 8);
        Path path = tempDir.resolve("invalid.txt");

        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, -1, null, null, 0, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Generation");
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, 0, null, null, 0, new double[][]{{1.0}}, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("four values");

        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(" ", population,
                0, null, null, 0, null, false)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                null, 0, null, null, 0, null, false)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                PopulationFactory.create(0, 8), 0, null, null, 0, null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, 0, null, null, -1, null, false)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, 0, null, null, 0, null, false, -2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, 0, new double[]{Double.NaN}, null, 0, null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CheckpointWriter().writeCheckpoint(path.toString(),
                population, 0, null, null, 0, new double[][]{{Double.POSITIVE_INFINITY, 1, 2, 3}}, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void assertCheckpointInvalid(String contents, String expectedMessage) throws IOException {
        Path path = tempDir.resolve("invalid-" + Math.abs(contents.hashCode()) + ".txt");
        Files.writeString(path, contents);
        assertThatThrownBy(() -> new CheckpointReader().readCheckpoint(path.toString(), 2, 1))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(expectedMessage);
    }

    private static void set(Individual individual, byte[] gene, double fitness,
                            double rawFitness, boolean needsEvaluation) {
        individual.setGene(Arrays.copyOf(gene, gene.length));
        individual.setFitness(fitness);
        individual.setRawFitness(rawFitness);
        individual.setNeedsEvaluation(needsEvaluation);
    }
}
