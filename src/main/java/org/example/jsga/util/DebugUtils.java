package org.example.jsga.util;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;

public class DebugUtils {

    public static void compareGenerations(Population prev, Population next) {
        System.out.println("🧬 Comparison of Populations:");
        for (int i = 0; i < prev.size(); i++) {
            String prevChrom = toBinaryString(prev.get(i).getGene(), prev.get(i).getGeneLength());
            String nextChrom = toBinaryString(next.get(i).getGene(), next.get(i).getGeneLength());

            System.out.printf("  [%d] FROM %s → TO %s  (fit: %.2f → %.2f)%n",
                    i,
                    prevChrom,
                    nextChrom,
                    prev.get(i).getFitness(),
                    next.get(i).getFitness()
            );
        }
    }

    public static void printPopulation(Population population, int generation) {
        System.out.println("\n📊 Generation " + generation + " population:");
        for (int i = 0; i < population.size(); i++) {
            Individual ind = population.get(i);
            System.out.printf("  [%d] %s  Fitness: %.2f%n",
                    i,
                    toBinaryString(ind.getGene(), ind.getGeneLength()),
                    ind.getFitness());
        }
    }

    public static String toBinaryString(byte[] gene, int geneLength) {
        StringBuilder sb = new StringBuilder();
        int totalBits = 0;
        for (byte b : gene) {
            for (int i = 0; i < 8 && totalBits < geneLength; i++) {
                sb.append((b >> i & 1) == 1 ? '1' : '0');
                totalBits++;
            }
        }
        return sb.toString();
    }
}
