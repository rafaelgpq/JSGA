import java.util.ArrayList;

public class Testing2 {

	private static final double DEPENDANT = 5d;
	private static final double DBZ_BEARANCE = .125;
	private static final double Z_BEARANCE = .125;
	private static final double LOWER_X = -50d;
	private static final double UPPER_X = 50d;
	private static final double LOWER_Y = -50d;
	private static final double UPPER_Y = 50d;
	private static final double K = (-LOWER_X - LOWER_Y + UPPER_X + UPPER_Y) / 2d;

	public static void main(String[] args) {
		Testing2 tstII = new Testing2();
		Population pop = new Population();
		pop.showPop();
		Evaluation.showPopAndFitDiploid(pop);
		
		Double[] fitness = tstII.F_Instrumented_Tracey(pop);
		int i=0;
		for (double fit: fitness) {
			System.out.println("fitness #" + i + ": " + fit);
			++i;
		}
		System.exit(0);
		
	}

	public Double[] F_Instrumented_Tracey(Population pop) {
		double phenotype;
		int k = 0, step = 0, myPos = 0;
		final int ALLELE_SIZE = Constants.alleleSize;
		final int ALLELE_NO = Constants.L / ALLELE_SIZE;
		double[] x = new double[ALLELE_NO];
		Double[] fitness = new Double[Constants.P];
		for (int person = 0; person < fitness.length; person++) {
			fitness[person] = 0.00d;
		}
		String individualAllele = "";
		ArrayList<String[]> popAL = pop.getPopulation();
		for (String[] aPopAL : popAL) {
			for (step = 0; step < Constants.L; step += ALLELE_SIZE) {
				individualAllele = aPopAL[k]
						.substring(step, step + ALLELE_SIZE);
				phenotype = 0d;
				for (int gene = 0; gene < individualAllele.length(); gene++) {
					phenotype += Math.pow(2d,
							(individualAllele.length() - gene - 1))
							* Double.parseDouble(String
									.valueOf(individualAllele.charAt(gene)));
				}
				myPos = step / ALLELE_SIZE;
				x[myPos] = Evaluation.normalizeChromosome(phenotype);
			}
			fitness[k] = F_Instrumented_Tracey(x);
			System.out.println("[F_Instrumented_Tracey()] Individual #" + k
					+ ": " + aPopAL[k] + " and fitness{" + k + "] = "
					+ fitness[k]);
			++k;
		}

		// for (int count = 0; count < x.length; count++) {
		// System.out.println("F_Instrumented_Tracey() [Solution]: x" + count +
		// " = " + x[count]);
		// }

		return fitness;
	}
	
	// x[0] = 20.87 and x[1] = 29.53
	public double F_Instrumented_Tracey(double[] x) {
		double xx = x[0];
		double yy = x[1];
		double fitness = 0d;
		double branch_distance = 0d;
		double exception = 0d;
		double executed = 1d;
		double z;
		double dvz = 0d;
		// System.out.print("Nodes: START ==>Ê*1 ==> *2 ==> ");
		if (xx < 0d) {
			branch_distance += xx + K;
			executed += 0d;
			// System.out.print("3 ==> ");
			// return -1;
		} else {
			branch_distance += 0d;
			executed += 1d;
		}
		dvz = xx * xx + yy * yy - 2 * xx * yy + xx + yy - 6d;
		z = (xx + 5d * yy) / dvz;
		executed += 1d;
		if ((dvz >= -DBZ_BEARANCE / 2d) && (dvz <= DBZ_BEARANCE / 2d)) {
			exception += K;
		} else {
			exception += 0d;
		}
		// System.out.printf("*4 [z = %.4f] ==> ", z);
		if ((z > 1d) && (z <= 5d)) {
			branch_distance += 0d;
			executed += 1d;
			// System.out.print("*5 ==> ");
			if ((z >= 2d - Z_BEARANCE / 2d) && (z <= 2d + Z_BEARANCE / 2d)) { // Division-by-Zero
																				// Node.
				branch_distance += 0d;
				executed += 1d;
				// System.out.println("@@@@@@@@@@@@@@ TARGET REACHED @@@@@@@@@@@@@");
				// System.out.print("*6 ==> ");
			} else {
				branch_distance += Math.abs(z - 2) + K;
				executed += 0d;
			}
			// return (int) ((z - 3)/(z*z -4d*z + 4));
		} else {
			branch_distance += (1d - z) + (z - 5d) + 2d * K;
			executed += 0d;
			// System.out.print("7 ==> 8 ==> ");
			// return (int) (((double) (xx * 4))/((z - 1d)*(z - 5d)));
		}
		// System.out.println("END.");
		fitness = executed * branch_distance / DEPENDANT + exception;
		fitness *= -1d;
		fitness += 10000d;

		 if (executed == 5d)
		 System.out.printf("fitness: %.4f, exec: %.0f, b_dist: %.2f & excep: %.0f [x = %.2f, y = %.2f, z = %.2f].\n",
		 + fitness, executed, branch_distance, exception, xx, yy, z);
		return fitness;
	}

}
