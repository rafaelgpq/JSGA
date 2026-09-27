package com.rafaelgpq.jsga.checkpoint;

import com.rafaelgpq.jsga.model.Population;

import java.io.IOException;

/**
 * Handles restoring from a saved checkpoint.
 */
public class RestartManager {

    private final CheckpointReader checkpointReader;
    private final String checkpointPath;

    public RestartManager(String checkpointPath) {
        this.checkpointPath = checkpointPath;
        this.checkpointReader = new CheckpointReader();
    }

    public Population restart(int geneLength, int populationSize) {
        return restartState(geneLength, populationSize).getPopulation();
    }

    public CheckpointState restartState(int geneLength, int populationSize) {
        try {
            return checkpointReader.readCheckpointState(checkpointPath, geneLength, populationSize);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to restore checkpoint from: " + checkpointPath, e);
        }
    }
}
