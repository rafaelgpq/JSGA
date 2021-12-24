
public class Testing {
	public static void main(String[] args) {
//		double[] fitness = new double[] { 10.4, 0.9, 1.2, 3.4, 5.7, .0, 0.1, 3.3, 20.3 };
		double[] fitness = new double[] { 1.1, 3.4, .1, .0, 2., 3.5, 8.2, 9.3, .0 };		
		for (int i=0; i<fitness.length; i++) { 
			fitness[i] *= -fitness[i];
//			fitness[i] += 24d;
			System.out.println("fitness #" + i + ": " + fitness[i]);
		}
//		for (int i=0; i<fitness.length; i++) { 
//			fitness[i] = Math.abs(fitness[i]);
//			System.out.println("fitness #" + i + ": " + fitness[i]);
//		}

		double theMinimum = 0d;
		double theMaximum = 0d;
		int index = 0;
		Testing tstng = new Testing();
		theMaximum = tstng.findTheBestFitness(fitness);
		index = tstng.findTheBestGuy(fitness);
		System.exit(0);
	}

	public int findTheBestGuy(double[] fitness) { 
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
		System.out.printf("[INDEX] theMaximum = %.1f\n", theMaximum);					
		System.out.printf("[INDEX] index = %d\n", index);
		return index;
	}
	
	// { 10.4, 0.9, 1.2, 3.4, 5.7, .0, 0.1, 3.3, 20.3 };
	public double findTheBestFitness(double[] fitness) {
		double theMaximum = fitness[0];
		for (int being=0; being<fitness.length; being++) {
			for (int other=being; other<fitness.length; other++) {
				System.out.printf("{being} fitness[%d] = %.1f >= fitness[%d] = %.1f\n", being, fitness[being], other, fitness[other]);
				if (fitness[being] >= fitness[other]) {
					
					if (fitness[being] >= theMaximum) {
						theMaximum = fitness[being];
						System.out.println("The Best is: " + theMaximum);
					}					
				}
			}
		}
		System.out.printf("theMaximum = %.1f\n", theMaximum);					
		return theMaximum;
	}
		

}
