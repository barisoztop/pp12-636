package predictor.markov.classifier;

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
			aComplete *= edge.getWeightComplete();
			bComplete *= (1 - edge.getWeightComplete());
			//tmh
			aTmh *= edge.getWeightTmh();
			bTmh *= (1 - edge.getWeightTmh());
			//nonTmh
			aNonTmh *= edge.getWeightNonTmh();
			bNonTmh *= (1 - edge.getWeightNonTmh());
		}

		classRateComplete = aComplete / (aComplete + bComplete);
		classRateTmh = aTmh / (aTmh + bTmh);
		classRateNonTmh = aNonTmh / (aNonTmh + bNonTmh);

		long end = System.currentTimeMillis();
		logger.trace("classified in " + (end - start) + " ms");
	}
}
