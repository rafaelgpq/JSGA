    package org.example.jsga.evaluation;

    import org.example.jsga.model.Individual;
    import org.example.jsga.model.Population;
    import org.example.jsga.track.StatisticsTracker;

    import java.util.List;
    import java.util.function.Function;

    /**
     * Evaluates fitness of individuals using a user-defined fitness function,
     * with optional Fitness Sharing for diversity preservation.
     */
    public class FitnessEvaluator {

        private FitnessFunction fitnessFunction;
        private StatisticsTracker stats;
        private boolean sharingEnabled;
        private double nicheRadius;
        private double alpha = 1.0; // Decay rate for smoother sharing

        public FitnessEvaluator(FitnessFunction fitnessFunction, StatisticsTracker stats) {
            this.fitnessFunction = fitnessFunction;
            this.stats = stats;
        }

        public FitnessEvaluator(Function<byte[], Double> fitnessFunction, boolean sharingEnabled, double nicheRadius) {
            this.fitnessFunction = fitnessFunction::apply;
            this.sharingEnabled = sharingEnabled;
            this.nicheRadius = nicheRadius;
        }

        public void evaluate(Population population) {
            List<Individual> individuals = population.getAll();
            int geneLength = individuals.get(0).getGeneLength(); // Assume uniform length
            double dynamicNicheRadius = nicheRadius * geneLength;

            for (Individual ind : individuals) {
                if (ind.needsEvaluation()) {
                    double rawFitness = fitnessFunction.evaluate(ind.getGene());
                    double adjustedFitness = rawFitness;

                    if (sharingEnabled) {
                        double sharingSum = 0.0;
                        for (Individual other : individuals) {
                            int hammingDist = computeHammingDistance(ind.getGene(), other.getGene());
                            sharingSum += sharingFunction(hammingDist, dynamicNicheRadius);
                        }
                        if (sharingSum > 0) {
                            adjustedFitness = rawFitness / sharingSum;
                        }
                    }

                    ind.setFitness(adjustedFitness);
                    ind.setNeedsEvaluation(false);

                    if (stats != null) {
                        stats.incrementTrials();
                        stats.updateBest(adjustedFitness);
                        stats.accumulateOnSum(adjustedFitness);
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
        }

        private double sharingFunction(int distance, double dynamicNicheRadius) {
            if (distance < dynamicNicheRadius) {
                return 1.0 - Math.pow(distance / dynamicNicheRadius, alpha);
            } else {
                return 0.0;
            }
        }

        private int computeHammingDistance(byte[] gene1, byte[] gene2) {
            int distance = 0;
            for (int i = 0; i < gene1.length; i++) {
                byte xor = (byte) (gene1[i] ^ gene2[i]);
                distance += Integer.bitCount(xor & 0xFF);
            }
            return distance;
        }

        /**
         * Functional interface for evaluating individuals.
         */
        public interface FitnessFunction {
            double evaluate(byte[] gene);
        }
    }
