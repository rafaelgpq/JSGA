package org.example.jsga.checkpoint;

import org.example.jsga.model.Population;

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
        try {
            return checkpointReader.readCheckpoint(checkpointPath, geneLength, populationSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore checkpoint from: " + checkpointPath, e);
        }
    }
}
