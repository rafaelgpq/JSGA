package com.rafaelgpq.jsga.selection;

import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.RandomUtils;

public class RouletteSelector implements Selection {
    @Override
    public void select(Population from, Population to) {
        if (SelectionSupport.isEmptyTarget(from, to)) return;
        double[] weights = SelectionWeights.forMinimization(from);
        for (int i = 0; i < to.size(); i++) {
            double pointer = RandomUtils.nextDouble();
            int selectedIndex = weights.length - 1;
            for (int sourceIndex = 0; sourceIndex < weights.length; sourceIndex++) {
                pointer -= weights[sourceIndex];
                if (pointer < 0.0) {
                    selectedIndex = sourceIndex;
                    break;
                }
            }
            SelectionSupport.copySelected(from.get(selectedIndex), to, i);
        }
    }
}
