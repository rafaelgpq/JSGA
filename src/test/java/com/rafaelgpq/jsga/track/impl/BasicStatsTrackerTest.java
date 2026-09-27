package com.rafaelgpq.jsga.track.impl;

import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BasicStatsTrackerTest {

    @TempDir
    Path tempDir;

    @Test
    void accumulatesBestAndWritesCsvRowsWhenLoggingEnabled() throws Exception {
        Path output = tempDir.resolve("stats.csv");
        BasicStatsTracker tracker = new BasicStatsTracker(true, output.toString());
        tracker.incrementTrials();
        tracker.updateBest(5.0);
        tracker.updateBest(3.0);
        tracker.updateBest(4.0);
        tracker.accumulateOnSum(8.0);
        tracker.accumulateOffSum(3.0);
        tracker.dumpCheckpoint();

        assertThat(tracker.getBest()).isEqualTo(3.0);
        assertThat(tracker.shouldDump()).isTrue();
        assertThat(tracker.shouldSaveBest()).isFalse();
        assertThat(Files.readAllLines(output)).containsExactly(
                "Trial,BestFitness,OnlineSum,OfflineSum", "1,3.00000,8.00000,3.00000");
    }

    @Test
    void disabledLoggingDoesNotCreateFilesOrDumpData() throws Exception {
        Path output = tempDir.resolve("disabled.csv");
        BasicStatsTracker tracker = new BasicStatsTracker(false, output.toString());
        tracker.incrementTrials();
        tracker.updateBest(-2.0);
        tracker.dumpCheckpoint();

        assertThat(tracker.shouldDump()).isFalse();
        assertThat(tracker.getBest()).isEqualTo(-2.0);
        assertThat(Files.exists(output)).isFalse();
        tracker.saveBest(PopulationFactory.create(1, 1).get(0));
    }

    @Test
    void logsInitializationAndAppendFailuresWithoutThrowing() throws Exception {
        Path directory = Files.createDirectory(tempDir.resolve("is-a-directory"));
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream original = System.err;
        try (PrintStream capture = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setErr(capture);
            BasicStatsTracker tracker = new BasicStatsTracker(true, directory.toString());
            tracker.dumpCheckpoint();
        } finally {
            System.setErr(original);
        }

        assertThat(errors.toString(StandardCharsets.UTF_8))
                .contains("Failed to initialize output file")
                .contains("Failed to write checkpoint");
    }
}
