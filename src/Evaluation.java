import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class Evaluation {

    /**
     * @param pop
     * @return
     */
    public static Double[] evaluate(Population pop) {
        return evaluator(pop);
    }
    
    /**
     * @param pop
     * @return
     */
    public static Double[] evaluator(Population pop) {
        return functionToBeEvaluated(pop);
    }

    /**
     * @param pop
     * @return
     */
    public static Double[] functionToBeEvaluated(Population pop) {
        Double[] outcome = null;
        try {
            Class classFunctionName = Class.forName(Constants.classFunctionName);
            classFunctionName.newInstance();
            Class[] params = new Class[]{Population.class};
            Method method = classFunctionName.getMethod(Constants.functionName, params);
            outcome = (Double[]) method.invoke(classFunctionName.newInstance(), new Object[]{pop});
        } catch (ClassNotFoundException cnfx) {
            cnfx.printStackTrace();
        } catch (InstantiationException ix) {
            ix.printStackTrace();
        } catch (NoSuchMethodException nsmx) {
            nsmx.printStackTrace();
        } catch (IllegalAccessException iax) {
            iax.printStackTrace();
        } catch (InvocationTargetException itx) {
            itx.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
        return outcome;
    }

    /**
     * @param fitness
     */
    public static void showFitness(Double[] fitness) {
        for (int being = 0; being < fitness.length; being++) {
            System.out.println("Evaluation: fitness[" + being + "] = " + fitness[being]);
        }
    }

    /**
     * @param fitness
     * @return
     */    
	public static double findTheBestFitness(Double[] fitness) {
		double theMaximum = fitness[0];
		for (int being=0; being<fitness.length; being++) {
			for (int other=being; other<fitness.length; other++) {
//				System.out.printf("{being} fitness[%d] = %.1f < fitness[%d] = %.1f\n", being, fitness[being], other, fitness[other]);
				if (fitness[being] >= fitness[other]) {
					if (fitness[being] >= theMaximum) {
						theMaximum = fitness[being];
					}					
				}
			}
		}
//		System.out.printf("theMaximum = %.1f\n", theMaximum);					
		return theMaximum;
	}


	/**
	 * 
	 * @param pop
	 * @return
	 */
	public static double findTheBestFitness(Population pop) {
        Double[] fitness = evaluate(pop);
        double bestFitness = findTheBestFitness(fitness);
        return bestFitness;
	}
    
    

    /**
     * @param fitness
     * @return
     */
    public static double findTheWorst(Double[] fitness) {
        double theValue = fitness[0];
        int index = 0;
        for (int being = 0; being < fitness.length; being++) {
            for (int other = being; other < fitness.length; other++) {
                if (fitness[being] <= fitness[other]) {
                    theValue = fitness[being] <= theValue ? fitness[being] : theValue;
                    index = fitness[being] <= theValue ? being : index;
                }
            }
        }
        return theValue;
    }

    /**
     * @param fitness
     * @return
     */
    public static double findFitnessAvg(Double[] fitness) {
        double outcome = 0d;
        for (double fit : fitness) {
        	outcome += fit;
//        	System.out.println("[findFitnessAvg()] fit: " + fit + " and total is: " + outcome);
        }
        
        return (fitness.length == 0) ? 0d : outcome / fitness.length;
    }

    /**
     * @param fitness
     * @return
     */
    public static double findFitnessSigma(Double[] fitness) {
        double avg = findFitnessAvg(fitness);
        double outcome = 0d;
        for (double fit : fitness) outcome += (fit - avg) * (fit - avg);
        outcome = fitness.length == 0 ? 1 : Math.sqrt(outcome / ((double) (fitness.length - 1)));
        outcome = outcome < 0d ? 0.0 : outcome;
        return outcome;
    }

    /**
     * @param pop
     * @return
     */
    public static int findTheBestGuy(Population pop) {
        Double[] fitness = evaluate(pop);
        int bestGuy = findTheBestGuy(fitness);
        return bestGuy;
    }
    
    /**
     * 
     * @param fitness
     * @return
     */
	public static int findTheBestGuy(Double[] fitness) { 
		double theMaximum = fitness[0];
		int index = 0;
		for (int being=0; being<fitness.length; being++) {			
			for (int other=being; other<fitness.length; other++) {
//				System.out.printf("{being} fitness[%d] = %.1f >= fitness[%d] = %.1f\n", being, fitness[being], other, fitness[other]);
				if (fitness[being] >= fitness[other]) {
					if (fitness[being] >= theMaximum) {
						theMaximum = fitness[being];
						index = being;
//						System.out.println("[INSIDE LOOP] index = " + index);
					}					
				}
			}
		}
//		System.out.printf("theMaximum = %.1f\n", theMaximum);					
//		System.out.printf("index = %d\n", index);
		return index;
	}

    
    
    
    
    /**
     * @param value
     * @return
     */
    public static double normalizeChromosome(double value) {
        return normalizeChromosome(value, Constants.phenotypeM, Constants.phenotypeB);
    }

    /**
     * @param value
     * @param m
     * @param b
     * @return
     */
    public static double normalizeChromosome(double value, double m, double b) {
        return m * value + b;
    }

    /**
     * @param aGenome
     * @return
     */
    public static double findTheFitnessHaploid(String aGenome) {
        double phenotype = 0d, outcome = 0d;
        for (int gene = 0; gene < aGenome.length(); gene++) {
            phenotype += Math.pow(2d, (Constants.L - gene - 1)) * Double.parseDouble(String.valueOf(aGenome.charAt(gene)));
        }
        phenotype = normalizeChromosome(phenotype);
        try {
            Class classFunctionName = Class.forName(Constants.classFunctionName);
            classFunctionName.newInstance();
            Class[] params = new Class[] { Double.TYPE };
            Method method = classFunctionName.getMethod(Constants.functionName, params);
            outcome = (Double) method.invoke(classFunctionName.newInstance(), phenotype);
        } catch (ClassNotFoundException cnfx) {
            cnfx.printStackTrace();
        } catch (InstantiationException ix) {
            ix.printStackTrace();
        } catch (NoSuchMethodException nsmx) {
            nsmx.printStackTrace();
        } catch (IllegalAccessException iax) {
            iax.printStackTrace();
        } catch (InvocationTargetException itx) {
            itx.printStackTrace();
        }
        return outcome;
    }

    /**
     * Shows the genotype (population) and the phenotype (its fitness) altogether.
     *
     * @param pop
     */
    public static void showPopAndFitHaploid(Population pop) {
        String chromosome = "";
        double fitness = 0d;
        if (pop.showPopSize() != Constants.P) {
            System.out.println("ERROR: Size is different to the P-parameter in the JSGA.properties file");
        } else {
            String[] strArray;
            int individual = 0;
            for (String[] aPopulation : pop.getPopulation()) {
                strArray = aPopulation;
                chromosome = strArray[individual];
                fitness = findTheFitnessHaploid(chromosome);
                System.out.println("Evaluation.showPopAndFit(): Genome #" + individual + ": |" + strArray[individual] + "| and fitness: " + fitness);
                ++individual;
            }
        }
    }

    /**
     * @param aGenome
     * @return
     */
    public static double findTheFitnessDiploid(String aGenome) {
    	double phenotype = 0d, outcome = 0d;
        int step = 0, myPos = 0;
        final int ALLELE_SIZE = Constants.alleleSize;
        final int ALLELE_NO = Constants.L / ALLELE_SIZE;
        double[] x = new double[ALLELE_NO];        
        // System.out.println("ALLELE_SIZE: " + ALLELE_SIZE + " and ALLELE_NO: " + ALLELE_NO);
        double fitness = 0d;
        for (step = 0; step < Constants.L; step += ALLELE_SIZE) {
            phenotype = 0d;
            for (int gene = 0; gene < aGenome.length(); gene++) {
                phenotype += Math.pow(2d, (aGenome.length() - gene - 1)) * Double.parseDouble(String.valueOf(aGenome.charAt(gene)));
                // System.out.println("phenotype: " + phenotype);
            }
            myPos = step / ALLELE_SIZE;
            x[myPos] = Evaluation.normalizeChromosome(phenotype);
        }
        fitness = AutoTestingEvaluation.F_Instrumented_Tracey(x);        
        return fitness;
    }

    /**
     * Shows the genotype (population) and the phenotype (its fitness) altogether.
     *
     * @param pop
     */
    public static void showPopAndFitDiploid(Population pop) {
        String chromosome = "";
        double fitness = 0d;
        if (pop.showPopSize() != Constants.P) {
            System.out.println("ERROR: Size is different to the P-parameter in the JSGA.properties file");
        } else {
            String[] strArray;
            int individual = 0;
            for (String[] aPopulation : pop.getPopulation()) {
                strArray = aPopulation;
                chromosome = strArray[individual];
                fitness = findTheFitnessDiploid(chromosome);
                System.out.println("Evaluation.showPopAndFit(): Genome #" + individual 
                		+ ": |" + strArray[individual] + "| and fitness: " + fitness);
                ++individual;
            }
        }
    }
    

}


