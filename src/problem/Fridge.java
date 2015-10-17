package problem;

public class Fridge {
	
	/** Name of fridge */
	private String name;
	/** Max number of items stored */
	private int capacity;
	/** Max number of item types a user might need in a week */
	private int maxTypes;
	/** Max number of items per item type a user might need in a week */
	private int maxItemsPerType;
	
	/**
	 * Constructor
	 * @param name
	 * @param capacity
	 * @param maxTypes
	 * @param maxItemsPerType
	 */
	public Fridge(String name, int capacity, int maxTypes,
			int maxItemsPerType) {
		this.name = name;
		this.capacity = capacity;
		this.maxTypes = maxTypes;
		this.maxItemsPerType = maxItemsPerType;
	}
	
	/**
	 * Constructor
	 * @param name Takes values tiny, small, medium, large or super
	 */
	public Fridge(String name) {
		this.name = name;
		if (name.equals("tiny")) { 
			capacity = 3;
			maxTypes = 3;
			maxItemsPerType = 2;
		} else if (name.equals("small")) {
			capacity = 5;
			maxTypes = 5;
			maxItemsPerType = 2;
		} else if (name.equals("medium")) {
			capacity = 10;
			maxTypes = 10;
			maxItemsPerType = 2;
		} else if (name.equals("large")) {
			capacity = 20;
			maxTypes = 25;
			maxItemsPerType = 3;
		} else if (name.equals("super")) {
			capacity = 40;
			maxTypes = 45;
			maxItemsPerType = 4;
		} else {
			throw new IllegalArgumentException("Invalid fridge name.");
		}
	}

	public String getName() {
		return name;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getMaxTypes() {
		return maxTypes;
	}

	public int getMaxItemsPerType() {
		return maxItemsPerType;
	}
}
