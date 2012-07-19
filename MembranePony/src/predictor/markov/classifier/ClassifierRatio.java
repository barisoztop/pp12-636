package predictor.markov.classifier;

import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;

/**
 * ratio of tmh/nonTmh versus complete weight
 *
 * @author greil
 */
public final class ClassifierRatio extends Classifier {

	public ClassifierRatio(List<Edge> listEdge) {
		this.listEdge = listEdge;
		logger = Logger.getLogger(ClassifierRatio.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();

//		double weightSumRatioTmh = 0d;
//		double weightSumRatioNonTmh = 0d;
//		double weightSumRatioComplete = 0d;
		double weightSumRatioTmh = 1d;
		double weightSumRatioNonTmh = 1d;
		double weightSumRatioComplete = 1d;

		for (Edge edge : listEdge) {
			if (edge == null) {
				continue;
			}
			double complete = edge.getWeightComplete();
			double tmh = edge.getWeightTmh();
			double nonTmh = edge.getWeightNonTmh();
//			weightSumRatioTmh += (tmh / complete);
//			weightSumRatioNonTmh += (nonTmh / complete);
//			weightSumRatioComplete = 1d;
			weightSumRatioTmh *= (tmh / complete);
			weightSumRatioNonTmh *= (nonTmh / complete);
			weightSumRatioComplete *= 1d;;
		}

		classRateTmh = weightSumRatioTmh;
		classRateNonTmh = weightSumRatioNonTmh;
		classRateComplete = weightSumRatioComplete;


		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
