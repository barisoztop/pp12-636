package evaluation;


/**
 * stores true/false positive/negative counts for a single sequence, a set of sequences, whatever you want.
 * 
 * @author Felix
 *
 */
public class Statistics {
	
	private int truePositives;
	private int trueNegatives;
	private int falsePositives;
	private int falseNegatives;
	
	
	public Statistics(int truePositives, int trueNegatives, int falsePositives,
			int falseNegatives) {
		super();
		this.truePositives = truePositives;
		this.trueNegatives = trueNegatives;
		this.falsePositives = falsePositives;
		this.falseNegatives = falseNegatives;
	}
	
	public Statistics() {
		
	}
	
	
	/**
	 * add the values of the counters of the given object to this object's counters
	 * @param stats
	 */
	public void addStatistics(Statistics stats) {
		truePositives += stats.truePositives;
		trueNegatives += stats.trueNegatives;
		falsePositives += stats.falsePositives;
		falseNegatives += stats.falseNegatives;
	}
	
	
	
	//TODO add methods for recall, f-measure, precision, quartiles?
	

	
	//convenience methods for incrementing individual counters by 1

	public void addTruePositive() {
		truePositives++;
	}
	
	public void addTrueNegative() {
		trueNegatives++;
	}
	
	public void addFalsePositive() {
		falsePositives++;
	}
	
	public void addFalseNegative() {
		falseNegatives++;
	}
	
	
	public int getTruePositives() {
		return truePositives;
	}

	
	
	//getters/setters

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}


	public int getTrueNegatives() {
		return trueNegatives;
	}


	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}


	public int getFalsePositives() {
		return falsePositives;
	}


	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}


	public int getFalseNegatives() {
		return falseNegatives;
	}


	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}
	
	
	
}
