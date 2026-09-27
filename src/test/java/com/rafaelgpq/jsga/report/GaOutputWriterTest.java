package com.rafaelgpq.jsga.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaOutputWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesNineColumnRowsThatReportGeneratorCanSummarize() throws IOException {
        Path outputPath = tempDir.resolve("nested").resolve("ga_output.txt");

        GaOutputWriter.initialize(outputPath.toString());
        GaOutputWriter.appendGeneration(outputPath.toString(), 0, 4, 1, 2, 0.5, 1.0, 2.0, -3.0, -1.5);
        GaOutputWriter.appendGeneration(outputPath.toString(), 1, 8, 0, 1, 0.25, 1.5, 2.5, -3.5, -2.0);

        List<String> lines = Files.readAllLines(outputPath);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).trim().split("\\s+")).hasSize(9);

        ReportGenerator.Summary summary = ReportGenerator.summarize(outputPath);
        assertThat(summary.getRowCount()).isEqualTo(2);
        assertThat(summary.getMeans()[0]).isEqualTo(0.5); // average Gen column
        assertThat(summary.getMeans()[7]).isEqualTo(-3.25); // average Best column
    }

    @Test
    void initializeTruncatesPreviousContentBeforeANewRun() throws IOException {
        Path outputPath = tempDir.resolve("ga_output.txt");
        GaOutputWriter.initialize(outputPath.toString());
        GaOutputWriter.appendGeneration(outputPath.toString(), 0, 1, 0, 0, 0.0, 1.0, 1.0, 1.0, 1.0);
        assertThat(Files.readAllLines(outputPath)).hasSize(1);

        GaOutputWriter.initialize(outputPath.toString());
        assertThat(Files.readAllLines(outputPath)).isEmpty();
    }

    @Test
    void appendGenerationRejectsInvalidArguments() {
        String path = tempDir.resolve("ga_output.txt").toString();
        assertThatThrownBy(() -> GaOutputWriter.appendGeneration(path, -1, 1, 0, 0, 0.0, 1.0, 1.0, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Generation");
        assertThatThrownBy(() -> GaOutputWriter.appendGeneration(path, 0, -1, 0, 0, 0.0, 1.0, 1.0, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trials");
        assertThatThrownBy(() ->
                GaOutputWriter.appendGeneration(path, 0, 1, 0, 0, Double.NaN, 1.0, 1.0, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() ->
                GaOutputWriter.appendGeneration(path, 0, 1, 0, 0, 0.0, Double.POSITIVE_INFINITY, 1.0, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() ->
                GaOutputWriter.appendGeneration(path, 0, 1, 0, 0, 0.0, 1.0, Double.NaN, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() ->
                GaOutputWriter.appendGeneration(path, 0, 1, 0, 0, 0.0, 1.0, 1.0, Double.NaN, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() ->
                GaOutputWriter.appendGeneration(path, 0, 1, 0, 0, 0.0, 1.0, 1.0, 1.0, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() -> GaOutputWriter.appendGeneration(null, 0, 1, 0, 0, 0.0, 1.0, 1.0, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path must not be empty");
        assertThatThrownBy(() -> GaOutputWriter.initialize(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path must not be empty");
    }

    @Test
    void acceptsARelativeFileNameWithoutAParentDirectory() throws IOException {
        String relativeName = "gaoutputwritertest-" + System.nanoTime() + ".txt";
        try {
            GaOutputWriter.initialize(relativeName);
            GaOutputWriter.appendGeneration(relativeName, 0, 1, 0, 0, 0.0, 1.0, 1.0, 1.0, 1.0);
            assertThat(Files.readAllLines(Path.of(relativeName))).hasSize(1);
        } finally {
            Files.deleteIfExists(Path.of(relativeName));
        }
    }
}
