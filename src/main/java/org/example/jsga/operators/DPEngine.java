package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.util.RandomUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Implements Dynamic Parameter Encoding (DPE) logic.
 */
public class DPEngine {

    private final int[] gaPosn;
    private final double[] gaFact;
    private final double[] gaBase;
    private final double[] dpeHist;
    private final int gaGenes;
    private final int dpeFreq;
    private final int fewThreshold;
    private final int popSize;
    private final String dpeLogFile;

    private int lastZoom = 0;

    public DPEngine(int[] gaPosn, double[] gaFact, double[] gaBase, double[] dpeHist,
                    int gaGenes, int dpeFreq, int fewThreshold, int popSize, String dpeLogFile) {
        this.gaPosn = gaPosn;
        this.gaFact = gaFact;
        this.gaBase = gaBase;
        this.dpeHist = dpeHist;
        this.gaGenes = gaGenes;
        this.dpeFreq = dpeFreq;
        this.fewThreshold = fewThreshold;
        this.popSize = popSize;
        this.dpeLogFile = dpeLogFile;
    }

    public void apply(Population population, int generation, long trials) throws IOException {
        int posn = 0;

        for (int j = 0; j < gaGenes; j++) {
            if (gaPosn[j] < 1) {
                posn = -gaPosn[j];
                continue;
            }

            int bit1 = posn % 8;
            int focus1 = posn / 8;
            posn++;
            int bit2 = posn % 8;
            int focus2 = posn / 8;

            int one = 0, two = 0;
            for (int i = 0; i < popSize; i++) {
                byte[] gene = population.get(i).getGene();
                if ((gene[focus1] & (1 << bit1)) != 0) one++;
                if ((gene[focus2] & (1 << bit2)) == 0) two++;
            }

            dpeHist[2 * j] = dpeHist[2 * j] * (1.0 - 1.0 / dpeFreq) + one / (double) dpeFreq;
            dpeHist[2 * j + 1] = dpeHist[2 * j + 1] * (1.0 - 1.0 / dpeFreq) + two / (double) dpeFreq;

            one = (int) dpeHist[2 * j];
            two = (int) dpeHist[2 * j + 1];

            int zoom = 0;
            if (one < fewThreshold) zoom = 1;
            else {
                one = popSize - one + 1;
                if (one < fewThreshold) zoom = 3;
                else one = fewThreshold;
            }
            if (two < one) {
                zoom = 2;
                bit1 = bit2;
                focus1 = focus2;
            }

            double range = Math.pow(2, gaPosn[j] - posn) * gaFact[j];
            posn = gaPosn[j];

            if (zoom > 0) {
                lastZoom = generation;
                dpeHist[2 * j] = dpeHist[2 * j + 1] = popSize / 2.0;
                range /= 2.0;
                gaFact[j] /= 2.0;
                gaBase[j] += (zoom - 1) * (range / 2.0);

                int bitTail = (posn - 1) % 8;
                int focusTail = (posn - 1) / 8;

                for (int i = 0; i < popSize; i++) {
                    Individual ind = population.get(i);
                    byte[] gene = ind.getGene();
                    int focus = focus1;
                    int twoBits = (gene[focus] & ((1 << bit1) - 1)) << 1;
                    if (zoom > 1) twoBits ^= (1 << bit1);

                    while (focus < focusTail) {
                        if ((gene[focus + 1] & 0x01) != 0) twoBits ^= 1;
                        gene[focus] = (byte) twoBits;
                        twoBits = gene[++focus] << 1;
                    }
                    gene[focus] &= ~(1 << bitTail);
                    gene[focus] |= twoBits & (1 << bitTail);

                    if (RandomUtils.rand() < 0.5) gene[focus] ^= (1 << bitTail);
                    ind.setNeedsEvaluation(true);
                }

                try (PrintWriter log = new PrintWriter(new FileWriter(dpeLogFile, true))) {
                    log.printf("%4d %7d %3d   % le % le   %le\n",
                            generation, trials, j, gaBase[j], gaBase[j] + range, gaFact[j]);
                }
            }
        }
    }
}
