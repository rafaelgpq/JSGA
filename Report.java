import static java.lang.System.*;

public final class Report {

    /**
     *
     */
    public static void reportHeader() {
        out.println("\n\t * * * * * A Java Simple Genetic Algorithm (JSGA) * * * * * ");
        out.println("\t\t\t* * * JSGA Parameters: * * * \n");
        out.println("\tGenome Length: " + Constants.L);
        out.println("\tPopulation Size: " + Constants.P);
        out.println("\tMax. Generations: " + Constants.maxGeneration);
        out.println("\tCrossover Probability: " + Constants.C);
        out.println("\tMutation Probability: " + Constants.M);
        out.println("\n\n");
    }

    /**
     * 
     * @param population
     * @param generationNumber
     */
    public static void report(Population population, int generationNumber) {
    	Double[] fitness = Evaluation.evaluate(population);
        out.println("\nAvg. Current Performance: " + Evaluation.findFitnessAvg(fitness));
        out.println("Standard Dev. Current Performance: " + Evaluation.findFitnessSigma(fitness));
        if (generationNumber == -1) {
//            out.println("Best Guy \t Best Performance ");
//            out.println(Evaluation.findTheBestGuy(population) + "\t"
//                    + Evaluation.findTheBest(Evaluation.evaluate(population)));
            out.println("\n");
        } else {
            out.println("Generation # \t Best Guy \t Best Performance ");
            out.println(generationNumber + "\t" + Evaluation.findTheBestGuy(fitness)
                    + "\t" + Evaluation.findTheBestFitness(fitness));
            out.println("\n");
        }
    }


}
