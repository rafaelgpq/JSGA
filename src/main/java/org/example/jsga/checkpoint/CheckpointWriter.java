package org.example.jsga.checkpoint;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.DecodeUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes a checkpoint file that captures GA execution state.
 */
public class CheckpointWriter {

    public void writeCheckpoint(String path,
                                Population population,
                                int generation,
                                double[] fitnessWindow,
                                long[] randomState,
                                int rngPosition,
                                double[][] dpeState,
                                boolean saveBest) throws IOException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {

            // Write metadata header (simplified from FORM_CKPT)
            writer.printf("Generation: %d\n", generation);

            if (fitnessWindow != null && fitnessWindow.length > 0) {
                writer.println("\nWindow:");
                for (int i = 0; i < fitnessWindow.length; i++) {
                    if (i % 4 == 0) writer.println();
                    writer.printf("%.8le\t", fitnessWindow[i]);
                }
                writer.println();
            }

            writer.println("\nRandom State:");
            for (int i = 0; i < randomState.length; i++) {
                if (i % 4 == 0) writer.println();
                writer.printf("0x%08x\t", randomState[i]);
            }
            writer.println(rngPosition);

            if (dpeState != null) {
                writer.println("\nDPE State:");
                for (double[] row : dpeState) {
                    writer.printf("%.8le\t%.8le\t%.8le\t%.8le\n", row[0], row[1], row[2], row[3]);
                }
            }

            writer.println("\nPopulation:");
            for (int i = 0; i < population.size(); i++) {
                Individual ind = population.get(i);
                boolean[] bits = DecodeUtils.unpack(ind.getGene(), ind.getGeneLength(), 0);
                for (boolean bit : bits) {
                    writer.print(bit ? '1' : '0');
                }
                writer.printf(" %.12le %d\n", ind.getFitness(), ind.needsEvaluation() ? 1 : 0);
            }

            if (saveBest) {
                writer.println("\n# Best individual tracking required\n");
                // Extend here to write PrintBest() output
            }
        }
    }
}

