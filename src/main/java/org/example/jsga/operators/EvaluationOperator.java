package org.example.jsga.operators;

import org.example.jsga.model.Individual;
import org.example.jsga.model.Population;
import org.example.jsga.track.StatisticsTracker;

/**
 * Evaluates fitness of individuals using a user-defined fitness function.
 */
public class EvaluationOperator {

    private final FitnessFunction fitnessFunction;
    private final StatisticsTracker stats;

    public EvaluationOperator(FitnessFunction fitnessFunction, StatisticsTracker stats) {
        this.fitnessFunction = fitnessFunction;
        this.stats = stats;
    }

    public void evaluate(Population population) {
        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            if (individual.needsEvaluation()) {
                double fitness = fitnessFunction.evaluate(individual.getGene());
                individual.setFitness(fitness);
                individual.setNeedsEvaluation(false);

                stats.incrementTrials();
                stats.updateBest(fitness);
                stats.accumulateOnSum(fitness);
                stats.accumulateOffSum(stats.getBest());

                if (stats.shouldSaveBest()) {
                    stats.saveBest(individual);
                }
                if (stats.shouldDump()) {
                    stats.dumpCheckpoint();
                }
            }
        }
    }

    /**
     * Functional interface for evaluating individuals.
     */
    public interface FitnessFunction {
        double evaluate(byte[] gene);
    }
}
