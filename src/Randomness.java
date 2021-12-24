public final class Randomness {

    private static int randomCount = 0;
    private static int[] randomX = new int[2];

    public static int rndInit(int seed) {
        randomX[0] = seed;
        return 0;
    }

    public static int rndInit() {
        randomX[0] = rndInit(Constants.seed);
        return 0;
    }

    public static double randomness() { //randomness() {
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

    public static int randomBetween(int a, int b) {
        double x = (b - a) * randomness() + a; // Turn It Into With The Randomness class.
        return (int) x;
    }

    public static void main(String[] args) {
        int rnd;
//        Randomness.rndInit(333);
//        for (int times = 0; times < 10000; times++) {
//            rnd = Randomness.randomBetween(0, 10);
//            System.out.println("Randomness: Random Number #" + times + ": " + rnd);
//            if (times % 100 == 0) System.out.println("\n\n\n");
//        }
//        int $1st_Time$ = (int) System.currentTimeMillis();
//        Randomness.rndInit($1st_Time$/333);
//        System.out.println("Time (msecs): " + $1st_Time$);
//        for (int t=0; t<$1st_Time$; t++) Randomness.randomBetween(1, 49);
//        for (int times=0; times<14; times++) {
//            System.out.println("x"+(times+1)+" = " + Randomness.randomBetween(1, 49));
//        }

        Randomness.rndInit(25);
        for (int $ttt = 0; $ttt < 7777777; $ttt++) {
            Randomness.randomBetween(1, 49);
//            System.out.println("x" + ($ttt%7 + 1) + " = " + Randomness.randomBetween(1, 49));
        }


        System.out.println("Number 1 is: = " + Randomness.randomBetween(1, 7));
        System.out.println("Number 2 is: = " + Randomness.randomBetween(8, 14));
        System.out.println("Number 3 is: = " + Randomness.randomBetween(15, 21));
        System.out.println("Number 4 is: = " + Randomness.randomBetween(22, 27));
        System.out.println("Number 5 is: = " + Randomness.randomBetween(28, 34));
        System.out.println("Number * is: = " + Randomness.randomBetween(35, 41));
        System.out.println("Number 7 is: = " + Randomness.randomBetween(42, 49));

//        int x0 = Randomness.randomBetween(1, 49);
//        int x0F = Randomness.randomBetween(1, 49);
//        int x1 = (x0 == x0F ) ? Randomness.randomBetween(1, 49) : x0F;
//        System.out.println("x0 = " + x0 + ", x0F = " + x0F + " and x1 = " + x1);
//
//        int x1F = Randomness.randomBetween(1, 49);
//        int x2 = (x1 == x1F) || (x1 == x0) ? Randomness.randomBetween(1, 49) : x1F;
//        System.out.println("x1F = " + x1F + " and x2 = " + x2);
//
//        int x2F = Randomness.randomBetween(1, 49);
//        int x3 = (x2 == x2F) || (x2 == x0) || (x2 == x1) ? Randomness.randomBetween(1, 49) : x2F;
//        System.out.println("x2F = " + x2F + " and x3 = " + x3);
//
//        int x3F = Randomness.randomBetween(1, 49);
//        int x4 = (x3 == x3F) || (x3 == x0) || (x3 == x1) || (x3 == x2) ? Randomness.randomBetween(1, 49) : x2F;
//        System.out.println("x3F = " + x3F + " and x4 = " + x4);


    }


}
