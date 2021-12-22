import static java.lang.System.out;

/**
 *
 */
public class JSGAMain {

    public static void main(String[] args) {
        int G = Constants.maxGeneration;
        int generation = 0;
        int fitnessCount;
        Double[] fitness = new Double[] { 0d };

        Population popBeginning = new Population(5321);
        Population popBest = new Population();
        
        Evaluation.showPopAndFitDiploid(popBest);
        
        Report.reportHeader();

        while (generation < G) {
            popBeginning = popBest;
            fitness = Evaluation.evaluate(popBeginning);

/*            
            fitnessCount = 0;
            for (double individual : fitness) {
                out.println("JSGAMain: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX fitness #" + fitnessCount + ": |" + individual + "| at generation = " + generation);
                ++fitnessCount;
            }
*/          
            // popBest.showPop();
            // Evaluation.showPopAndFitDiploid(popBest);
            
            System.out.println();
            
            Population selectedPop = Selection.select(popBeginning);

            Population popCrossedOver = Recombination.crossover(selectedPop);
            Population popMutated = Recombination.mutate(popCrossedOver, false);

            popBest = Elitism.elitist(popBeginning, popMutated);
            
            Evaluation.showPopAndFitDiploid(popBest);
            popBest.showPop();
            
            //  --- PARTIAL REPORT ---
            Report.report(popBest, generation);
            
            
            
            ++generation;
        }

        // --- FINAL REPORT ----
        Report.report(popBest, generation);

//        popBest.showPop();

//        Evaluation.showPopAndFitDiploid(popBest);
        
        System.exit(0);
    }
}