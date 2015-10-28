package solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TreeNode {
	
    static Random r = new Random();
    static int nActions = 5;
    static double epsilon = 1e-6;
    
    boolean terminates;	//if the node goes to the bottom of the tree;
    
    int numTypes;	///number of item types
    
    int items;	//number to purchase at this node
    int totPurchased;	//total number of items purchased including this node
    int level;	//level in the tree ie item type
    
    
    List<Integer> maxOrderPerType;
    int maxOrder; 	//maxOrder quantity for this node
    int totMaxOrder;	//total order quantity for complete state
    private List<List<Double>> failProb;
    
    List<TreeNode> children;
    //actuall values
    double exptFail;	//expected fails to this point
    
    //min value to end of path
    double minValue;
    
    //simulation values
    double nVisits, totValue;		//avgVal = totValue/nVisits
    
    //initial values of starting state
    int capacity; //fridge capacity
    int initNumItems;	//number of items in initial state
    
    
    public TreeNode(List<List<Double>> fProb, List<Integer> maxOrd, int nTypes, int totMaxOrd, int cap, int numItems) {
    	//initialise the root
    	failProb = fProb;
    	maxOrderPerType = maxOrd;
    	totMaxOrder = totMaxOrd;
    	maxOrder = totMaxOrder;		//root node
    	numTypes = nTypes;
    	level = 0;
    	totValue = 0;
    	nVisits = 0;
    	exptFail = 0;
    	minValue = Double.MAX_VALUE;
    	capacity = cap;
    	initNumItems = numItems;
    	
    	children = new ArrayList<TreeNode>();
    	
    }
    
    /**
     * 
     * @param parent
     * 		the node parent
     * @param buy
     * 		items to buy
     */
    public TreeNode(TreeNode parent, int buy) {
    	level = parent.level + 1;
    	totMaxOrder = parent.totMaxOrder;
    	totPurchased = parent.totPurchased + buy;
    	items = buy;
    	
    	//simulation values
    	nVisits = 0;
    	totValue = 0;
    	
    	failProb = parent.failProb;
    	maxOrderPerType = parent.maxOrderPerType;
    	numTypes = parent.numTypes;
    	
    	maxOrder = Math.min(totMaxOrder - totPurchased, maxOrderPerType.get(level-1));
//    	System.out.print("level: ");
//    	System.out.print(level);
//    	System.out.print("  max order: ");
//    	System.out.println(maxOrder);
    	
    	exptFail = parent.exptFail + failProb.get(level-1).get(items);
    	
    	children = new ArrayList<TreeNode>();
    	
    	minValue = Double.MAX_VALUE;
    	
    	capacity = parent.capacity;
    	initNumItems = parent.initNumItems;
    	
    }
    
    public void selectAction() {
    	TreeNode curr = this;
    	
//    	System.out.println();
    	
    	List<TreeNode> visited = new ArrayList<TreeNode>();
    	visited.add(curr);
    	
    	TreeNode nxt;
    	while(!curr.isLeaf()) {
    		
    		//either traverse to another node
    		//or breaks to expand to a new Node
    		nxt = curr.traverse();
    		if(nxt == null) break;		//goinq to create a new node
    		
    		curr = nxt;	// only if not null
    		visited.add(curr);
    		
//    		System.out.print(curr.items);
//    		System.out.print(" ");
//    		System.out.println(curr.maxOrder);
    	}
    	
    	
    	double result;
    	
    	//expand to a new Node
    	if(curr.level != numTypes) {
        	nxt = curr.expand();
        	visited.add(nxt);

//        	System.out.print(nxt.items);
//        	System.out.print(" ");
//    		System.out.println(nxt.maxOrder);
        	
        	curr.children.add(nxt);
        	
        	curr = nxt;
        	//simulate to the end
//        	result = curr.simulate();
    	}

    	
    	result = curr.simulate();

    	//update statistics of all visted nodes
    	if(curr.level == numTypes) {
    		//also update every nodes min value
        	for(TreeNode v : visited) {
        		v.nVisits ++;
        		v.totValue += result;
        		curr.minValue = curr.exptFail;
        		v.minValue = Math.min(v.minValue, curr.exptFail);
        		v.terminates = true;
        	}
        	
    	} else {
        	for(TreeNode v : visited) {
        		v.nVisits ++;
        		v.totValue += result;
        	}
    	}

//    	System.out.print("  ");
//    	System.out.print("  ");
//    	System.out.println(curr.minValue);
    	
    }
    
    
    /** Biased sample to traverse to either a child or a new node
     * 
     * @return
     * 		chosen child or null if wish to get a new node
     */
    private TreeNode traverse() {
    	
    	//if all possible children exist have to choose a child
    	//otherwise can either choose a child or return null indicating were creating a new node
    	
    	int maxNumChild = Math.min(capacity - (initNumItems + totPurchased), Math.min(totMaxOrder - totPurchased, maxOrderPerType.get(level))) + 1;		//+1 to include 0
    	
    	//biased sampling
    	double avgVal = totValue/nVisits;
    	int numChild = children.size();
//    	avgVal += avgVal/numChild * (maxNumChild - numChild);	//add extra for unvisited nodes
    	
//    	double rand = r.nextDouble();
    	double dist = 0;	//distribution
    	
    	TreeNode next = null;
    	
    	double bestVal = Double.MAX_VALUE;
    	int numUnvisit = maxNumChild - numChild;
    	if( numUnvisit > 0) {
//    		bestVal = epsilon*(1 - numUnvisit/maxNumChild)*Math.pow(nVisits*0.02, 0.5);
    		bestVal = (0.5 *numChild/maxNumChild)*totValue/Math.sqrt(nVisits);
    	}

    	
    	for(TreeNode c : children ) {
//    		dist += (c.totValue/c.nVisits) / avgVal;		//percentage avergae value of child compared to total average (distibution)
    		
//    		if(dist > rand) {
//    			next = c;
//    			break;
//    		}
    		
    		double chk = c.totValue/(Math.sqrt(c.nVisits + epsilon)) +r.nextFloat()*epsilon;
    		
//    		System.out.print("  ");
//    		System.out.print(chk);
//    		System.out.print(" ");
//    		System.out.println(bestVal);
    		
    		
    		if(chk < bestVal) {
    			next = c;
    			bestVal = chk;
    		}
    			
    	}
    	return next;	
    }
    
    /**
     * Choose a new node to expand too and add to the tree
     * 
     * @return newly added node
     */
    private TreeNode expand() {
    	
    	
    	int maxItems = Math.min(capacity - (initNumItems + totPurchased), Math.min(totMaxOrder - totPurchased, maxOrderPerType.get(level)));	//max order for next node
    	
    	List<Integer> possibleItems = new ArrayList<Integer>();
    	
    	//populate all possible items
    	for(int i = 0; i <= maxItems; i ++) {
    		possibleItems.add(i);
    	}
    	
    	//remove items 
    	List<Integer> rmv = new ArrayList<Integer>();
    	for(TreeNode c: children) {
    		rmv.add(c.items);
    	}
    	//reverse order and remove
    	Collections.sort(rmv, Collections.reverseOrder());
    	for(int r : rmv) {
    		possibleItems.remove(r);
    	}
    	
    	//randomly select an item number thats left
//    	System.out.print("MaxItmes ");
//    	System.out.println(maxItems);
//    	System.out.print("Possible size: ");
//    	System.out.println(possibleItems.size());
//    	System.out.println(rmv);
//    	System.out.println(possibleItems);
    	int buy = possibleItems.get(r.nextInt(possibleItems.size()));
    	
    	TreeNode tmp = new TreeNode(this, buy);
    	
//    	System.out.print(tmp.items);
//    	System.out.print(" ");
//    	System.out.println(maxItems);
    	return tmp;
    }
    
    /**
     * Simulate from current tree level to the bottom of the tree (level = numTypes)
     * and calculates the expected number of failures
     * 
     * @return expected probability of failures
     */
    private double simulate() { 	
    	
    	double result = exptFail;
    	
    	int qty = maxOrder;
    	
    	
    	int idx = 0;
    	
    	//choose a path from the next level to the end
    	for (int i = level + 1; i <= numTypes; i ++) {
    		
    		
    		double min = Double.MAX_VALUE;
    		
    		List<Double> fail = failProb.get(level-1);
    		//find minimum fail prob from 0 to max qty
    		for(int j = items; j <= Math.min(qty+items, fail.size()); j ++) {
    			if(fail.get(j) < min) {
    				idx = j;
    			}
    	
    		}
    		//oder idx of item i
    		result += fail.get(idx);
    		qty -= idx;
    		
		}
    	
    	
    	return result;
    }
    
    /**
     * checks if there are any children of the current node
     * @return true if there are no children
     */
    private boolean isLeaf() {
    	return children.size() == 0;
    }
    
    
    /**
     * Find the best path to the final node
     * use this function from the root only
     * @return
     * 		list of path
     */
    public List<Integer> bestPath() {
    	List<Integer> path = new ArrayList<Integer>();
    	
    	TreeNode curr = this;	//root
    	
    	while(!curr.isLeaf()) {
    		//loop to end of tree
    		
//    		System.out.println();
    		
    		//find best child option for each node
        	double bstVal = Double.MAX_VALUE;
        	double chkVal;
        	TreeNode bestChild = null;
        	for(TreeNode c: curr.children) {
//        		System.out.print(c.minValue);
//        		System.out.print(" ");
//        		System.out.println(bstVal);
        		
        		if(c.minValue + r.nextDouble()*epsilon < bstVal) {
        			bstVal = c.minValue;
        			bestChild = c;
        		}
//        		chkVal = c.totValue/(c.nVisits + epsilon) + r.nextFloat()*epsilon;
//        		if(c.terminates && chkVal < bstVal) {
//        			bstVal = chkVal;
//        			bestChild = c;
//        		}
        	}
        	
//        	System.out.println(bestChild.items);
        	path.add(bestChild.items);
        	curr = bestChild;
    	}

    	
//    	System.out.println(path);
    	return path;
    }

}
