package solver;

import java.io.IOException;
import java.util.List;

import problem.ProblemSpec;
import problem.Simulator;

public class Solver {
	
	/** The default file to read the user model from. */
	public static final String DEFAULT_INPUT = "testcases/small-v1.txt";
	/** The default file to output to. */
	public static final String DEFAULT_OUTPUT = "testcases/output.txt";
	/** The path for the input file. */
	private static String inputPath = null;
	/** The path for the output file. */
	private static String outputPath = null;

	public static void main(String[] args) throws IOException {
		parseCommandLine(args);
		ProblemSpec spec = new ProblemSpec();
		spec.loadInputFile(inputPath);
		Simulator simulator = new Simulator(inputPath);
		ShoppingGenerator generator = new ShoppingGenerator(inputPath);
		
		for (int i = 0; i < spec.getNumWeeks(); i++) {
			List<Integer> shopping = generator.generateShopping(
					simulator.getInventory());
			simulator.simulateStep(shopping);
		}
		
		simulator.saveOutput(outputPath);

	}
	
	/**
	 * Parses the command line arguments.
	 * 
	 * @param args
	 *            the array of command line arguments.
	 */
	public static void parseCommandLine(String args[]) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].trim();
			if (inputPath == null) {
				inputPath = arg;
			} else if (outputPath == null) {
				outputPath = arg;
			}
		}
		if (inputPath == null) {
			inputPath = DEFAULT_INPUT;
		}
		if (outputPath == null) {
			outputPath = DEFAULT_OUTPUT;
		}
	}

}
