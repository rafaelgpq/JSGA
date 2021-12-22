public final class Selection {

	/**
     * @param people
     * @return
     */
    public static Population select(Population people) {
    	int strategy = Constants.selectionStrategy;
    	if (strategy < 1 || strategy > 4) strategy = 1;	// (RouletteWheel) default
        return select(people, Constants.selectionStrategy);
    }
    
    /**
     * @param people
     * @param choice
     * @return
     */
    public static Population select(Population people, int choice) {
    	Population p = new Population();
        if (choice == 1) {
            p = selectWithRouletteWheel(people);
        } else if (choice == 2) {
            p = selectWithTournament(people);
        } else if (choice == 3) {
            p = selectWithRankBased(people);
        } else if (choice == 4) {
            p = selectWithFitnessSharing(people);
        }
        return p;
    }

    /**
     * @param people
     * @return
     */
    public static Population selectWithRouletteWheel(Population people) {
    	Population p;
        Double[] fitness = Evaluation.evaluate(people);
//        people.showPop();
        int[] selected = spinningTheWheel(fitness);
        for (int kk=0; kk<selected.length; kk++) {
//        	System.out.println("Selection.selectWithRouletteWheel(): selected[" + kk + "] = " + selected[kk]);
	}
        p = people.selectedPopulation(selected);
        return p;
    }

    private static int[] spinningTheWheel(Double[] fitness) {
        int i = 0, j = 0, k = 0;
        int[] selected = new int[Constants.P];
        double sum = 0d, factor = 0d, worst = 0d, pointer = 0d, expected = 0d;
        if (fitness.length != 0) {
            for (sum = 0d, i = 0; i < fitness.length; i++) {
                sum += fitness[i];
                j++;
            }
        }
        // PHI = P / {  Worst*P - Sum } ==> Worst*P - Sum = P / PHI  ==>  Sum = Worst*P - P / PHI = P * { Worst - 1/PHI)
        // ==> Sum / P = Worst - 1/PHI ==> Avg = Worst - 1 / PHI ==> PHI = 1 / ( Worst - Avg) = - 1 / ( Avg - Worst)
        // ==> PHI = - 1 / ( Avg - Worst) where Avg = Sum(fitness) / P
        worst = Evaluation.findTheWorst(fitness);
        factor = Constants.P / (worst * j - sum);
        Randomness.rndInit();
        pointer = Randomness.randomness();
        k = 0;
        for (sum = 0d, i = 0; i < fitness.length; i++) {
            if (fitness[i] > worst) {
                expected = factor * (worst - fitness[i]);
            } else {
                expected = 0d;
            }
            for (sum += expected; sum > pointer; pointer++) {
                selected[k++] = i;
            }
        }
        return selected;
    }

    /**
     * @param people
     * @return
     */
    public static Population selectWithTournament(Population people) {
    	Population p;
        int[] selected = new int[Constants.P];
        int count = 0, participant1 = 0, participant2 = 0, setoff = 0;
        double r = 0.0;
        Double[] fitness = Evaluation.evaluate(people);
        Randomness.rndInit();
        System.out.println("Tournament Selection entered.");
        if (fitness.length != 0) {
            count = 0;
            Randomness.rndInit();
            do {
                participant1 = Randomness.randomBetween(0, Constants.P - 1);
                participant2 = Randomness.randomBetween(0, Constants.P - 1);
                if (participant1 == participant2) {
                    setoff = Randomness.randomBetween(0, Constants.P - 1);
                    participant2 = (participant1 + setoff) % Constants.P;
                }
                r = Randomness.randomness();
                if (r > 0.5) {
                    selected[count] = participant1;
                } else {
                    selected[count] = participant2;
                }
                count++;
            } while (count < selected.length);
        } else {
            System.out.println("WARNING: THERE IS NO OLD-POPULATION FITNESS.");
        }
        System.out.println("Tournament Selection completed.");
        p = people.selectedPopulation(selected);
        return p;
    }

    /**
     * @param people
     * @return
     */
    public static Population selectWithRankBased(Population people) {
    	Population p;
        int[] selected = new int[Constants.P];
        Randomness.rndInit();
        for (int i = 0; i < Constants.P; i++) {
            selected[i] = Randomness.randomBetween(0, Constants.P - 1);
        }
        /*
        for (int jj = 0; jj < selected.length; jj++) {
            System.out.println("selectWithRankBased(): selected[" + jj + "] = " + selected[jj]);
        }
        */
        p = people.selectedPopulation(selected);
        return p;
    }

    /**
     * @param people
     * @return
     */
    public static double[][] bitDistance(Population people) {
        String[] genome = people.getGenome();
        return bitDistance(genome);
    }

    /**
     * @param genome
     * @return
     */
    public static double[][] bitDistance(String[] genome) {
        double[][] distance = new double[Constants.P][Constants.P];
        for (int genome1Gene = 0; genome1Gene < distance.length - 1; genome1Gene++) {
            for (int genome2Gene = genome1Gene + 1; genome2Gene < distance[genome1Gene].length; genome2Gene++) {
                for (int gene = 0; gene < Constants.L; gene++) {
                    if (genome[genome1Gene].charAt(gene) == genome[genome2Gene].charAt(gene)) {
                        distance[genome1Gene][genome2Gene] += 0.0;
                    } else {
                        distance[genome1Gene][genome2Gene] += 1.0;
                    }
                    distance[genome2Gene][genome1Gene] = distance[genome1Gene][genome2Gene];
                } // End of for (gene ...
                System.out.println("bitDistance(): distance[" + genome1Gene + "][" + genome2Gene + "] = " + distance[genome1Gene][genome2Gene]);
            } // End of for (genome2Gene ..
        } // End of for (genome1Gene ...
        return distance;
    } // End of bitDistance() method.

    /**
     * @param distance
     * @return
     */
    private static double[][] sharingFunction(double[][] distance) {
        double SHARINGSIGMA = Constants.L;
        double[][] sharingF = new double[Constants.P][Constants.P];
        for (int genome1Gene = 0; genome1Gene < distance.length - 1; genome1Gene++) {
            for (int genome2Gene = genome1Gene + 1; genome2Gene < distance[genome1Gene].length; genome2Gene++) {
                if (distance[genome1Gene][genome2Gene] < SHARINGSIGMA) {
                    sharingF[genome1Gene][genome2Gene] = 1.0 - distance[genome1Gene][genome2Gene] / SHARINGSIGMA;
                } else {
                    sharingF[genome1Gene][genome2Gene] = 0.0;
                }
                sharingF[genome2Gene][genome1Gene] = sharingF[genome1Gene][genome2Gene];
            } // End of for (genome2Gene ..
        } // End of for (genome1Gene ...
        return sharingF;
    }

    /**
     * @param sharingFunction
     * @return
     */
    private static double[] nicheCount(double[][] sharingFunction) {
        double[] niche = new double[Constants.P];
        for (int individual1 = 0; individual1 < sharingFunction.length; individual1++) {
            for (int individual2 = 0; individual2 < sharingFunction[individual1].length; individual2++) {
                niche[individual1] += sharingFunction[individual1][individual2];
            }
            System.out.println("nicheCount(): niche[" + individual1 + "] = " + niche[individual1]);
        }
        return niche;
    }

    /**
     * @param people
     * @return
     */
    public static Population selectWithFitnessSharing(Population people) {
    	Population p;
        Double[] fitness = Evaluation.evaluate(people);
        double[][] bitDistance = bitDistance(people);
        double[][] sharingFunction = sharingFunction(bitDistance);
        double[] nicheCount = nicheCount(sharingFunction);
        for (int individual = 0; individual < fitness.length; individual++) {
            fitness[individual] = (nicheCount[individual] != 0d)
                    ? fitness[individual] / nicheCount[individual]
                    : fitness[individual];
            System.out.println("selectWithFitnessSharing() [AFTER SHARING]: fitness[" + individual + "] = " + fitness[individual]);
        }
        int[] selected = spinningTheWheel(fitness);
        p = people.selectedPopulation(selected);
        return p;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Population p = new Population(1);
        p = selectWithFitnessSharing(p);
        Population p2 = new Population(1);
        p2 = selectWithRouletteWheel(p2);
        Population p3 = new Population(1);
        p3 = selectWithRankBased(p3);
        System.exit(0);
    }
}
