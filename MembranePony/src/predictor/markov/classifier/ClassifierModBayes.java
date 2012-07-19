package predictor.markov.classifier;

import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;

/**
 * naive bayes somehow modified to work with non-double values
 *
 * @author greil
 */
public final class ClassifierModBayes extends Classifier {

	public ClassifierModBayes(List<Edge> listEdge) {
		this.listEdge = listEdge;
		logger = Logger.getLogger(ClassifierModBayes.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();

//		double weightSumAll = 0d;
//		double weightSumTmh = 0d;
//		double weightSumNonTmh = 0d;

		double weightSumAll = 1d;
		double weightSumTmh = 1d;
		double weightSumNonTmh = 1d;

		for (Edge edge : listEdge) {
			if (edge == null) {
				continue;
			}
			double complete = edge.getWeightComplete();
			double tmh = edge.getWeightTmh();
			double nonTmh = edge.getWeightNonTmh();
//			weightSumAll += complete;
//			weightSumTmh += tmh;
//			weightSumNonTmh += nonTmh;
			weightSumAll *= complete;
			weightSumTmh *= tmh;
			weightSumNonTmh *= nonTmh;
		}


		classRateComplete = weightSumAll / weightSumAll;
		classRateTmh = weightSumTmh / weightSumAll;
		classRateNonTmh = weightSumNonTmh / weightSumAll;

		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
