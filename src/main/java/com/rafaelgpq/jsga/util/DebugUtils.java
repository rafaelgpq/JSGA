package com.rafaelgpq.jsga.util;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.track.BestSetManager;

import java.util.List;

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

    /**
     * Prints the ranked archive of best-ever individuals discovered during a run
     * (see {@link BestSetManager}), most-fit first. JSGA is a minimization
     * framework internally: a smaller (more negative) fitness is always better,
     * even for problems phrased as "maximize" (they negate their objective).
     */
    public static void printBestSet(BestSetManager bestSetManager) {
        List<BestSetManager.TrackedIndividual> ranked = bestSetManager.getBestSet();
        System.out.println("\n🏆 BEST SOLUTIONS FOUND (ranked, most-fit first; lower fitness = better) 🏆");
        if (ranked.isEmpty()) {
            System.out.println("  (no individuals were recorded)");
            return;
        }
        for (int i = 0; i < ranked.size(); i++) {
            BestSetManager.TrackedIndividual tracked = ranked.get(i);
            Individual ind = tracked.individual;
            System.out.printf("  #%d  %s  Fitness: %.6f  (generation %d, trial %d)%n",
                    i + 1,
                    toBinaryString(ind.getGene(), ind.getGeneLength()),
                    ind.getFitness(),
                    tracked.generation,
                    tracked.trialNumber);
        }
        BestSetManager.TrackedIndividual champion = ranked.get(0);
        System.out.printf("%n👑 Overall best: %s  Fitness: %.6f  (found at generation %d, trial %d)%n",
                toBinaryString(champion.individual.getGene(), champion.individual.getGeneLength()),
                champion.individual.getFitness(),
                champion.generation,
                champion.trialNumber);
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
