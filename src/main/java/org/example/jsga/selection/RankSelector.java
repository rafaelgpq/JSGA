package org.example.jsga.selection;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankSelector implements Selection {
    @Override
    public void select(Population from, Population to) {
        List<Individual> sorted = from.getAll().stream()
                .sorted(Comparator.comparingDouble(Individual::getFitness))
                .collect(Collectors.toList());
        int size = sorted.size();
        for (int i = 0; i < to.size(); i++) {
            int idx = (int) (i * size / (double) to.size());
            to.set(i, sorted.get(idx).clone());
        }
    }
}
