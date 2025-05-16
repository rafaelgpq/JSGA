package org.example.jsga.util;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

/**
 * Utility to print or save the best individual from a population.
 */
public class BestIndividualPrinter {

    public static void printToConsole(Population population) {
        Individual best = population.getAll().stream()
                .min(Comparator.comparingDouble(Individual::getFitness))
                .orElse(null);

        if (best != null) {
            System.out.printf("Best Individual (Fitness %.6f): ", best.getFitness());
            for (byte b : best.getGene()) {
                System.out.printf("%02X ", b);
            }
            System.out.println();
        }
    }

    public static void saveToFile(Population population, String path) throws IOException {
        Individual best = population.getAll().stream()
                .min(Comparator.comparingDouble(Individual::getFitness))
                .orElse(null);

        if (best == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(path, true))) {
            writer.printf("Best Individual (Fitness %.6f): ", best.getFitness());
            for (byte b : best.getGene()) {
                writer.printf("%02X ", b);
            }
            writer.println();
        }
    }
}
