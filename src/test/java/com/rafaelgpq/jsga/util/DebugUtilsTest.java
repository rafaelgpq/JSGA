package com.rafaelgpq.jsga.util;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.PopulationFactory;
import com.rafaelgpq.jsga.track.BestSetManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ResourceLock(Resources.SYSTEM_OUT)
class DebugUtilsTest {

    @Test
    void rendersLogicalBitsAndPrintsGenerationComparisons() {
        Population previous = population(new byte[]{(byte) 0b10000101}, 3.0);
        Population next = population(new byte[]{0b00000011}, -1.0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            DebugUtils.compareGenerations(previous, next);
            DebugUtils.printPopulation(previous, 7);
        } finally {
            System.setOut(original);
        }

        assertThat(DebugUtils.toBinaryString(new byte[]{(byte) 0b10000101}, 3)).isEqualTo("101");
        assertThat(DebugUtils.toBinaryString(new byte[]{0b00000001, 0b00000001}, 9))
                .isEqualTo("100000001");
        String report = output.toString(StandardCharsets.UTF_8);
        assertThat(report).contains("FROM 101 → TO 110", "fit: 3.00 → -1.00",
                "Generation 7 population", "[0] 101  Fitness: 3.00");
    }

    @Test
    void handlesEmptyPopulationWhenPrintingOrComparing() {
        Population empty = PopulationFactory.create(0, 8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            DebugUtils.compareGenerations(empty, empty);
            DebugUtils.printPopulation(empty, 0);
        } finally {
            System.setOut(original);
        }

        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("Comparison of Populations", "Generation 0 population");
    }

    @Test
    void printBestSetRendersRankedArchiveAndOverallChampion() {
        BestSetManager bestSet = new BestSetManager(5, true);
        Individual worse = individual(new byte[]{0b00000011}, -2.0);
        Individual better = individual(new byte[]{(byte) 0b10000101}, -5.0);
        bestSet.trySave(worse, 0, 10);
        bestSet.trySave(better, 1, 20);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            DebugUtils.printBestSet(bestSet);
        } finally {
            System.setOut(original);
        }

        String report = output.toString(StandardCharsets.UTF_8);
        assertThat(report).contains("BEST SOLUTIONS FOUND",
                "#1  101  Fitness: -5.000000  (generation 1, trial 20)",
                "#2  110  Fitness: -2.000000  (generation 0, trial 10)",
                "Overall best: 101  Fitness: -5.000000  (found at generation 1, trial 20)");
    }

    @Test
    void printBestSetHandlesAnEmptyArchive() {
        BestSetManager bestSet = new BestSetManager(5, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            DebugUtils.printBestSet(bestSet);
        } finally {
            System.setOut(original);
        }

        assertThat(output.toString(StandardCharsets.UTF_8))
                .contains("BEST SOLUTIONS FOUND", "(no individuals were recorded)");
    }

    private static Population population(byte[] gene, double fitness) {
        Population population = PopulationFactory.create(1, 3);
        population.get(0).setGene(gene);
        population.get(0).setFitness(fitness);
        return population;
    }

    private static Individual individual(byte[] gene, double fitness) {
        Population population = population(gene, fitness);
        return population.get(0);
    }
}
