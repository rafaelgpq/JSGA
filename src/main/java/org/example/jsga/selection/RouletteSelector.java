package org.example.jsga.selection;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

import java.util.List;

public class RouletteSelector implements Selection {
    @Override
    public void select(Population from, Population to) {
        List<Individual> pool = from.getAll();
        double totalFitness = pool.stream().mapToDouble(Individual::getFitness).sum();
        for (int i = 0; i < to.size(); i++) {
            double rand = RandomUtils.nextDouble() * totalFitness;
            double cumulative = 0.0;
            for (Individual ind : pool) {
                cumulative += ind.getFitness();
                if (cumulative >= rand) {
                    to.set(i, ind.clone());
                    break;
                }
            }
        }
    }
}
