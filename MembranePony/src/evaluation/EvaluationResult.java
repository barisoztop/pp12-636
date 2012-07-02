package evaluation;

public class EvaluationResult {

	/**
	 * the individual runs of the tenfold crossvalidation
	 */
	public final EvaluationRun[] evaluationRuns;


	public EvaluationResult(EvaluationRun[] evaluationRuns) {
		this.evaluationRuns = evaluationRuns;
	}



	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "inside".
	 * @return
	 */
	public Statistics getStatisticsInside() {
		Statistics s = new Statistics();

		for(EvaluationRun run : evaluationRuns)
			for(TestCase t : run.getTestCases())
				s.addStatistics(t.inside);

		return s;
	}

	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "outside".
	 * @return
	 */
	public Statistics getStatisticsOutside() {
		Statistics s = new Statistics();

		for(EvaluationRun run : evaluationRuns)
			for(TestCase t : run.getTestCases())
				s.addStatistics(t.outside);

		return s;
	}

	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "transmembrane".
	 * @return
	 */
	public Statistics getStatisticsTMH() {
		Statistics s = new Statistics();

		for(EvaluationRun run : evaluationRuns)
			for(TestCase t : run.getTestCases())
				s.addStatistics(t.transmembrane);

		return s;
	}


	/**
	 * use the {@link Statistics} object generated by this method to calculate recall, precision, specificity
	 * with regard to class "transmembrane".
	 * @return
	 */
	public Statistics getStatisticsNonTMH() {
		Statistics s = new Statistics();

		for(EvaluationRun run : evaluationRuns)
			for(TestCase t : run.getTestCases())
				s.addStatistics(t.nontmh);

		return s;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Evaluation - "+evaluationRuns.length+" runs performed.\n\n");

		sb.append("Overall statistics:\n");
		sb.append("  TMH     => ").append(getStatisticsTMH()).append("\n");
		sb.append("  Non-TMH => ").append(getStatisticsNonTMH()).append("\n");
		sb.append("  Outside => ").append(getStatisticsOutside()).append("\n");
		sb.append("  Inside  => ").append(getStatisticsInside()).append("\n");
		sb.append("\n\n");

		for(int i=0; i<evaluationRuns.length; i++)
			sb.append("Run #").append(i).append("\n").append(evaluationRuns[i]).append("\n\n");

		return sb.toString();

	}

}
