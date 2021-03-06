package solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;


import problem.Fridge;
import problem.Matrix;
import problem.ProblemSpec;


public class MySolver implements OrderingAgent {
	
	private ProblemSpec spec = new ProblemSpec();
	private Fridge fridge;
    private List<Matrix> probabilities;
    
    
    private List<List<Double>> failProb;		//probability of failures
    private List<Policy> policies;
    private List<List<Policy>>  goToPolicies;
	
	
    /**
     * Get a particular policy related to a given state
     * @param state
     * 		state to search policies
     * @return
     * 		policy relating to given state
     */
    public Policy getPolicy(List<Integer> state) {
		
    	for( Policy p : policies) {
    		if(p.state.equals(state)) return p;
    	}
    	return null;
    }
	
	/**
	 * Generate the failure probability matrix
	 * @param prob
	 * 		on probability matrix for item k
	 */
	private List<Double> generateFailProb_1Type(Matrix prob) {
		
		
		int size = fridge.getMaxItemsPerType() + 1;
		List<Double> pRow = new ArrayList<Double>(size);
		List<Double> fails = new ArrayList<Double>();
		for(int i = 0; i < prob.getNumRows() - 1; i ++ ) {

			//generate for one row
			pRow = prob.getRow(i);
			
			double val = 0;
			for(int j = 0; j < pRow.size() ; j ++) {
				val = pRow.get(j)*Math.max((j -i), 0) + val;
			}
			fails.add(val);		
			
//			System.out.println(pRow);		
			
		}
//		System.out.print( "fails: ");
//		System.out.println(fails);
		return fails;

	}
	
	/**
	 * Generate the fail probabilities 
	 */
	public void generateFailProb() {
		
		failProb = new ArrayList<List<Double>>();
		
		for(Matrix prob : probabilities) {
			
			failProb.add(generateFailProb_1Type(prob));
			
		}
	}

//////////////////////// VALUE ITERATION///////////////////////////////
	/**
	 * Calculate the expected values for each possible state
	 */
	public void calcValues() {
		
//		System.out.println("calcvals");
		
		for (Policy p : policies) {
			p.value = 0;
			for (int i  = 0; i < fridge.getMaxTypes(); i ++) {
//				System.out.println(failProb.get(i));
//				System.out.println(p.state.get(i));
				
				p.value += failProb.get(i).get(p.state.get(i));
			}
		}
		
	}
	
