package org.example.jsga.selection;

public class SelectionFactory {
    public static Selection create(String selectionType, double parameter) {
        switch (selectionType.toLowerCase()) {
            case "tournament":
                return new TournamentSelector(parameter);
            case "roulette":
                return new RouletteSelector();
            case "rank":
                return new RankSelector();
            case "sus":
                return new StochasticUniversalSamplingSelector();
            default:
                System.out.println("[JSGA][Warning] Unknown selection type: " + selectionType + ". Defaulting to TournamentSelector.");
                return new TournamentSelector(parameter);
        }
    }
}
