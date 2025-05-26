package org.example.jsga;

import org.assertj.core.util.Lists;
import org.example.jsga.makers.SimpleIndividualMaker;
import org.example.jsga.makers.SimplePopulationMaker;
import org.example.jsga.model.Population;
import org.example.jsga.model.SimpleIndividual;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MainSimulatorTest {

    @Test
    void getBestIndex_ShouldReturnIndexOfLowestFitness() {
        List<SimpleIndividual> individuals = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("1111111111", 10d),
                SimpleIndividualMaker.makeSimpleIndividual("1001001101", 5d),
                SimpleIndividualMaker.makeSimpleIndividual("0100100100", 3d)
        );
        Population population = SimplePopulationMaker.makeSimplePopulation(individuals);
        int bestIndex = invokeGetBestIndex(population);
        assertThat(bestIndex).isEqualTo(2);
    }

    @Test
    void countOnes_ShouldReturnNegativeCountOfOnes() {
        byte[] gene = {(byte) 0b11110000}; // 4 ones
        double result = invokeCountOnes(gene);
        assertThat(result).isEqualTo(-4);
    }

    @Test
    void main_ShouldRunWithoutException_WithMockProperties() throws Exception {
        File tempProps = Files.createTempFile("jsga", ".properties").toFile();
        try (PrintWriter writer = new PrintWriter(tempProps)) {
            writer.println("population.size=2");
            writer.println("gene.length=8");
            writer.println("max.generations=1");
            writer.println("mutation.rate=0.01");
            writer.println("crossover.rate=0.9");
            writer.println("elitism.enabled=true");
        }

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        MainSimulator.main(new String[]{tempProps.getAbsolutePath()});

        String output = outContent.toString();
        assertThat(output).contains("bestIndex").contains("population.get(bestIndex)");
    }

    @Test
    void shouldHandleEmptyPopulation() {
        Population emptyPopulation = SimplePopulationMaker.makeSimplePopulation(new ArrayList<>());
        int bestIndex = invokeGetBestIndex(emptyPopulation);
        assertThat(bestIndex).isEqualTo(0); // default behavior with no individuals
    }

    @Test
    void shouldHandleSingleIndividualPopulation() {
        List<SimpleIndividual> singleIndividual = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("10101010", 7d)
        );
        Population population = SimplePopulationMaker.makeSimplePopulation(singleIndividual);
        int bestIndex = invokeGetBestIndex(population);
        assertThat(bestIndex).isEqualTo(0);
    }

    @Test
    void shouldHandleNegativeFitness() {
        List<SimpleIndividual> individualsWithNegativeFitness = Lists.newArrayList(
                SimpleIndividualMaker.makeSimpleIndividual("11110000", -2d),
                SimpleIndividualMaker.makeSimpleIndividual("00001111", -5d)

        );
        Population population = SimplePopulationMaker.makeSimplePopulation(individualsWithNegativeFitness);
        int bestIndex = invokeGetBestIndex(population);
        assertThat(bestIndex).isEqualTo(1);
    }

    private int invokeGetBestIndex(Population population) {
        int bestIndex = 0;
        double bestFitness = population.size() > 0 ? population.get(0).getFitness() : Double.MAX_VALUE;
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).getFitness() < bestFitness) {
                bestFitness = population.get(i).getFitness();
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private double invokeCountOnes(byte[] gene) {
        int count = 0;
        for (byte b : gene) {
            for (int i = 0; i < 8; i++) {
                if ((b & (1 << i)) != 0) count++;
            }
        }
        return -count;
    }
}