	/**
	 * populate all possible states for the given problem spec
	 */
	public void populateStates(){
		
		
		int c = fridge.getCapacity();
		int M = fridge.getMaxItemsPerType();
		
		//Iterate for all total items in the fridge
		List<Policy> pols1 = new ArrayList<Policy>();
		List<Policy> pols2 = new ArrayList<Policy>();
		
		//first item type
		for(int s1 = 0; s1 <= Math.min(c, M); s1 ++) {
			pols1.add(new Policy(s1));
		}
		
		for (int t = 1; t < fridge.getMaxTypes(); t++) {
			//Item Types
			for(Policy P : pols1) {
				for (int si = 0; si <= Math.min(M, c-P.totItems); si ++) {
					
					pols2.add(new Policy(si, P));
				}
			}
			pols1.clear();
			pols1.addAll(pols2);		//increase tree level
			pols2.clear();
		}
		
		//update all policies
		policies.addAll(pols1);	
		
		
		//Get all the expected values
		calcValues();
		
//		//display states -- debugging
//		for( Policy P: policies) {
//			System.out.print(P.state);
//			System.out.print(" ");
//			System.out.println(P.value);
//		}
	}
	
	
	private void sortStates() {
		
		//Initialise
		List<List<Policy>> sortPolicy = new ArrayList<List<Policy>>();
		
		for (int i = 0 ; i <= fridge.getCapacity(); i ++) {
			sortPolicy.add(new ArrayList<Policy>());
		}
		//sort into corresponding list containing same number of tot items
		for(Policy p: policies) {
			sortPolicy.get(p.totItems).add(p);
		}
		
		
		
		//best policy to action to 
		goToPolicies = new ArrayList<List<Policy>>();
		for(int i = 0; i < fridge.getCapacity(); i ++) {
//			//each number of items
			goToPolicies.add(sortPolicy.get(i));
			for (int j = i+1; j <= Math.min(i + fridge.getMaxPurchase(), fridge.getCapacity()); j ++) {
				goToPolicies.get(i).addAll(sortPolicy.get(j));
			}
		}
		
		//sort each list on value
		for(List<Policy> lis : goToPolicies) {
			Collections.sort(lis, new Comparator<Policy>() {
				public int compare(Policy p1, Policy p2) {
					if(p1.value > p2.value) return 1;
					if(p1.value < p2.value) return -1;
					return 0;
			}});
			
		}
		
//		//Debug -- display 
//		for(List<Policy> l : goToPolicies) {
//			System.out.println();
//			for(Policy p: l) {
//				System.out.print(p.state);
//				System.out.println(p.value);
//			}
//		}
		
	}
	
	
	/**
	 * Generates the best action to take for each state
	 */
	public void generateAction() {
		
		List<Integer> zeroAction = new ArrayList<Integer>(Collections.nCopies(fridge.getMaxTypes(), 0));
		
		for(Policy p : policies) {
			if(p.totItems >= fridge.getCapacity()) {
				//At max number of items must stay
				p.finalState = p.state;
				p.action = zeroAction;
				p.expectedFails = getPolicy(p.finalState).value;
			} else {
				
				p.action = new ArrayList<Integer>();
				Policy next;
				boolean good = true;
				int idx = 0;
				
				do {
					good = true;
					
					next = goToPolicies.get(p.totItems).get(idx);
					//Action
					
					for(int t = 0; t<fridge.getMaxTypes(); t ++) {
						int ai = next.state.get(t) - p.state.get(t);
						
						if(ai < 0) {
							//bad final state
							good= false;
							idx ++;
							p.action.clear();
							break;
						}
						p.action.add(ai);
					}
					
				}while(!good);

				//update values
				p.finalState = next.state;
				p.expectedFails = next.value;
			}
		}
	}
	

	
	
/////////////////////////// Monte Carlo ////////////////////////////////////////
	
	
	public List<Integer> monteCarlo(List<Integer> inventory) {
		System.out.println(inventory);
		List<Integer> maxOrder = new ArrayList<Integer>();
		
		int totItems = 0;
		
		for(Integer i : inventory){
			int val = Math.min(fridge.getMaxItemsPerType() - i, fridge.getMaxPurchase());
			maxOrder.add(val);
			
			totItems += i;
		}
//		System.out.println(maxOrder);

		
		TreeNode root = new TreeNode(failProb, maxOrder, fridge.getMaxTypes(), fridge.getMaxPurchase(), fridge.getCapacity(), totItems);
//		System.out.println(root);
		
		Timer time = new Timer();
		
		while(time.elapsed() < 10000) {
			root.selectAction();
		}
		
//		List<Integer> tmp = new ArrayList<Integer>();
//		tmp.add(0); tmp.add(0); tmp.add(0);
//		
//		return tmp;
//
		return root.bestPath();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public MySolver(ProblemSpec spec) throws IOException {
	    this.spec = spec;
		fridge = spec.getFridge();
        probabilities = spec.getProbabilities();
        
        policies = new ArrayList<Policy>();
	}
	
	public void doOfflineComputation() {
		generateFailProb();
		
		if (fridge.getCapacity() < 8) {
			// smaller fridges
			populateStates();
			sortStates();
			generateAction();
		}

//		
//		//for debug -- print policies
//		System.out.println();
//		for(Policy p : policies) {
//			System.out.print(p.state);
//			System.out.print(p.action);
//			System.out.print(p.finalState);
//			System.out.println(p.expectedFails);
//		}
		
		
	   
	}
	
	public List<Integer> generateShoppingList(List<Integer> inventory,
	        int numWeeksLeft) {

		
		if (fridge.getCapacity() < 8) {
			//tiny, small & medium
			return getPolicy(inventory).action;
		} else {
			return monteCarlo(inventory);
		}
		
		
		
		
	}
		
		

}
