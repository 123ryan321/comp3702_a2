package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problem.Fridge;
import problem.Matrix;
import problem.ProblemSpec;
import solver.Policies.Policy;




public class ShoppingGenerator {
	
	private ProblemSpec spec = new ProblemSpec();
	private Fridge fridge;
	private List<Matrix> probabilities;
	
	
	private class Policy {
		public List<Integer> state;
		public List<Integer> action;	//shopping
		public float value;	//expected reward
	}

	private List<Policy> policies;
	
	/**
	 * populate all possible states for the given problem spec
	 */
	public void populateStates(){
		
	}
	
	/**
	 * Iterate through all possible actions to find the action with the best reward for
	 * for the given state
	 *  
	 * @return
	 */
	public float valueIterate(List<Integer> state) {
		return 0;
	}
	
	
	
	
	public ShoppingGenerator(String problemSpecFilename) throws IOException {
		spec.loadInputFile(problemSpecFilename);
		fridge = spec.getFridge();
		probabilities = spec.getProbabilities();
	}
	
	
	public List<Integer> generateShopping(List<Integer> inventory) {
		
		// Example code that buys one of each item type.
		// Replace this with your code.
		List<Integer> shopping = new ArrayList<Integer>();
		int totalItems = 0;
		for (int i : inventory) {
			totalItems += i;
		}
		int totalShopping = 0;
		for (int i = 0; i < fridge.getMaxTypes(); i++) {
			if (totalItems >= fridge.getCapacity()) {
				shopping.add(0);
			} else {
				shopping.add(1);
				totalItems ++;
			}
		}
		return shopping;
	}

}
