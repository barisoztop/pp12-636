package predictor.markov.classifier;

import data.Constants;
import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;

/**
 * naive bayes rule<br> http://paulgraham.com/naivebayes.html
 *
 * @author greil
 */
public final class ClassifierBayes extends Classifier {

	public ClassifierBayes(List<Edge> listEdge) {
		this.listEdge = listEdge;
		logger = Logger.getLogger(ClassifierBayes.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();

		double aComplete = 1d;
		double bComplete = 1d;
		double aTmh = 1d;
		double bTmh = 1d;
		double aNonTmh = 1d;
		double bNonTmh = 1d;

		for (Edge edge : listEdge) {
			if (edge == null) {
				continue;
			}
			//complete
			double complete = edge.getWeightComplete();
			aComplete *= complete;
			if (complete == 1d) {
				complete = Constants.MIN_EDGE_WEIGHT;
			}
			bComplete *= (1 - complete);

			//tmh
			double tmh = edge.getWeightTmh();
			aTmh *= tmh;
			if (tmh == 1d) {
				tmh = Constants.MIN_EDGE_WEIGHT;
			}
			bTmh *= (1 - tmh);

			//nonTmh
			double nonTmh = edge.getWeightNonTmh();
			aNonTmh *= nonTmh;
			if (nonTmh == 1d) {
				nonTmh = Constants.MIN_EDGE_WEIGHT;
			}
			bNonTmh *= (1 - nonTmh);
		}

		classRateComplete = aComplete / (aComplete + bComplete);
		classRateTmh = aTmh / (aTmh + bTmh);
		classRateNonTmh = aNonTmh / (aNonTmh + bNonTmh);

		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
