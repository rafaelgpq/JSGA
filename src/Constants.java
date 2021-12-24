import java.util.ResourceBundle;

public final class Constants {

    // CONSTANTS
    public static ResourceBundle resourceBundle = ResourceBundle.getBundle("JSGA");
    public static final int L = Integer.parseInt(resourceBundle.getString("GenomeLength"));
    public static final int P = Integer.parseInt(resourceBundle.getString("PopulationSize"));
    public static final double C = Double.parseDouble(resourceBundle.getString("CrossoverRate"));
    public static final double M = Double.parseDouble(resourceBundle.getString("MutationRate"));
    public static final int seed = Integer.parseInt(resourceBundle.getString("Seed"));
    public static int maxGeneration = Integer.parseInt(resourceBundle.getString("MaxGeneration"));
    public static int windowSize = Integer.parseInt(resourceBundle.getString("WindowSize"));
    public static double gapSize = Double.parseDouble(resourceBundle.getString("GapSize"));
    public static int selectionStrategy = Integer.parseInt(resourceBundle.getString("SelectionStrategy"));

    public static final String classFunctionName = resourceBundle.getString("eval.function.class.name");
    public static final String functionName = resourceBundle.getString("eval.function.name");

    public static final double phenotypeM = Double.parseDouble(resourceBundle.getString("eval.function.normalizeChrm.m"));
    public static final double phenotypeB = Double.parseDouble(resourceBundle.getString("eval.function.normalizeChrm.b"));

    public static final int alleleSize = Integer.parseInt(resourceBundle.getString("eval.function.allele.size"));
}
