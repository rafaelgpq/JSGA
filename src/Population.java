import java.io.Serializable;
import java.util.ArrayList;

public final class Population implements Serializable {

    private String[] genome;    // P-genomes of L-length
    private int[] randomX = new int[2];
    private int randomCount = 0;
    private ArrayList<String[]> population;

    /**
     * @return
     */
    public String[] getGenome() {
        // for (int kk=0; kk<genome.length; kk++) {
        // 	System.out.println("Population: getGenome(): genome[" + kk + "] = " + genome[kk]);
        // }
        return genome;
    }

    /**
     * @param genome
     */
    public void setGenome(String[] genome) {
        this.genome = genome;
    }

    /**
     * @return
     */
    public int[] getRandomX() {
        return randomX;
    }

    public void setRandomX(int[] randomX) {
        this.randomX = randomX;
    }

    /**
     * @return
     */
    public int getRandomCount() {
        return randomCount;
    }

    /**
     * @param randomCount
     */
    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }

    /**
     * @param mySeed
     */
    public Population(int mySeed) {
        int L = Constants.L;
        int P = Constants.P;
        randomX[0] = mySeed;
        population = new ArrayList<String[]>(P);

        genome = new String[P];

        for (int being = 0; being < P; being++) {
            genome[being] = "";
            for (int gene = 0; gene < L; gene++) {
                if (randomness() > 0.5) {// Turn It Into With The Randomness class.
                    this.genome[being] += "1";
                } else {
                    this.genome[being] += "0";
                }
            } // End of for --- gene
//            System.out.println("Population(MySeed) [Constructor]:  @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ individual: |" + genome[being] + "|");
            population.add(genome);
        } // End of for --- being
    }

    public Population() {
        this(Constants.seed);
    }

    // See http://www.canadiancontent.net/en/jd/go?Url=http://sources.redhat.com/gsl/ref/gsl-ref_17.html
    // gsl_rng_rand (Random Generator).
    /**
     * PseudoRandom Number Generator seen on:
     * http://www.canadiancontent.net/en/jd/go?Url=http://sources.redhat.com/gsl/ref/gsl-ref_17.html
     *
     * @return a pseudo-Random Number
     */
    private double randomness() { //randomness() {
        double randomXDouble = 0.0;
        int a = 1103515245, b = 12345;
        int m = 0;
        m = (int) Math.pow(2.0, 31.0);

        // traceThis("randomCount = "+ randomCount);
        if (randomCount >= randomX.length - 1) {
            randomCount = randomCount % (randomX.length - 1);
            randomX[randomCount] = randomX[randomX.length - 1];
        }
        randomX[randomCount + 1] = (a * randomX[randomCount] + b) % m;
        randomCount++;

        randomXDouble = (double) randomX[randomCount];
        return Math.abs(randomXDouble / Math.pow(2, 31));
    }

    /**
     * @return
     */
    public ArrayList<String[]> getPopulation() {
        return population;
    }

    /**
     * @param population
     */
    public void setPopulation(ArrayList<String[]> population) {
        this.population = population;
    }

    public void showPop() {
        String[] strArray;
        int k = 0;
        for (String[] aPopulation : population) {
            strArray = aPopulation;
            System.out.println("Population: Individual #" + k + ": is |" + strArray[k] + "| with its size of " + strArray[k].length() + " genes.");
            ++k;
        }
    }

// CHECK OUT IF COUPLING MUST BE REMOVED BY CREATING A SUPERCLASS COMMON TO BOTH OF THE METHODS...!!!!
//    public double findTheFitness(String aGenome) {
//        return Evaluation.findTheFitness(aGenome);
//    }


    /**
     * @return
     */
    public int showPopSize() {
        return population.size();
    }

    /**
     * @param selected
     * @return
     */
    public Population selectedPopulation(int[] selected) {
        String[] selectedStr = new String[Constants.P];
        ArrayList<String[]> popAL = new ArrayList<String[]>();
        for (int select = 0; select < selected.length; select++) {
            selectedStr[select] = getGenome()[selected[select]];
//            System.out.println("[RGP-1] Population: selectedPopulation(): selectedStr["
//                    + select + "] = |" + selectedStr[select] + "|");
        }
        for (String myGenome : selectedStr) {
            popAL.add(selectedStr);
        }
        setPopulation(popAL);
        return this;
    }

    /**
     * @param aGenome
     * @return
     */
    public Population replacePopulation(String[] aGenome) {
        ArrayList<String[]> popAL = new ArrayList<String[]>();
        for (String myGenome : aGenome) {
            popAL.add(aGenome);
        }
        setPopulation(popAL);
        return this;
    }

    /**
     * @param previousGenome
     * @param genome1
     * @param pos1
     * @param genome2
     * @param pos2
     * @return
     */
    public Population addTwoGenomes(String[] previousGenome, String genome1, int pos1, String genome2, int pos2) {
        int affectedChromosome = 0;
        String[] children = previousGenome;
//        for (int i=0; i<previousGenome.length; i++) {
//            System.out.println("Population: addTwoGenomes(): previousGenome[" + i + "] = |" + previousGenome[i] + "|");
//        }
//        for (int i=0; i<children.length; i++) {
//            System.out.println("Population: addTwoGenomes(): children[" + i + "] = |" + children[i] + "|");
//        }
        ArrayList<String[]> popAL = new ArrayList<String[]>();
        for (String myGenome : children) {
            if (affectedChromosome == pos1) {
                previousGenome[affectedChromosome] = genome1;
//                System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW HIT IT (1)");
            } else if (affectedChromosome == pos2) {
                previousGenome[affectedChromosome] = genome2;
//                System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW HIT IT (2)");
            } else {
                previousGenome[affectedChromosome] = children[affectedChromosome];
//                System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW ELSEWHERE...!!!!");
            }
            ++affectedChromosome;
        }
//        for (int i=0; i<previousGenome.length; i++) {
//            System.out.println("Population: addTwoGenomes() [AFTER] : previousGenome[" + i + "] = |" + previousGenome[i] + "|");
//        }
        return replacePopulation(previousGenome);
    }

}
