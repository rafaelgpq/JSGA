package com.rafaelgpq.jsga.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock(Resources.SYSTEM_ERR)
class ReportGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void computesColumnMeansAndSampleVariancesAndIgnoresHeader() throws IOException {
        Path report = tempDir.resolve("report.txt");
        Files.writeString(report, "# run summary\n"
                + "Generation A B C D E F G H\n"
                + "1 2 3 4 5 6 7 8 9\n"
                + "3 4 5 6 7 8 9 10 11\n");

        ReportGenerator.Summary summary = ReportGenerator.summarize(report);

        assertThat(summary.getRowCount()).isEqualTo(2);
        assertThat(summary.getMeans()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(summary.getSampleVariances()).containsExactly(2, 2, 2, 2, 2, 2, 2, 2, 2);
    }

    @Test
    void returnsZeroVarianceForOneRowAndRejectsMalformedDataAfterHeader() throws IOException {
        Path report = tempDir.resolve("single.txt");
        Files.writeString(report, "header\n1 2 3 4 5 6 7 8 9\n");
        assertThat(ReportGenerator.summarize(report).getSampleVariances())
                .containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0);

        Files.writeString(report, "header\n1 2 3 4 5 6 7 8 9\nbroken row\n");
        assertThatThrownBy(() -> ReportGenerator.summarize(report))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("line 3");
    }

    @Test
    void rejectsShortNonFiniteAndInvalidNumericRows() throws IOException {
        Path report = tempDir.resolve("invalid.txt");
        Files.writeString(report, "1 2 3\n");
        assertThatThrownBy(() -> ReportGenerator.summarize(report))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("fewer than 9");

        Files.writeString(report, "1 2 3 4 5 6 7 8 NaN\n");
        assertThatThrownBy(() -> ReportGenerator.summarize(report))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Non-finite");

        Files.writeString(report, "1 2 3 4 5 6 7 8 invalid\n");
        assertThatThrownBy(() -> ReportGenerator.summarize(report))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid number");
    }

    @Test
    void ignoresBlankCommentAndHeaderRowsAndReadsExtraColumns() throws IOException {
        Path report = tempDir.resolve("mixed.txt");
        Files.writeString(report, "\n# comment\nGeneration Fitness\n1 2 3 4 5 6 7 8 9 ignored\n");

        ReportGenerator.Summary summary = ReportGenerator.summarize(report);

        assertThat(summary.getRowCount()).isEqualTo(1);
        assertThat(summary.getMeans()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    void rejectsNumericHeaderRowsAfterDataAndRejectsNullOrMissingPaths() throws IOException {
        Path report = tempDir.resolve("numeric-header.txt");
        Files.writeString(report, "1 2 3 4 5 6 7 8 9\nGeneration 1 2 3 4 5 6 7 8\n");
        assertThatThrownBy(() -> ReportGenerator.summarize(report))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("non-numeric report row");
        assertThatThrownBy(() -> ReportGenerator.summarize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
        assertThatThrownBy(() -> ReportGenerator.summarize(tempDir.resolve("missing.txt")))
                .isInstanceOf(IOException.class);
    }

    @Test
    void summaryReturnsDefensiveCopiesAndMainPrintsOrReportsNoData() throws Exception {
        Path report = tempDir.resolve("summary.txt");
        Files.writeString(report, "1 2 3 4 5 6 7 8 9\n");
        ReportGenerator.Summary summary = ReportGenerator.summarize(report);
        summary.getMeans()[0] = 100.0;
        summary.getSampleVariances()[0] = 100.0;
        assertThat(summary.getMeans()[0]).isEqualTo(1.0);
        assertThat(summary.getSampleVariances()[0]).isZero();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            ReportGenerator.main(new String[]{report.toString()});
        } finally {
            System.setOut(originalOut);
        }
        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("GA Summary Report", "Col 1: Avg = 1.00000");

        Path emptyReport = tempDir.resolve("empty.txt");
        Files.writeString(emptyReport, "# nothing\nheader\n");
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try (PrintStream capture = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setErr(capture);
            ReportGenerator.main(new String[]{emptyReport.toString()});
        } finally {
            System.setErr(originalErr);
        }
        assertThat(errors.toString(StandardCharsets.UTF_8)).contains("No data found");
        assertThatThrownBy(() -> ReportGenerator.main(new String[]{"one", "two"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usage");
    }
}
