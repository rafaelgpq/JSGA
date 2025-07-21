package org.example.jsga.core;

import org.example.jsga.model.Population;
import org.example.jsga.recombine.crossover.Crossover;
import org.example.jsga.recombine.mutation.Mutation;
import org.example.jsga.util.RandomUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Recombination {

    private Crossover crossover;
    private Mutation mutation;
    private double crossoverRate;

    public void configure(Crossover crossover, double crossoverRate, Mutation mutation) {
        this.crossover = crossover;
        this.crossoverRate = crossoverRate;
        this.mutation = mutation;
    }

    private void applyCrossover(Population population) {
        int size = population.size();
        List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, RandomUtils.getInstance());
        for (int i = 0; i + 1 < size; i += 2) {
            if (RandomUtils.nextDouble() < crossoverRate) {
                var p1 = population.get(indices.get(i));
                var p2 = population.get(indices.get(i + 1));
                var offspring = crossover.crossover(p1, p2);
                population.set(indices.get(i), offspring[0]);
                population.set(indices.get(i + 1), offspring[1]);
                offspring[0].setNeedsEvaluation(true);
                offspring[1].setNeedsEvaluation(true);
            }
        }
    }

    private void applyMutation(Population population) {
        int size = population.size();
        List<Integer> indices = IntStream.range(0, size).boxed().collect(Collectors.toList());
        Collections.shuffle(indices, RandomUtils.getInstance());
        for (int i = 0; i < size; i++) {
            var individual = population.get(indices.get(i));
            var mutated = mutation.mutate(individual);
            population.set(indices.get(i), mutated);
        }
    }

    public void apply(Population population, int generation, int maxGenerations) {
        applyCrossover(population);
        applyMutation(population);
    }
}
