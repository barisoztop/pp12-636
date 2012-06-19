package evaluation;

public class EvaluationResult {
	
	/**
	 * the individual runs of the tenfold crossvalidation
	 */
	public final EvaluationRun[] evaluationRuns;

	
	public EvaluationResult(EvaluationRun[] evaluationRuns) {
		this.evaluationRuns = evaluationRuns;
	}
	
	//TODO: add methods to calculate f-measure, recall, precision over all runs?
	//TODO: quartiles maybe?
	//put into Statistics and use Statistics.addStatistics() for this (probably easiest)
	
}
