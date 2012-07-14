package predictor.markov.normalizer;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DirectedNeighborIndex;
import predictor.markov.graph.Edge;
import predictor.markov.graph.MarkovDirectedWeightedGraph;
import predictor.markov.graph.Vertex;

/**
 * normalizes all weights of all outgoing edges to be in sum 1<br> sets edges
 * without weight to smalles weight found
 *
 * @author rgreil
 */
public final class NormalizerMarkov extends Normalizer {

	public NormalizerMarkov(MarkovDirectedWeightedGraph graph) {
		this.graph = graph;
		neighbor = new DirectedNeighborIndex(this.graph);
		logger = Logger.getLogger(NormalizerMarkov.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();
		for (Vertex source : graph.vertexSet()) {
			ArrayList<Edge> listEdge = new ArrayList<Edge>();
			double sumWeightComplete = 0d;
			double sumWeightTmh = 0d;
			double sumWeightNonTmh = 0d;
			double minWeightTmh = Double.MAX_VALUE;
			double minWeightNonTmh = Double.MAX_VALUE;
			for (Vertex target : neighbor.successorListOf(source)) {
				Edge e = graph.getEdge(source, target);
				listEdge.add(e);

				double weightComplete = e.getWeightComplete();
				sumWeightComplete += weightComplete;

				double weightTmh = e.getWeightTmh();
				if (weightTmh != 0d && weightTmh < minWeightTmh) {
					minWeightTmh = weightTmh;
				}
				sumWeightTmh += weightTmh;

				double weightNonTmh = e.getWeightNonTmh();
				if (weightNonTmh != 0 && weightNonTmh < minWeightNonTmh) {
					minWeightNonTmh = weightNonTmh;
				}
				sumWeightNonTmh += weightNonTmh;

			}
			for (Edge e : listEdge) {
				//calculate new weights
				double newWeightComplete = e.getWeightComplete() / sumWeightComplete;

				double newWeightTmh = e.getWeightTmh();
				if (newWeightTmh == 0d) {
					newWeightTmh = minWeightTmh / sumWeightTmh;
				} else {
					newWeightTmh /= sumWeightTmh;
				}

				double newWeightNonTmh = e.getWeightNonTmh();
				if (newWeightNonTmh == 0d) {
					newWeightNonTmh = minWeightNonTmh / sumWeightNonTmh;
				} else {
					newWeightNonTmh /= sumWeightNonTmh;
				}

				//set new weights
				e.setWeightComplete(newWeightComplete);
				e.setWeight(true, newWeightTmh);
				e.setWeight(false, newWeightNonTmh);
			}
		}

		logger.info("NORMALIZING ALL: weightAll && weightTmh && weightNonTmh");
		long end = System.currentTimeMillis();
		logger.trace("normalized in " + (end - start) + " ms");
	}
}
