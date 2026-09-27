package com.rafaelgpq.jsga.dpe;

import com.rafaelgpq.jsga.model.Individual;
import com.rafaelgpq.jsga.model.Population;
import com.rafaelgpq.jsga.util.DecodeUtils;
import com.rafaelgpq.jsga.util.RandomUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

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
        Objects.requireNonNull(gaPosn, "DPE positions must not be null.");
        Objects.requireNonNull(gaFact, "DPE factors must not be null.");
        Objects.requireNonNull(gaBase, "DPE bases must not be null.");
        Objects.requireNonNull(dpeHist, "DPE history must not be null.");
        if (gaGenes <= 0 || gaPosn.length < gaGenes || gaFact.length < gaGenes || gaBase.length < gaGenes
                || dpeHist.length < 2 * gaGenes) {
            throw new IllegalArgumentException("DPE arrays must contain state for every configured gene.");
        }
        if (dpeFreq <= 0 || fewThreshold <= 0 || popSize <= 0) {
            throw new IllegalArgumentException("DPE frequency, few threshold, and population size must be positive.");
        }
        if (dpeLogFile == null || dpeLogFile.trim().isEmpty()) {
            throw new IllegalArgumentException("DPE log file must not be empty.");
        }
        for (int i = 0; i < gaGenes; i++) {
            if (!Double.isFinite(gaFact[i]) || gaFact[i] <= 0 || !Double.isFinite(gaBase[i])) {
                throw new IllegalArgumentException("DPE factors must be positive and bases must be finite.");
            }
        }
        for (double history : dpeHist) {
            if (!Double.isFinite(history) || history < 0) {
                throw new IllegalArgumentException("DPE history values must be finite and non-negative.");
            }
        }
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
        Objects.requireNonNull(population, "Population must not be null.");
        if (generation < 0 || trials < 0) {
            throw new IllegalArgumentException("Generation and trial count must not be negative.");
        }
        if (population.size() != popSize) {
            throw new IllegalArgumentException("Population size does not match the configured DPE population size.");
        }
        if (population.size() == 0) {
            throw new IllegalArgumentException("DPE requires a non-empty population.");
        }
        Individual firstIndividual = Objects.requireNonNull(population.get(0),
                "Population individual at index 0 must not be null.");
        int geneLength = firstIndividual.getGeneLength();
        if (geneLength <= 0) {
            throw new IllegalArgumentException("DPE gene length must be greater than zero.");
        }
        validatePositions(geneLength);
        for (int i = 0; i < popSize; i++) {
            Individual individual = Objects.requireNonNull(population.get(i),
                    "Population individual at index " + i + " must not be null.");
            if (individual.getGeneLength() != geneLength || individual.getGene() == null
                    || individual.getGene().length != (geneLength + 7) / 8) {
                throw new IllegalArgumentException("Population individual at index " + i
                        + " has an invalid genome shape.");
            }
        }

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

                    if (RandomUtils.nextDouble() < 0.5) gene[focus] ^= (1 << bitTail);
                    ind.setNeedsEvaluation(true);
                }

                try (BufferedWriter log = Files.newBufferedWriter(Path.of(dpeLogFile), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    log.write(String.format(java.util.Locale.ROOT, "%4d %7d %3d   % .17g % .17g   %.17g%n",
                            generation, trials, j, gaBase[j], gaBase[j] + range, gaFact[j]));
                }
            }
        }
    }

    public int getLastZoomGeneration() {
        return lastZoom;
    }

    public double[] decodeParameters(byte[] gene, int geneLength, boolean grayEncoded) {
        if (gene == null || geneLength <= 0 || gene.length != (geneLength + 7) / 8) {
            throw new IllegalArgumentException("Genome shape does not match the configured gene length.");
        }
        validatePositions(geneLength);
        boolean[] bits = DecodeUtils.unpack(gene, geneLength);
        double[] parameters = new double[gaGenes];
        int position = 0;
        for (int i = 0; i < gaGenes; i++) {
            int endpoint = Math.abs(gaPosn[i]);
            if (endpoint <= position) {
                throw new IllegalArgumentException("DPE parameter segments must have positive widths.");
            }
            boolean[] encoded = java.util.Arrays.copyOfRange(bits, position, endpoint);
            boolean[] binary = grayEncoded ? DecodeUtils.degray(encoded) : encoded;
            double integer = DecodeUtils.binaryToDouble(binary, false);
            parameters[i] = gaBase[i] + integer * gaFact[i];
            if (!Double.isFinite(parameters[i])) {
                throw new IllegalArgumentException("Decoded DPE parameter is not finite.");
            }
            position = endpoint;
        }
        return parameters;
    }

    private void validatePositions(int geneLength) {
        int position = 0;
        for (int i = 0; i < gaGenes; i++) {
            long endpointLong = Math.abs((long) gaPosn[i]);
            if (endpointLong <= position || endpointLong > geneLength) {
                throw new IllegalArgumentException("DPE parameter segments must fit within the configured gene length.");
            }
            int endpoint = (int) endpointLong;
            if (gaPosn[i] > 0) {
                if (endpoint - position < 2) {
                    throw new IllegalArgumentException("DPE segment positions must fit within the configured gene length.");
                }
            }
            position = endpoint;
        }
    }
}
