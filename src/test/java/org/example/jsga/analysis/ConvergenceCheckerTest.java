package org.example.jsga.analysis;

import org.example.jsga.makers.SimplePopulationMaker;
import org.example.jsga.model.Population;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConvergenceCheckerTest {

    @Test
    void analyze_ShouldIdentifyLostAndConvergedBits() {
        // Genes: All individuals have same bits at position 0, others vary
        byte[] gene1 = {(byte) 0b10000000}; // 1 at bit 0
        byte[] gene2 = {(byte) 0b10000001}; // 1 at bit 0 and bit 7
        byte[] gene3 = {(byte) 0b10000010}; // 1 at bit 0 and bit 6
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene1, gene2, gene3));

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(1);  // Only bit 0 is fully converged
        assertThat(checker.getConvergedBits()).isGreaterThanOrEqualTo(1); // At least bit 0 converged
        assertThat(checker.getBias()).isBetween(0.0, 1.0);
    }

    @Test
    void analyze_ShouldHandleFullyConvergedPopulation() {
        byte[] gene = {(byte) 0b11111111}; // All bits 1
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene, gene, gene));

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true);
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

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(0); // No bits fully converged
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(1.0);  // Bias is max since all bits alternate
    }

    @Test
    void analyze_ShouldSkipIfDisabled() {
        byte[] gene = {(byte) 0b11111111};
        Population population = SimplePopulationMaker.createPopulationWithGenes(List.of(gene, gene));

        ConvergenceChecker checker = new ConvergenceChecker(8, 1, false);
        checker.analyze(population);

        assertThat(checker.getLostBits()).isEqualTo(0);
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(0.0);
    }

    @Test
    void analyze_ShouldHandleEdgeCases() {
        // Edge case: Empty population
        ConvergenceChecker checker = new ConvergenceChecker(8, 1, true);
        Population emptyPopulation = SimplePopulationMaker.createPopulationWithGenes(List.of());
        checker.analyze(emptyPopulation);
        assertThat(checker.getLostBits()).isEqualTo(0);
        assertThat(checker.getConvergedBits()).isEqualTo(0);
        assertThat(checker.getBias()).isEqualTo(0.0);

        // Edge case: Large gene length
        byte[] largeGene = new byte[16]; // 128 bits, all 0
        Population largeGenePopulation = SimplePopulationMaker.createPopulationWithGenes(List.of(largeGene, largeGene));
        ConvergenceChecker largeChecker = new ConvergenceChecker(128, 1, true);
        largeChecker.analyze(largeGenePopulation);
        assertThat(largeChecker.getLostBits()).isEqualTo(128);
    }
}
