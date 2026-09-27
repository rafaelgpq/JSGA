package com.rafaelgpq.jsga.schema;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void analyzesSchemaCountsAndDoesNotOverwriteTheInputSchema() throws IOException {
        Path schemaFile = tempDir.resolve("schema.txt");
        Files.writeString(schemaFile, "1#0\n");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaFile.toString(), 0.0);
        Population population = populationOfLength(3, new byte[]{0b00000001}, -3.0,
                new byte[]{0b00000011}, -2.0,
                new byte[]{0b00000000}, -1.0);

        analyzer.analyze(population, 0);

        assertThat(Files.readString(schemaFile)).isEqualTo("1#0\n");
        List<String> lines = Files.readAllLines(analyzer.getOutputFile());
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0)).isEqualTo("1#0");
        assertThat(lines.get(1)).contains("Gen").contains("Count");
        assertThat(lines.get(2)).contains("0").contains("2");
    }

    @Test
    void emitsZeroMatchRowsAndUsesTheExplicitOutputPath() throws IOException {
        Path schemaFile = tempDir.resolve("schema.txt");
        Path outputFile = tempDir.resolve("results.txt");
        Files.writeString(schemaFile, "11##\n");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaFile.toString(), outputFile.toString(), 0.0);

        analyzer.analyze(populationOfLength(4, new byte[]{0}, -2.0, new byte[]{1}, -1.0), 0);
        analyzer.analyze(populationOfLength(4, new byte[]{0}, -2.0, new byte[]{1}, -1.0), 1);

        List<String> lines = Files.readAllLines(outputFile);
        assertThat(lines).hasSize(4);
        assertThat(lines.get(2)).contains("0").contains("0");
        assertThat(lines.get(3)).contains("1").contains("0");
    }

    @Test
    void rejectsInvalidSchemaFilesFitnessAndPopulationShapes() throws IOException {
        Path emptySchema = tempDir.resolve("empty.schema");
        Files.writeString(emptySchema, "\n");
        assertThatThrownBy(() -> new SchemaAnalyzer(emptySchema.toString(), 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty");

        Path badSchema = tempDir.resolve("bad.schema");
        Files.writeString(badSchema, "10x\n");
        assertThatThrownBy(() -> new SchemaAnalyzer(badSchema.toString(), 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only");
        assertThatThrownBy(() -> new SchemaAnalyzer(badSchema.toString(), Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");

        Path validSchema = tempDir.resolve("valid.schema");
        Files.writeString(validSchema, "1#\n");
        assertThatThrownBy(() -> new SchemaAnalyzer(validSchema.toString(), validSchema.toString(), 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overwrite");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(validSchema.toString(), 0.0);
        assertThatThrownBy(() -> analyzer.analyze(PopulationFactory.create(0, 2), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        assertThatThrownBy(() -> analyzer.analyze(populationOfLength(2, new byte[]{1}, 1.0), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no greater");
        assertThatThrownBy(() -> analyzer.analyze(populationOfLength(
                2, new byte[]{1}, Double.NaN), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
        assertThatThrownBy(() -> analyzer.analyze(population(new byte[]{1}, -1.0), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema length");
        Population nullMember = populationOfLength(2, new byte[]{1}, -1.0);
        nullMember.set(0, null);
        assertThatThrownBy(() -> analyzer.analyze(nullMember, 0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("index 0");
        Population malformedGenome = populationOfLength(2, new byte[]{1}, -1.0);
        malformedGenome.get(0).setGene(new byte[0]);
        assertThatThrownBy(() -> analyzer.analyze(malformedGenome, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema length");
    }

    @Test
    void requiresStrictlyIncreasingGenerationNumbers() throws IOException {
        Path schemaFile = tempDir.resolve("schema.txt");
        Files.writeString(schemaFile, "##\n");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaFile.toString(), 0.0);
        Population population = populationOfLength(2, new byte[]{0}, -1.0);
        analyzer.analyze(population, 1);

        assertThatThrownBy(() -> analyzer.analyze(population, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly increasing");
        assertThatThrownBy(() -> analyzer.analyze(population, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly increasing");
    }

    @Test
    void firstObservedSchemaCountHasUnitIncrementEvenAfterEmptyGenerations() throws IOException {
        Path schemaFile = tempDir.resolve("schema-first-count.txt");
        Files.writeString(schemaFile, "1\n");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaFile.toString(), 0.0);
        analyzer.analyze(populationOfLength(1, new byte[]{0}, -1.0), 0);
        analyzer.analyze(populationOfLength(1, new byte[]{1}, -1.0), 1);

        List<String> lines = Files.readAllLines(analyzer.getOutputFile());
        assertThat(lines.get(2)).contains("0").contains("0.000");
        assertThat(lines.get(3)).contains("1.000");
    }

    @Test
    void expectedCountFallsBackToObservedCountWhenAllFitnessMatchesWorstValue() throws IOException {
        Path schemaFile = tempDir.resolve("schema-all-worst.txt");
        Files.writeString(schemaFile, "1#\n");
        SchemaAnalyzer analyzer = new SchemaAnalyzer(schemaFile.toString(), 0.0);
        analyzer.analyze(populationOfLength(2,
                new byte[]{1}, 0.0, new byte[]{0}, 0.0), 0);

        String result = Files.readAllLines(analyzer.getOutputFile()).get(2);
        assertThat(result).contains("  1  ", "1.000");
    }

    private static Population population(Object... genesAndFitnesses) {
        return populationOfLength(8, genesAndFitnesses);
    }

    private static Population populationOfLength(int geneLength, Object... genesAndFitnesses) {
        Population population = PopulationFactory.create(genesAndFitnesses.length / 2, geneLength);
        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            individual.setGene((byte[]) genesAndFitnesses[i * 2]);
            individual.setFitness((double) genesAndFitnesses[i * 2 + 1]);
            individual.setNeedsEvaluation(false);
        }
        return population;
    }
}
