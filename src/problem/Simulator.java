package problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Simulator {

	private int currentWeek = 1;
	private ProblemSpec problemSpec = new ProblemSpec();
	private ArrayList<Integer> inventory = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> inventoryHistory =
			new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> shoppingHistory =
			new ArrayList<ArrayList<Integer>>();
	private ArrayList<Double> penaltyHistory = new ArrayList<Double>();
	private double totalPenalty = 0;
	private double totalMinPenalty = 0;
	private Fridge fridge;
	private List<Matrix> probabilities;
	private boolean verbose = true;
	
	/**
	 * Constructor
	 * @param problemSpecFilename path to input file
	 * @throws IOException
	 */
	public Simulator(String problemSpecFilename) throws IOException {
		problemSpec.loadInputFile(problemSpecFilename);
		fridge = problemSpec.getFridge();
		probabilities = problemSpec.getProbabilities();
		for (int i = 0; i < fridge.getMaxTypes(); i++) {
			inventory.add(0);
		}
		
		if (verbose) {
			System.out.println("Problem spec loaded.");
			System.out.println("Fridge: " + fridge.getName());
			System.out.println("Discount factor: " + 
					problemSpec.getDiscountFactor());
			System.out.println("Cost per failure: " + problemSpec.getCost());
		}
	}
	
	/**
	 * Simulate a week. A runtime exception is thrown if the shopping list is 
	 * invalid e.g. if total items in the current inventory plus the total items
	 * in the shopping list exceeds the fridge capacity. If the shopping list is
	 * valid, the user consumption is sampled and the current week is advanced.
	 * @param shopping List of item quantities to buy.
	 */
	public void simulateStep(List<Integer> shopping) {
		if (verbose && currentWeek > problemSpec.getNumWeeks()) {
			System.out.println("Warning: problem spec num weeks exceeded.");
		}
		
		// Process shopping
		if (shopping.size() != fridge.getMaxTypes()) {
			throw new IllegalArgumentException("Invalid shopping list size");
		}
		ArrayList<Integer> tempState = new ArrayList<Integer>(inventory);
		for (int i = 0; i < shopping.size(); i++) {
			if (shopping.get(i) < 0) {
				throw new IllegalArgumentException("Negative shopping?");
			}
			tempState.set(i, tempState.get(i) + shopping.get(i));
		}
		int sum = 0;
		for (int i : tempState) {
			sum += i;
		}
		if (sum > fridge.getCapacity()) {
			throw new IllegalArgumentException("Fridge capacity exceeded");
		}
		shoppingHistory.add(new ArrayList<Integer>(shopping));
		
		// Sample next inventory
		inventoryHistory.add(inventory);
		inventory = tempState;
		List<Integer> wants = sampleUserWants(inventory);
		int numFailures = 0;
		for (int i = 0; i < wants.size(); i++) {
			int net = inventory.get(i) - wants.get(i);
			if (net < 0) {
				inventory.set(i, 0);
				numFailures -= net;
			} else {
				inventory.set(i, net);
			}
		}
		
		// Calculate penalty
		double penalty = problemSpec.getCost() * numFailures;
		totalPenalty += Math.pow(problemSpec.getDiscountFactor(),
				currentWeek - 1) * penalty;
		penaltyHistory.add(penalty);
		int wantsSum = 0;
		for (int i : wants) {
			wantsSum += i;
		}
		int minNumFailures = wantsSum - fridge.getCapacity();
		if (minNumFailures < 0) {
			minNumFailures = 0;
		}
		double minPenalty = minNumFailures * problemSpec.getCost();
		totalMinPenalty += Math.pow(problemSpec.getDiscountFactor(),
				currentWeek - 1) * minPenalty;
		
		if (verbose) {
			System.out.println();
			System.out.println("Week " + currentWeek);
			List<Integer> startInventory = inventoryHistory.get(
					inventoryHistory.size() - 1);
			System.out.println("Start inventory: " + startInventory);
			System.out.println("Shopping: " + shopping);
			System.out.println("User wants: " + wants);
			System.out.println("End inventory: " + inventory);
			System.out.println("Num failures: " + numFailures);
			System.out.println("Penalty this week: " + penalty);
			System.out.println("Total penalty: " + totalPenalty);
			System.out.println("Minimum penalty this week: " + minPenalty);
		
			if (currentWeek == problemSpec.getNumWeeks()) {
				System.out.println();
				System.out.println("Total minimum penalty: " + totalMinPenalty);
				double temp = totalPenalty / totalMinPenalty;
				System.out.println("Total penalty/total min penalty: " + temp);
			}
		}	
		currentWeek ++;	
	}
	
	/**
	 * Uses the currently loaded stochastic model to sample user wants.
	 * Note that user wants may exceed the inventory
	 * @param state The inventory
	 * @return User wants as list of item quantities
	 */
	public List<Integer> sampleUserWants(List<Integer> state) {
		List<Integer> wants = new ArrayList<Integer>();
		for (int k = 0; k < fridge.getMaxTypes(); k++) {
			int i = state.get(k);
			List<Double> prob = probabilities.get(k).getRow(i);
			wants.add(sampleIndex(prob));
		}
		return wants;
	}
	
	/**
	 * Returns an index sampled from a list of probabilities
	 * @precondition probabilities in prob sum to 1
	 * @param prob
	 * @return an int with value within [0, prob.size() - 1]
	 */
	public int sampleIndex(List<Double> prob) {
		double sum = 0;
		double r = Math.random();
		for (int i = 0; i < prob.size(); i++) {
			sum += prob.get(i);
			if (sum >= r) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Saves the current shopping history and total penalty to file
	 * @param filename The path to the text file to save to
	 * @throws IOException
	 */
	public void saveOutput(String filename) throws IOException {
		problemSpec.saveOutput(filename, getTotalPenalty(), shoppingHistory);
	}

	/**
	 * Set verbose to true for console output
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * Get penalty from history.
	 * @precondition week < currentWeek
	 * @param week The week to retrieve. Week starts at 1. 
	 * @return penalty
	 */
	public double getPenalty(int week) {
		return penaltyHistory.get(week - 1);
	}
	
	/**
	 * @return the total penalty so far
	 */
	public double getTotalPenalty() {
		return totalPenalty;
	}
	
	/** 
	 * @return the total minimum penalty so far
	 */
	public double getTotalMinPenalty() {
		return totalMinPenalty;
	}

	public int getCurrentWeek() {
		return currentWeek;
	}
	
	public List<Integer> getInventory() {
		return new ArrayList<Integer>(inventory);
	}
}
