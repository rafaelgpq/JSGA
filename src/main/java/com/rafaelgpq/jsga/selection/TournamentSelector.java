package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.RandomUtils;

public class TournamentSelector implements Selection {
    private final double selectionPressure;

    public TournamentSelector(double selectionPressure) {
        if (!Double.isFinite(selectionPressure) || selectionPressure <= 0.0 || selectionPressure > 1.0) {
            throw new IllegalArgumentException("Selection pressure must be finite, greater than 0, and at most 1.");
        }
        this.selectionPressure = selectionPressure;
    }

    @Override
    public void select(Population from, Population to) {
        if (SelectionSupport.isEmptyTarget(from, to)) {
            return;
        }
        int tournamentSize = Math.max(1,
                (int) Math.ceil(selectionPressure * from.size()));
        for (int i = 0; i < to.size(); i++) {
            int[] sampledIndices = new int[from.size()];
            for (int candidate = 0; candidate < sampledIndices.length; candidate++) {
                sampledIndices[candidate] = candidate;
            }
            for (int candidate = 0; candidate < tournamentSize; candidate++) {
                int selected = candidate + RandomUtils.nextInt(from.size() - candidate);
                int temp = sampledIndices[candidate];
                sampledIndices[candidate] = sampledIndices[selected];
                sampledIndices[selected] = temp;
            }
            Individual winner = from.get(sampledIndices[0]);
            for (int candidate = 1; candidate < tournamentSize; candidate++) {
                Individual challenger = from.get(sampledIndices[candidate]);
                if (challenger.getFitness() < winner.getFitness()) {
                    winner = challenger;
                }
            }
            SelectionSupport.copySelected(winner, to, i);
        }
    }
}
