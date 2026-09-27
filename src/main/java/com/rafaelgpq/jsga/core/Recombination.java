package com.rafaelgpq.jsga.core;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.recombine.crossover.Crossover;
import com.rafaelgpq.jsga.recombine.mutation.Mutation;
import com.rafaelgpq.jsga.recombine.GeneticOperatorSupport;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Recombination {

    private Crossover crossover;
    private Mutation mutation;
    private double crossoverRate;
    private boolean configured;

    public void configure(Crossover crossover, double crossoverRate, Mutation mutation) {
        Crossover validatedCrossover = Objects.requireNonNull(crossover, "Crossover must not be null.");
        double validatedCrossoverRate =
                GeneticOperatorSupport.requireProbability("Crossover rate", crossoverRate);
        Mutation validatedMutation = Objects.requireNonNull(mutation, "Mutation must not be null.");
        this.crossover = validatedCrossover;
        this.crossoverRate = validatedCrossoverRate;
        this.mutation = validatedMutation;
        configured = true;
    }

    private void validatePopulation(Population population) {
        if (population.size() == 0) {
            return;
        }
        Individual first = Objects.requireNonNull(population.get(0), "Population individual must not be null.");
        int geneLength = first.getGeneLength();
        GeneticOperatorSupport.copyGene(first, geneLength);
        for (int i = 1; i < population.size(); i++) {
            Individual individual = Objects.requireNonNull(
                    population.get(i), "Population individual at index " + i + " must not be null.");
            if (individual.getGeneLength() != geneLength) {
                throw new IllegalArgumentException("Population individuals must have matching gene lengths.");
            }
            GeneticOperatorSupport.copyGene(individual, geneLength);
        }
    }

    private void applyCrossover(Population population, int offspringCount) {
        int size = offspringCount;
        List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, RandomUtils.getInstance());
        for (int i = 0; i + 1 < size; i += 2) {
            if (RandomUtils.nextDouble() < crossoverRate) {
                int firstIndex = indices.get(i);
                int secondIndex = indices.get(i + 1);
                Individual p1 = population.get(firstIndex);
                Individual p2 = population.get(secondIndex);
                int geneLength = p1.getGeneLength();
                if (geneLength != p2.getGeneLength()) {
                    throw new IllegalArgumentException("Crossover parents must have matching gene lengths.");
                }
                byte[] parentGene1 = GeneticOperatorSupport.copyGene(p1, geneLength);
                byte[] parentGene2 = GeneticOperatorSupport.copyGene(p2, geneLength);
                boolean parent1NeedsEvaluation = p1.needsEvaluation();
                boolean parent2NeedsEvaluation = p2.needsEvaluation();
                var offspring = crossover.crossover(p1.clone(), p2.clone());
                if (offspring == null || offspring.length != 2) {
                    throw new IllegalStateException("Crossover must return exactly two offspring.");
                }
                if (offspring[0] == null || offspring[1] == null) {
                    throw new IllegalStateException("Crossover offspring must not be null.");
                }
                byte[] childGene1 = GeneticOperatorSupport.copyGene(offspring[0], geneLength);
                byte[] childGene2 = GeneticOperatorSupport.copyGene(offspring[1], geneLength);
                offspring[0] = offspring[0].clone();
                offspring[1] = offspring[1].clone();
                offspring[0].setGene(childGene1);
                offspring[1].setGene(childGene2);
                offspring[0].setNeedsEvaluation(
                        parent1NeedsEvaluation || !Arrays.equals(parentGene1, childGene1));
                offspring[1].setNeedsEvaluation(
                        parent2NeedsEvaluation || !Arrays.equals(parentGene2, childGene2));
                population.set(firstIndex, offspring[0]);
                population.set(secondIndex, offspring[1]);
            }
        }
    }

    private void applyMutation(Population population, int offspringCount) {
        int size = offspringCount;
        List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, RandomUtils.getInstance());
        for (int i = 0; i < size; i++) {
            int index = indices.get(i);
            Individual individual = population.get(index);
            int geneLength = individual.getGeneLength();
            byte[] previousGene = GeneticOperatorSupport.copyGene(individual, geneLength);
            boolean needsEvaluation = individual.needsEvaluation();
            var mutated = mutation.mutate(individual.clone());
            if (mutated == null) {
                throw new IllegalStateException("Mutation offspring must not be null.");
            }
            byte[] mutatedGene = GeneticOperatorSupport.copyGene(mutated, geneLength);
            mutated.setGene(mutatedGene);
            mutated.setNeedsEvaluation(needsEvaluation
                    || !Arrays.equals(previousGene, mutatedGene));
            population.set(index, mutated);
        }
    }

    public void apply(Population population, int generation, int maxGenerations) {
        Objects.requireNonNull(population, "Population must not be null.");
        apply(population, generation, maxGenerations, population.size());
    }

    public void apply(Population population, int generation, int maxGenerations, int offspringCount) {
        Objects.requireNonNull(population, "Population must not be null.");
        if (!configured) {
            throw new IllegalStateException("Recombination must be configured before use.");
        }
        if (offspringCount < 0 || offspringCount > population.size()) {
            throw new IllegalArgumentException("Offspring count must be between zero and population size.");
        }
        validatePopulation(population);
        applyCrossover(population, offspringCount);
        applyMutation(population, offspringCount);
    }
}
