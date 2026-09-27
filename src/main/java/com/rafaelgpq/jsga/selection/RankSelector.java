package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.util.Comparator;
import java.util.List;

public class RankSelector implements Selection {
    @Override
    public void select(Population from, Population to) {
        if (SelectionSupport.isEmptyTarget(from, to)) {
            return;
        }
        List<Individual> sorted = new java.util.ArrayList<>(from.getAll());
        sorted.sort(Comparator.comparingDouble(Individual::getFitness));
        int size = sorted.size();
        double totalWeight = (double) size * (size + 1) / 2.0;
        for (int i = 0; i < to.size(); i++) {
            double pointer = RandomUtils.nextDouble() * totalWeight;
            int rank = 0;
            while (rank < size - 1 && pointer >= size - rank) {
                pointer -= size - rank;
                rank++;
            }
            SelectionSupport.copySelected(sorted.get(rank), to, i);
        }
    }
}
