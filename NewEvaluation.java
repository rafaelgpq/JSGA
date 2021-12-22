import java.util.ArrayList;

public class NewEvaluation {

    /**
     * f0(x) = 7 - (x + 3) (x - 2)
     * <p/>
     * Parameters: L=3, P=5, C=0.97, M=0.0377, Seed=33355, G=20
     *
     * @param pop
     * @return
     */
    public static Double[] function0(Population pop) {
        int k = 0;
        double x = 0d;
        Double[] phenotype = new Double[Constants.P];
        Double[] fitness = new Double[Constants.P];
        for (int person = 0; person < fitness.length; person++) {
            fitness[person] = 0.00d;
            phenotype[person] = 0.00d;
        }
        String individual;
        ArrayList<String[]> popAL = pop.getPopulation();
        for (String[] aPopAL : popAL) {
            individual = aPopAL[k];
            for (int gene = 0; gene < individual.length(); gene++) {
                phenotype[k] += Math.pow(2d, (Constants.L - gene - 1)) * Double.parseDouble(String.valueOf(individual.charAt(gene)));
            }
            x = Evaluation.normalizeChromosome(phenotype[k]);
            fitness[k] = function0(x); // f0(x) = 1000 - { 7 (x - 3) (x - 5) }
            ++k;
        }
        return fitness;
    }

    public static double function0(double x) {
        return (7 - (x + 3d) * (x - 2d));
    }
    
    public static Double[] function1(Population pop) {
        double phenotype;
        int k = 0, step = 0, myPos = 0;
        final int ALLELE_SIZE = Constants.alleleSize;
        final int ALLELE_NO = Constants.L / ALLELE_SIZE;
        double[] x = new double[ALLELE_NO];
        Double[] fitness = new Double[Constants.P];
        for (int person = 0; person < fitness.length; person++) {
            fitness[person] = 0.00d;
        }
        String individualAllele;
        ArrayList<String[]> popAL = pop.getPopulation();
        for (String[] aPopAL : popAL) {
            for (step = 0; step < Constants.L; step += ALLELE_SIZE) {
                individualAllele = aPopAL[k].substring(step, step + ALLELE_SIZE);
                phenotype = 0d;
                for (int gene = 0; gene < individualAllele.length(); gene++) {
                    phenotype += Math.pow(2d, (individualAllele.length() - gene - 1)) * Double.parseDouble(String.valueOf(individualAllele.charAt(gene)));
                }
                myPos = step / ALLELE_SIZE;
                x[myPos] = Evaluation.normalizeChromosome(phenotype);

            }
            fitness[k] = f1(x);
            ++k;
        }

        for (int count = 0; count < x.length; count++) {
            System.out.println("Evaluation.function1() [Solution]: x" + count + " = " + x[count]);
        }
        return fitness;
    }

    /**
     * interval = [ 30-(-30) ] / .1 = 600 values
     * Li = log2(600) = ln(600) / ln(2) = 9.23 ~= 10
     * Lt = N * Li = 30 * 10 = 300 bits.
     *
     * @param x
     * @return
     */
    public static double f1(double... x) { // x-double array got a size of 30 elements where each is a 10-gene-length allele.
        double outcome = 0d;
        final int ALLELE_SIZE = Constants.alleleSize;
        final int ALLELE_NO = Constants.L / ALLELE_SIZE;
        double c1 = 20d, c2 = .2d, c3 = 2d * Math.PI;
        double total_sqr = 0d;
        double total_cos = 0d;
        for (int count = 0; count < x.length; count++) {
            total_sqr += x[count] * x[count];
            total_cos += Math.cos(c3 * x[count]);
        }
        total_sqr /= (double) (ALLELE_NO);
        total_cos /= (double) (ALLELE_NO);
        outcome = (-c1) * Math.exp((-c2) * Math.sqrt(total_sqr)) - Math.exp(total_cos) + c1 + Math.E;
        return outcome;
    }

    /**
     * interval = [ 600-(-600) ] / .1 = 12000 values
     * Li = log2(12000) = ln(12000) / ln(2) = 13.5 ~= 14
     * Lt = N * Li = 10 * 14 = 140 bits.
     *
     * @param x
     * @return
     */
    public static double f2(double... x) {
        double outcome = 0d;
        double total_sqr = 0d;
        double total_cos = 1d;
        for (int count = 0; count < x.length; count++) {
            total_sqr += x[count] * x[count];
            total_cos *= Math.cos(x[count] / Math.sqrt(count + 1));
        }
        total_sqr /= 4000d;
        outcome = 1d + total_sqr - total_cos;
        return outcome;
    }

    /**
     * interval = [ 500-(-500) ] / .1 = 1000 values
     * Li = log2(1000) = ln(1000) / ln(2) = 9.97 ~= 10
     * Lt = N * Li = 10 * 10 = 100 bits.
     *
     * @param x
     * @return
     */
    public static double f3(double... x) {
        double outcome = 0d;
        double total = 0d;
        for (int count = 0; count < x.length; count++) {
            total += x[count] * Math.sin(Math.sqrt(Math.abs(x[count])));
        }
        outcome = 4189.829 + total;
        return outcome;
    }

    /**
     * interval = [ 5.12-(-5.12) ] / .01 = 1024 values
     * Li = log2(1024.0) = ln(1024.0) / ln(2) = 10
     * Lt = N * Li = 20 * 10 = 200 bits.
     *
     * @param x
     * @return
     */
    public static double f4(double... x) {
        double outcome = 0d;
        double total = 0d;
        for (int count = 0; count < x.length; count++) {
            total += (x[count] * x[count]) - (3d * Math.cos(2d * Math.PI * x[count]));
        }
        outcome = 3d * 20d + total;
        return outcome;
    }


    /**
     * interval = [ 2.048-(-2.048) ] / .001 = 4096 values
     * Li = log2(4096) = ln(4096) / ln(2) = 12
     * Lt = N * Li = 2 * 12 = 24 bits.
     *
     * @param x1
     * @param x2
     * @return
     */
    public static double f5(double x1, double x2) {
        return (100.0d * (x1 * x1 - x2 * x2) * (x1 * x1 - x2 * x2) + (1.0d - x1 * x1) * (1.0d - x1 * x1));
    }

}
