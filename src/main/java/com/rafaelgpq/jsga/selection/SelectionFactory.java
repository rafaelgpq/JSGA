package com.rafaelgpq.jsga.selection;

import java.util.Locale;
import java.util.Objects;

public class SelectionFactory {
    public static Selection create(String selectionType, double parameter) {
        String type = Objects.requireNonNull(selectionType, "Selection type must not be null.")
                .trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "tournament":
                return new TournamentSelector(parameter);
            case "roulette":
                return new RouletteSelector();
            case "rank":
                return new RankSelector();
            case "sus":
                return new StochasticUniversalSamplingSelector();
            default:
                throw new IllegalArgumentException("Unsupported selection type: '" + selectionType + "'.");
        }
    }
}
