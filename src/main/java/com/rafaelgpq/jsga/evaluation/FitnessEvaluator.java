    package com.rafaelgpq.jsga.evaluation;

    import com.rafaelgpq.jsga.diversity.FitnessSharingStrategy;
    import com.rafaelgpq.jsga.model.Individual;
    import com.rafaelgpq.jsga.model.Population;
    import com.rafaelgpq.jsga.track.StatisticsTracker;

    import java.util.List;
    import java.util.Objects;
    import java.util.function.Function;

    /**
     * Evaluates fitness of individuals using a user-defined fitness function,
     * with optional Fitness Sharing for diversity preservation.
     */
    public class FitnessEvaluator {

        private final FitnessFunction fitnessFunction;
        private final StatisticsTracker stats;
        private final boolean sharingEnabled;
        private final double nicheRadius;

        public FitnessEvaluator(FitnessFunction fitnessFunction, StatisticsTracker stats) {
            this.fitnessFunction = Objects.requireNonNull(fitnessFunction, "Fitness function must not be null.");
            this.stats = stats;
            this.sharingEnabled = false;
            this.nicheRadius = 0.0;
        }

        public FitnessEvaluator(Function<byte[], Double> fitnessFunction, boolean sharingEnabled, double nicheRadius) {
            Objects.requireNonNull(fitnessFunction, "Fitness function must not be null.");
            this.fitnessFunction = gene -> Objects.requireNonNull(
                    fitnessFunction.apply(gene), "Fitness function must not return null.");
            this.stats = null;
            this.sharingEnabled = sharingEnabled;
            if (sharingEnabled && (!Double.isFinite(nicheRadius) || nicheRadius <= 0.0)) {
                throw new IllegalArgumentException("Niche radius must be a finite value greater than zero.");
            }
            this.nicheRadius = nicheRadius;
        }

        /**
         * Evaluates all dirty individuals and returns the number of objective calls made.
         */
        public int evaluate(Population population) {
            Objects.requireNonNull(population, "Population must not be null.");
            List<Individual> individuals = population.getAll();
            if (individuals.isEmpty()) {
                return 0;
            }

            int geneLength = individuals.get(0).getGeneLength();
            if (geneLength <= 0) {
                throw new IllegalArgumentException("Gene length must be greater than zero.");
            }
            for (int i = 0; i < individuals.size(); i++) {
                Individual individual = Objects.requireNonNull(individuals.get(i),
                        "Population individual at index " + i + " must not be null.");
                if (individual.getGeneLength() != geneLength
                        || individual.getGene() == null
                        || individual.getGene().length != (geneLength + Byte.SIZE - 1) / Byte.SIZE) {
                    throw new IllegalArgumentException("Individual at index " + i
                            + " has an invalid gene length; expected " + geneLength + " bits.");
                }
            }
            int evaluated = 0;
            for (Individual ind : individuals) {
                if (ind.needsEvaluation()) {
                    double rawFitness = fitnessFunction.evaluate(ind.getGene());
                    if (!Double.isFinite(rawFitness)) {
                        throw new IllegalArgumentException("Fitness function must return a finite value.");
                    }
                    ind.setRawFitness(rawFitness);
                    ind.setFitness(rawFitness);
                    ind.setNeedsEvaluation(false);
                    evaluated++;

                    if (stats != null) {
                        stats.incrementTrials();
                        stats.updateBest(rawFitness);
                        stats.accumulateOnSum(rawFitness);
                        stats.accumulateOffSum(stats.getBest());

                        if (stats.shouldSaveBest()) {
                            stats.saveBest(ind);
                        }
                        if (stats.shouldDump()) {
                            stats.dumpCheckpoint();
                        }
                    }
                }
            }
            if (sharingEnabled) {
                new FitnessSharingStrategy(nicheRadius).apply(population, 0);
            }
            return evaluated;
        }

        /**
         * Functional interface for evaluating individuals.
         */
        public interface FitnessFunction {
            double evaluate(byte[] gene);
        }
    }
