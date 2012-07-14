package predictor.markov.classificator;

import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;

/**
 * naive bayes somehow modified to work with non-double values
 *
 * @author greil
 */
public final class ClassificatorModBayes extends Classificator {

	public ClassificatorModBayes(List<Edge> listEdge) {
		this.listEdge = listEdge;
		logger = Logger.getLogger(ClassificatorModBayes.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();

		double weightSumAll = 0d;
		double weightSumTmh = 0d;
		double weightSumNonTmh = 0d;

		for (Edge edge : listEdge) {
			if (edge == null) {
				continue;
			}
			weightSumAll += edge.getWeightComplete();
			weightSumTmh += edge.getWeightTmh();
			weightSumNonTmh += edge.getWeightNonTmh();
		}


		classRateComplete = weightSumAll / weightSumAll;
		classRateTmh = weightSumTmh / weightSumAll;
		classRateNonTmh = weightSumNonTmh / weightSumAll;

		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
