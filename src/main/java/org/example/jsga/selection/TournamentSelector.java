package org.example.jsga.selection;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

import java.util.List;

public class TournamentSelector implements Selection {
    private final double selectionPressure;

    public TournamentSelector(double selectionPressure) {
        this.selectionPressure = selectionPressure;
    }

    @Override
    public void select(Population from, Population to) {
        List<Individual> pool = from.getAll();
        for (int i = 0; i < to.size(); i++) {
            Individual winner = pool.get(RandomUtils.nextInt(pool.size()));
            for (int j = 0; j < (int)(selectionPressure * pool.size()); j++) {
                Individual challenger = pool.get(RandomUtils.nextInt(pool.size()));
                if (challenger.getFitness() < winner.getFitness()) {
                    winner = challenger;
                }
            }
            to.set(i, winner.clone());
        }
    }
}
