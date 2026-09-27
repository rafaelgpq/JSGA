package com.rafaelgpq.jsga.util;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ResourceLock(Resources.SYSTEM_OUT)
class BestIndividualPrinterTest {

    @TempDir
    Path tempDir;

    @Test
    void printsTheMinimumFitnessIndividualAndSilentlyHandlesEmptyPopulation() {
        Population population = population();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            BestIndividualPrinter.printToConsole(population);
            BestIndividualPrinter.printToConsole(PopulationFactory.create(0, 8));
        } finally {
            System.setOut(original);
        }

        assertThat(output.toString(StandardCharsets.UTF_8))
                .isEqualTo("Best Individual (Fitness -2.000000): 03 \n");
    }

    @Test
    void appendsBestIndividualToFileAndDoesNothingForEmptyPopulation() throws Exception {
        Path output = tempDir.resolve("best.txt");
        Files.writeString(output, "previous\n");
        BestIndividualPrinter.saveToFile(population(), output.toString());
        BestIndividualPrinter.saveToFile(PopulationFactory.create(0, 8), output.toString());

        assertThat(Files.readString(output))
                .isEqualTo("previous\nBest Individual (Fitness -2.000000): 03 \n");
    }

    private static Population population() {
        Population population = PopulationFactory.create(2, 8);
        population.get(0).setGene(new byte[]{0x01});
        population.get(0).setFitness(2.0);
        population.get(1).setGene(new byte[]{0x03});
        population.get(1).setFitness(-2.0);
        return population;
    }
}
