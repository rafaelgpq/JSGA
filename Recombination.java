import java.util.ArrayList;

public final class Recombination {

    /**
     * Default by Two-Points Crossover.
     *
     * @param pop
     * @return
     */
    public static Population crossover(Population pop) {
        return crossover(pop, 2);
    }

    /**
     * @param pop
     * @param choice
     * @return
     */
    public static Population crossover(Population pop, int choice) {
        Population p = null;
        if (choice == 1) {
            p = crossover1P(pop);
        } else if (choice == 2) {
            p = crossover2P(pop);
        }
        return p;
    }

    /**
     * @param pop
     * @return
     */
    public static Population crossover2P(Population pop) {
        String[] firstGenome = pop.getGenome();
        ArrayList<String[]> popAL = pop.getPopulation();
        Randomness.rndInit();

        int crossTimes = 0, pickParent;
        int xPoint1 = 0;
        int xPoint2 = 0;
        int tmp;

        String dad = "", mom = "";
        String kid1 = "", kid2 = "";

        String leftMostDad = "", middleDad = "", rightMostDad = "";
        String leftMostMom = "", middleMom = "", rightMostMom = "";

        if (Constants.C >= Randomness.randomness()) {
            for (String[] parent : popAL) {

                dad = parent[crossTimes];
                pickParent = Randomness.randomBetween(0, Constants.P);
                mom = parent[pickParent];

                xPoint1 = Randomness.randomBetween(0, Constants.L);
                xPoint2 = Randomness.randomBetween(1, Constants.L + 1);

                if (xPoint1 > xPoint2) {
                    tmp = xPoint1;
                    xPoint1 = xPoint2;
                    xPoint2 = tmp;
                } // End of if(xPoint1 > ...

                leftMostDad = dad.substring(0, xPoint1);
                middleDad = dad.substring(xPoint1, xPoint2);
                rightMostDad = dad.substring(xPoint2);

                leftMostMom = mom.substring(0, xPoint1);
                middleMom = mom.substring(xPoint1, xPoint2);
                rightMostMom = mom.substring(xPoint2);

                kid1 = leftMostDad + middleMom + rightMostDad;
                kid2 = leftMostMom + middleDad + rightMostMom;

                pop = pop.addTwoGenomes(firstGenome, kid1, crossTimes, kid2, pickParent);
                ++crossTimes;
            }
        }
        return pop;
    }

    public static Population crossover1P(Population pop) {

        String[] firstGenome = pop.getGenome();
        ArrayList<String[]> popAL = pop.getPopulation();
        Randomness.rndInit();

        int crossTimes = 0, pickParent = 0;
        int xPoint = 0;
        int tmp;

        String dad = "", mom = "";
        String kid1 = "", kid2 = "";

        String leftMostDad = "", rightMostDad = "";
        String leftMostMom = "", rightMostMom = "";

        if (Constants.C >= Randomness.randomness()) {
            for (String[] parent : popAL) {

                dad = parent[crossTimes];
                pickParent = Randomness.randomBetween(0, Constants.P);
                mom = parent[pickParent];

                xPoint = Randomness.randomBetween(0, Constants.L);

                leftMostDad = dad.substring(0, xPoint);
                rightMostDad = dad.substring(xPoint);

                leftMostMom = mom.substring(0, xPoint);
                rightMostMom = mom.substring(xPoint);

                kid1 = leftMostDad + rightMostMom;
                kid2 = leftMostMom + rightMostDad;

                pop = pop.addTwoGenomes(firstGenome, kid1, crossTimes, kid2, pickParent);
                ++crossTimes;
            }
        }
        return pop;

    } // End of crossover1P() method

    /**
     * @param pop
     * @param mutateEverWithZero
     * @return
     */
    public static Population mutate(Population pop, boolean mutateEverWithZero) {
        long muNext = 0L;
        double mutateValueRef = 0d;
        double r = 0d;

        if (mutateEverWithZero) {
            mutateValueRef = 0d;
            System.out.println("Recombination.mutate(): mutateEverWithZero was selected.");
        } else {
            Randomness.rndInit();
            mutateValueRef = Randomness.randomness();
        }

        long bits = Constants.P * Constants.L;
        int individual = 0;
        int gene = 0;
        String[] people = pop.getGenome();
        String[] shouldBe = people;

        if (Constants.M > mutateValueRef) {
            while (muNext < bits) {

                individual = Randomness.randomBetween(0, Constants.P);
                gene = Randomness.randomBetween(0, Constants.L);

                if (people[individual].charAt(gene) == '0') {
                    shouldBe[individual] = replaceBit(people[individual], gene, '1');
                } else {
                    shouldBe[individual] = replaceBit(people[individual], gene, '0');
                }

                if (Constants.M < 1.0) {
                    while ((r = Randomness.randomness()) == 0.0) ; // Turn It Into With The Randomness class.
                    muNext += Math.ceil(Math.log(r) / Math.log(1.0 - Constants.M)) / 2;
                } else {
                    muNext += 1L;
                }
            }
        }
        pop.replacePopulation(shouldBe);
        return pop;
    }

    /**
     * @param chainChars
     * @param index
     * @param ifBy
     * @return
     */
    public static String replaceBit(String chainChars, int index, char ifBy) {
        String outcome = "";
        char[] charArray = chainChars.toCharArray();
        charArray[index] = ifBy;

        for (int u = 0; u < charArray.length; u++) {
            outcome += String.valueOf(charArray[u]);
        }
        return outcome;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Population popToRecombine = new Population(12345);
        System.out.println("\n\t\t\t >>>>>>> POP. BEFORE MUTATION AT THE FIRST GEN.: \t\t\t\t\n");
        popToRecombine.showPop();

        for (int gen = 0; gen < 1000; gen++) {
            Population popMutation = mutate(popToRecombine, false);

            System.out.println("\n\t\t\t >>>>>>> POP. AFTER MUTATION AT GEN. #" + gen + ": \t\t\t\t\n");
            popMutation.showPop();
        }
        System.exit(0);
    }

}
