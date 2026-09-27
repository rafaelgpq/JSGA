package com.rafaelgpq.jsga.analysis;

import com.rafaelgpq.jsga.makers.SimplePopulationMaker;
import com.rafaelgpq.jsga.model.Population;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConvergenceCheckerTest {

    @Test
    void analyze_ShouldIdentifyLostAndConvergedBits() {
        // Bit 0 is fixed at 1; each other bit occurs as both 0 and 1.
        byte[] gene1 = {(byte) 0b00000001};
        byte[] gene2 = {(byte) 0b11111111};
        byte[] gene3 = {(byte) 0b01010101};
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene1, gene2, gene3));

        ConvergenceChecker checker = new ConvergenceChecker(8, 0, true, 5);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(1);
        assertThat(checker.getConvergedBits()).isEqualTo(1);
        assertThat(checker.getBias()).isBetween(0.0, 1.0);
    }

    @Test
    void analyze_ShouldHandleFullyConvergedPopulation() {
        byte[] gene = {(byte) 0b11111111}; // All bits 1
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene, gene, gene));

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true, 5);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(8); // All bits converged
        assertThat(checker.getConvergedBits()).isEqualTo(8);
        assertThat(checker.getBias()).isEqualTo(1.0);
    }

    @Test
    void analyze_ShouldHandleNoConvergence() {
        byte[] gene1 = {(byte) 0b00000000};
        byte[] gene2 = {(byte) 0b11111111};
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene1, gene2));

        ConvergenceChecker checker = new ConvergenceChecker(8, 0, true, 5);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(0);
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(0.5);
    }

    @Test
    void analyze_ShouldSkipIfDisabled() {
        byte[] gene = {(byte) 0b11111111};
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene, gene));

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, false, 5);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(0);
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(0.0);
    }

    @Test
    void analyze_ShouldHandleEdgeCases() {
        // An empty population has no measured or converged loci.
        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true, 5);
        Population emptyPopulation = SimplePopulationMaker.createPopulationWithGenes(List.of());
        checker.analyze(emptyPopulation);
        assertThat(checker.getLostBits()).isEqualTo(0);
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(0.0);

        // Edge case: Large gene length
        byte[] largeGene = new byte[16]; // 128 bits, all 0
        Population largeGenePopulation = SimplePopulationMaker.createPopulationWithGenes(List.of(largeGene, largeGene));
        ConvergenceChecker largeChecker = new ConvergenceChecker(128, 1, true, 5);
        largeChecker.analyze(largeGenePopulation);
        assertThat(largeChecker.getLostBits()).isEqualTo(128);
    }

    @Test
    void analyzeRejectsPopulationWithGenesShorterThanConfiguredLength() {
        Population population = SimplePopulationMaker.createPopulationWithGenes(
                List.of(new byte[]{0}));
        ConvergenceChecker checker = new ConvergenceChecker(9, 0, true, 0);

        assertThatThrownBy(() -> checker.analyze(population))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configured gene length 9");
    }

    @Test
    void constructorRejectsInvalidConfiguration() {
        assertThatThrownBy(() -> new ConvergenceChecker(0, 0, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gene length");
        assertThatThrownBy(() -> new ConvergenceChecker(8, -1, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Few threshold");
        assertThatThrownBy(() -> new ConvergenceChecker(8, 0, true, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimum generations");
    }

    @Test
    void analyzeRejectsNullPopulation() {
        ConvergenceChecker checker = new ConvergenceChecker(8, 0, true, 0);

        assertThatThrownBy(() -> checker.analyze(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population");
    }

    @Test
    void configuredStagnationThresholdControlsConvergenceAndZeroDisablesIt() {
        Population population = SimplePopulationMaker.createPopulationWithGenes(
                List.of(new byte[]{0}, new byte[]{0}));
        ConvergenceChecker checker = new ConvergenceChecker(8, 0, true, 0, 0.01, 2);

        assertThat(checker.hasConverged(population, 0)).isFalse();
        assertThat(checker.hasConverged(population, 1)).isFalse();
        assertThat(checker.hasConverged(population, 2)).isTrue();

        ConvergenceChecker disabled = new ConvergenceChecker(8, 0, true, 0, 0.01, 0);
        assertThat(disabled.hasConverged(population, 10)).isFalse();
    }

    @Test
    void rejectsInvalidThresholdsGenerationsAndPopulationFitness() {
        assertThatThrownBy(() -> new ConvergenceChecker(8, 0, true, 0, -0.1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("threshold");
        assertThatThrownBy(() -> new ConvergenceChecker(8, 0, true, 0, Double.NaN, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("threshold");
        assertThatThrownBy(() -> new ConvergenceChecker(8, 0, true, 0, 0.1, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stagnant");
        ConvergenceChecker checker = new ConvergenceChecker(8, 0, true, 0, 0.1, 1);
        assertThatThrownBy(() -> checker.hasConverged(
                SimplePopulationMaker.createPopulationWithGenes(List.of(new byte[]{0})), -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Generation");
        assertThatThrownBy(() -> checker.hasConverged(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Population");

        Population invalidFitness = SimplePopulationMaker.createPopulationWithGenes(List.of(new byte[]{0}));
        invalidFitness.get(0).setFitness(Double.NaN);
        assertThatThrownBy(() -> checker.hasConverged(invalidFitness, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("finite");
    }
}
