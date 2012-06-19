package evaluation;

import interfaces.Sequence;

import java.util.LinkedList;


/**
 * a single run of the tenfold crossvalidation. there's ten of them.
 * 
 * @author Felix
 *
 */
public class EvaluationRun {
	
	/**
	 * each sequence from the test set is one test case; these end up here, along with true/false positive/negative counts.
	 */
	private LinkedList<TestCase> testCases = new LinkedList<TestCase>();
	
	/**
	 * the evaluation that generated this result
	 */
	private Evaluation evaluation;
	
		
	public EvaluationRun(Evaluation evaluation) {
		this.evaluation = evaluation;
	}
	
	
	/**
	 * add a record for a single sequence from the test set
	 * 
	 * @param sequence the sequence that was tested
	 * @param transmembrane true/false positive/negative counts for class transmembrane
	 * @param inside true/false positive/negative counts for class inside
	 * @param outside true/false positive/negative counts for class outside
	 */
	public void logSequence(Sequence sequence, Statistics transmembrane, Statistics inside, Statistics outside) {
		testCases.add(new TestCase(sequence, transmembrane, inside, outside));
	}
	
	
	/**
	 * 
	 * @return results for all sequences that were logged with this object (usually all elements of the test set), complete
	 * with true/false positive/negative counts
	 */
	public LinkedList<TestCase> getTestCases() {
		return testCases;
	}
	
	
	//TODO: add methods to calculate f-measure, recall, precision over this run?
	//TODO: quartiles maybe?
	//put into Statistics and use Statistics.addStatistics() for this (probably easiest)
	
	
}
