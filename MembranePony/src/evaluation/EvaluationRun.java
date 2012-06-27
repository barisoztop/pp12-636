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
	 * @param nontmh 
	 */
	public void logSequence(Sequence sequence, Statistics transmembrane, Statistics nontmh, Statistics inside, Statistics outside) {
		testCases.add(new TestCase(sequence, transmembrane, nontmh, inside, outside));
	}
	
	
	@Override
	public String toString() {
		String s ="EvaluationRun [\n";
		s+="  TMH     => "+getRunStatisticsTMH()+"\n";
		s+="  Non-TMH => "+getRunStatisticsNonTMH()+"\n";
		s+="  Outside => "+getRunStatisticsOutside()+"\n";
		s+="  Inside  => "+getRunStatisticsInside()+"\n";
		s+="]";
		return s;
	}
	
	
	/**
	 * 
	 * @return results for all sequences that were logged with this object (usually all elements of the test set), complete
	 * with true/false positive/negative counts
	 */
	public LinkedList<TestCase> getTestCases() {
		return testCases;
	}
	
	
	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "inside".
	 * @return
	 */
	public Statistics getRunStatisticsInside() {
		Statistics s = new Statistics();
		
		for(TestCase t : testCases)
			s.addStatistics(t.inside);
		
		return s;
	}
	
	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "outside".
	 * @return
	 */
	public Statistics getRunStatisticsOutside() {
		Statistics s = new Statistics();
		
		for(TestCase t : testCases)
			s.addStatistics(t.outside);
		
		return s;
	}

	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "transmembrane".
	 * @return
	 */
	public Statistics getRunStatisticsTMH() {
		Statistics s = new Statistics();
		
		for(TestCase t : testCases)
			s.addStatistics(t.transmembrane);
		
		return s;
	}
	
	
	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "non-transmembrane".
	 * @return
	 */
	public Statistics getRunStatisticsNonTMH() {
		Statistics s = new Statistics();
		
		for(TestCase t : testCases)
			s.addStatistics(t.nontmh);
		
		return s;
	}
}
