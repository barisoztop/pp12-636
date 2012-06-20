package evaluation;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;


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
	
	
	//the actual statistics
	
	/**
	 * true positive rate; fraction of all elements that should have been classified positive and actually were.<p>
	 * true positives / (true positives + false negatives)
	 *  
	 * @return
	 */
	public double getRecall() {
		return (double)truePositives/(double)(truePositives+falseNegatives);
	}
	
	/**
	 * Fraction of elements that were correctly classified as positive from all that were classified positive.<p>
	 * true positives / (true positives + false positives)
	 * @return
	 */
	public double getPrecision() {
		return (double)truePositives/(double)(truePositives+falsePositives);
	}
	
	/**
	 * fraction of those elements correctly classified as negative from all that were classified negative.<p>
	 * true negatives / (true negatives + false positives)
	 * @return
	 */
	public double getSpecificity() {
		return (double)trueNegatives/(double)(trueNegatives+falsePositives);
	}
	
	
	
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
