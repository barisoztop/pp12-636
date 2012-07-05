package markov.classificator;

import java.util.List;
import markov.graph.Edge;
import org.apache.log4j.Logger;

/**
 * ratio of tmh/nonTmh versus complete weight
 *
 * @author greil
 */
public final class ClassificatorRatio extends Classificator {

	public ClassificatorRatio(List<Edge> listEdge) {
		this.listEdge = listEdge;
		logger = Logger.getLogger(ClassificatorRatio.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();

		double weightSumRatioTmh = 0d;
		double weightSumRatioNonTmh = 0d;
		double weightSumRatioComplete = 0d;

		for (Edge edge : listEdge) {
			if (edge == null) {
				continue;
			}
			weightSumRatioTmh += (edge.getWeightTmh() / edge.getWeightComplete());
			weightSumRatioNonTmh += (edge.getWeightNonTmh() / edge.getWeightComplete());
			weightSumRatioComplete = (edge.getWeightComplete() / edge.getWeightComplete());
		}

		classRateTmh = weightSumRatioTmh;
		classRateNonTmh = weightSumRatioNonTmh;
		classRateComplete = weightSumRatioComplete;


		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
