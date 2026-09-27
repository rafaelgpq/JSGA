package com.rafaelgpq.jsga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@ResourceLock(Resources.SYSTEM_OUT)
@ResourceLock("com.rafaelgpq.jsga.util.RandomUtils")
class GeneticOperatorIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void simulatorRunsEveryConfiguredCrossoverAndMutationCombination() throws Exception {
        String[] crossoverTypes = {"onepoint", "twopoint", "uniform"};
        String[] mutationTypes = {"bitflip", "adaptivebitflip", "swap", "gaussian"};

        for (String crossoverType : crossoverTypes) {
            for (String mutationType : mutationTypes) {
                Path configPath = writeConfig(crossoverType, mutationType);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                PrintStream originalOut = System.out;
                try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                    System.setOut(capture);
                    MainSimulator.main(new String[]{configPath.toString()});
                } finally {
                    System.setOut(originalOut);
                }

                String text = output.toString(StandardCharsets.UTF_8);
                assertThat(text)
                        .contains("Crossover       = " + crossoverType)
                        .contains("Mutation        = " + mutationType)
                        .contains("Gen 0, Evaluations 4")
                        .contains("Gen 1,");
            }
        }
    }

    @Test
    void simulatorWiresEveryPhaseFourSelectionStrategy() throws Exception {
        for (String selectionType : new String[]{"tournament", "roulette", "rank", "sus"}) {
            Path configPath = writeBaseConfig("selection-" + selectionType);
            Properties properties = load(configPath);
            properties.setProperty("selection.type", selectionType);
            Path savedConfig = save(configPath, properties);

            String output = runSimulator(savedConfig);

            assertThat(output).contains("Selection     = " + selectionType)
                    .contains("Gen 0, Evaluations 4")
                    .contains("Gen 1,");
        }
    }

    @Test
    void simulatorAppliesPartialGenerationalGapAndReevaluatesImmigrants() throws Exception {
        Path configPath = writeBaseConfig("partial-gap");
        Properties properties = load(configPath);
        properties.setProperty("gap.size", "0.25");
        properties.setProperty("crossover.rate", "0");
        properties.setProperty("mutation.rate", "0");
        Path savedConfig = save(configPath, properties);

        String output = runSimulator(savedConfig);

        assertThat(output)
                .contains("Gap Size      = 0.2500")
                .contains("Gen 0, Evaluations 4")
                .contains("Gen 1, Evaluations 4");
    }

    @Test
    void simulatorEvaluatesConfiguredRandomImmigrants() throws Exception {
        Path configPath = writeBaseConfig("immigrants");
        Properties properties = load(configPath);
        properties.setProperty("gap.size", "0.25");
        properties.setProperty("diversity.enabled", "true");
        properties.setProperty("diversity.strategy", "random_immigrants");
        properties.setProperty("random_immigrants.count", "2");
        properties.setProperty("crossover.rate", "0");
        properties.setProperty("mutation.rate", "0");
        Path savedConfig = save(configPath, properties);

        String output = runSimulator(savedConfig);

        assertThat(output)
                .contains("Gen 0, Evaluations 4")
                .contains("Gen 1, Evaluations 6");
    }

    @Test
    void simulatorMigratesIslandsAfterPartialGapCreatesTheNewGeneration() throws Exception {
        Path configPath = writeBaseConfig("island-model");
        Properties properties = load(configPath);
        properties.setProperty("gap.size", "0.25");
        properties.setProperty("diversity.enabled", "true");
        properties.setProperty("diversity.strategy", "island_model");
        properties.setProperty("island.count", "2");
        properties.setProperty("island.migration.interval", "1");
        properties.setProperty("island.migration.size", "1");
        Path savedConfig = save(configPath, properties);

        String output = runSimulator(savedConfig);

        assertThat(output)
                .contains("Gap Size      = 0.2500")
                .contains("Gen 0, Evaluations 4")
                .contains("Gen 1, Evaluations 4");
    }

    @Test
    void simulatorAppliesCrowdingAndFitnessSharingStrategies() throws Exception {
        Path crowdingConfig = writeBaseConfig("crowding");
        Properties crowdingProperties = load(crowdingConfig);
        crowdingProperties.setProperty("diversity.enabled", "true");
        crowdingProperties.setProperty("diversity.strategy", "crowding");
        crowdingProperties.setProperty("crowding.similarity.threshold", "0.3");
        String crowdingOutput = runSimulator(save(crowdingConfig, crowdingProperties));
        assertThat(crowdingOutput).contains("Gen 0, Evaluations 4", "Gen 1,");

        Path sharingConfig = writeBaseConfig("fitness-sharing");
        Properties sharingProperties = load(sharingConfig);
        sharingProperties.setProperty("fitness_sharing.enabled", "true");
        sharingProperties.setProperty("fitness_sharing.niche_radius", "3");
        String sharingOutput = runSimulator(save(sharingConfig, sharingProperties));
        assertThat(sharingOutput).contains("Gen 0, Evaluations 4", "Gen 1,");
    }

    @Test
    void simulatorValidatesDiversityStrategyAndSupportsRandomInitialization() throws Exception {
        Path invalidConfig = writeBaseConfig("invalid-diversity");
        Properties invalidProperties = load(invalidConfig);
        invalidProperties.setProperty("diversity.enabled", "true");
        invalidProperties.setProperty("diversity.strategy", "unknown");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> MainSimulator.main(
                        new String[]{save(invalidConfig, invalidProperties).toString()}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diversity.strategy");

        Path randomConfig = writeBaseConfig("random-initialization");
        Properties randomProperties = load(randomConfig);
        randomProperties.setProperty("initialization.type", "random");
        randomProperties.remove("initialization.genes");
        String output = runSimulator(save(randomConfig, randomProperties));
        assertThat(output).contains("Initialization  = random", "Gen 0, Evaluations 4", "Gen 1,");
    }

    @Test
    void simulatorStopsOnConfiguredConvergenceAndLowDiversity() throws Exception {
        Path convergenceConfig = writeBaseConfig("convergence");
        Properties convergenceProperties = load(convergenceConfig);
        convergenceProperties.setProperty("initialization.genes",
                "0000000000000,0000000000000,0000000000000,0000000000000");
        convergenceProperties.setProperty("convergence.enabled", "true");
        convergenceProperties.setProperty("convergence.min_generations", "0");
        convergenceProperties.setProperty("convergence.maxconv", "1");
        convergenceProperties.setProperty("maxbias", "1.0");
        convergenceProperties.setProperty("sigma.factor", "0.0");
        String convergenceOutput = runSimulator(save(convergenceConfig, convergenceProperties));
        assertThat(convergenceOutput).contains("Convergence detected. Stopping early at generation 1");

        Path diversityConfig = writeBaseConfig("low-diversity");
        Properties diversityProperties = load(diversityConfig);
        diversityProperties.setProperty("initialization.genes",
                "0000000000000,0000000000000,0000000000000,0000000000000");
        diversityProperties.setProperty("convergence.enabled", "true");
        diversityProperties.setProperty("maxbias", "1.0");
        diversityProperties.setProperty("sigma.factor", "0.1");
        String diversityOutput = runSimulator(save(diversityConfig, diversityProperties));
        assertThat(diversityOutput).contains("Population diversity too low");
    }

    @Test
    void simulatorWritesOptionalGaOutputReportThatReportGeneratorCanSummarize() throws Exception {
        Path reportPath = tempDir.resolve("ga_output.txt");
        Path configPath = writeBaseConfig("report");
        Properties properties = load(configPath);
        properties.setProperty("max.generations", "4");
        properties.setProperty("report.enabled", "true");
        properties.setProperty("report.file", reportPath.toString());
        properties.setProperty("report.interval", "1");
        Path savedConfig = save(configPath, properties);

        runSimulator(savedConfig);

        List<String> lines = Files.readAllLines(reportPath);
        assertThat(lines).hasSize(4);
        for (int generation = 0; generation < lines.size(); generation++) {
            String[] columns = lines.get(generation).trim().split("\\s+");
            assertThat(columns).hasSize(9);
            assertThat(Integer.parseInt(columns[0])).isEqualTo(generation);
        }

        com.rafaelgpq.jsga.report.ReportGenerator.Summary summary =
                com.rafaelgpq.jsga.report.ReportGenerator.summarize(reportPath);
        assertThat(summary.getRowCount()).isEqualTo(4);
    }

    @Test
    void simulatorWritesOptionalSchemaMeasurementsToSeparateFile() throws Exception {
        Path schemaPath = tempDir.resolve("schema.txt");
        Path resultsPath = tempDir.resolve("schema-results.txt");
        Files.writeString(schemaPath, "1############\n");
        Path configPath = writeBaseConfig("schema");
        Properties properties = load(configPath);
        properties.setProperty("schema.enabled", "true");
        properties.setProperty("schema.file", schemaPath.toString());
        properties.setProperty("schema.output", resultsPath.toString());
        properties.setProperty("schema.worst_fitness", "0");
        Path savedConfig = save(configPath, properties);

        runSimulator(savedConfig);

        assertThat(Files.readString(schemaPath)).isEqualTo("1############\n");
        assertThat(Files.readAllLines(resultsPath)).hasSize(4);
    }

    private Path writeConfig(String crossoverType, String mutationType) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("population.size", "4");
        properties.setProperty("gene.length", "13");
        properties.setProperty("max.generations", "2");
        properties.setProperty("seed", "193");
        properties.setProperty("initialization.type", "manual");
        properties.setProperty("initialization.genes", "0000000000000,1111111111111,0101010101010,1010101010101");
        properties.setProperty("selection.type", "rank");
        properties.setProperty("crossover.type", crossoverType);
        properties.setProperty("crossover.rate", "1");
        properties.setProperty("mutation.type", mutationType);
        properties.setProperty("mutation.rate", "1");
        properties.setProperty("mutation.adaptive", "true");
        properties.setProperty("elitism.enabled", "false");
        properties.setProperty("convergence.enabled", "false");
        properties.setProperty("termination.done.enabled", "false");
        properties.setProperty("trace", "true");

        Path configPath = tempDir.resolve(crossoverType + "-" + mutationType + ".properties");
        try (java.io.OutputStream output = Files.newOutputStream(configPath)) {
            properties.store(output, null);
        }
        return configPath;
    }

    private Path writeBaseConfig(String name) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("population.size", "4");
        properties.setProperty("gene.length", "13");
        properties.setProperty("max.generations", "2");
        properties.setProperty("seed", "193");
        properties.setProperty("initialization.type", "manual");
        properties.setProperty("initialization.genes", "0000000000000,1111111111111,0101010101010,1010101010101");
        properties.setProperty("selection.type", "rank");
        properties.setProperty("crossover.type", "onepoint");
        properties.setProperty("crossover.rate", "0.0");
        properties.setProperty("mutation.type", "bitflip");
        properties.setProperty("mutation.rate", "0.0");
        properties.setProperty("elitism.enabled", "false");
        properties.setProperty("convergence.enabled", "false");
        properties.setProperty("termination.done.enabled", "false");
        properties.setProperty("trace", "true");
        return save(tempDir.resolve(name + ".properties"), properties);
    }

    private Properties load(Path path) throws Exception {
        Properties properties = new Properties();
        try (java.io.InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

    private Path save(Path path, Properties properties) throws Exception {
        try (java.io.OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, null);
        }
        return path;
    }

    private String runSimulator(Path configPath) throws Exception {
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
