package solver;

import java.util.ArrayList;
import java.util.List;


public class Policy {
	
	public int idx;		//index in policies
	
	public List<Integer> state;
	public float value;				//reward for given state
	public int totItems;
	
	public List<Integer> action;	//shopping
	public List<Integer> finalState;
	public float expectedFails;	//expected number of fails after once in final state
	
	public Policy(int s1) {
		state = new ArrayList<Integer>();
		state.add(s1);
		totItems = s1;
	}
	
	/**
	 * Generate new Policy with one extra state than the given poly
	 * @param si
	 * 		new state
	 * @param poly
	 * 		previous policy
	 */
	public Policy(int si, Policy poly) {
		state = new ArrayList<Integer>(poly.state);
		state.add(si);
		totItems = poly.totItems + si;
		
	}
}
