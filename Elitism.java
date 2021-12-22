
// Statistics betterOffs = Statistics.mesure(pop, popNewIII);
public final class Elitism {

    /**
     * @param oldPop
     * @param newPop
     * @return
     */
    public static Population elitist(Population oldPop, Population newPop) {

        Population outcomePop;
        double theOldBest = Evaluation.findTheBestFitness(Evaluation.evaluate(oldPop));
        double theNewBest = Evaluation.findTheBestFitness(Evaluation.evaluate(newPop));

        if (theOldBest > theNewBest) {
            outcomePop = oldPop;
        } else {
            outcomePop = newPop;
        }
        return outcomePop;
    }
}
