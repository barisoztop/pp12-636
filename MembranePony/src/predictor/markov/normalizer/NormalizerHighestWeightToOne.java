package markov.normalizer;

import data.Constants;
import java.util.ArrayList;
import markov.graph.Edge;
import markov.graph.MarkovDirectedWeightedGraph;
import markov.graph.Vertex;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DirectedNeighborIndex;

/**
 * normalizes all weights of all outgoing edges to be in accordance to weight
 * with highes value<br> sets edges without weight to smalles weight found
 *
 * @author rgreil
 */
public final class NormalizerHighestWeightToOne extends Normalizer {

	public NormalizerHighestWeightToOne(MarkovDirectedWeightedGraph graph) {
		this.graph = graph;
		neighbor = new DirectedNeighborIndex(this.graph);
		logger = Logger.getLogger(NormalizerHighestWeightToOne.class);
		compute();
	}

	@Override
	protected void compute() {
		long start = System.currentTimeMillis();
		for (Vertex source : graph.vertexSet()) {
			ArrayList<Edge> listEdge = new ArrayList<Edge>();
			double maxWeightComplete = Double.MIN_VALUE;
			double maxWeightTmh = Double.MIN_VALUE;
			double maxWeightNonTmh = Double.MIN_VALUE;
			double minWeightTmh = Double.MAX_VALUE;
			double minWeightNonTmh = Double.MAX_VALUE;
			for (Vertex target : neighbor.successorListOf(source)) {
				Edge e = graph.getEdge(source, target);
				listEdge.add(e);

				double weightComplete = e.getWeightComplete();
				if (weightComplete > maxWeightComplete) {
					maxWeightComplete = weightComplete;
				}

				double weightTmh = e.getWeightTmh();
				if (weightTmh > maxWeightTmh) {
					maxWeightTmh = weightTmh;
				} else if (weightTmh != 0d && weightTmh < minWeightTmh) {
					minWeightTmh = weightTmh;
				}

				double weightNonTmh = e.getWeightNonTmh();
				if (weightNonTmh > maxWeightNonTmh) {
					maxWeightNonTmh = weightNonTmh;
				} else if (weightNonTmh != 0 && weightNonTmh < minWeightNonTmh) {
					minWeightNonTmh = weightNonTmh;
				}
			}
			for (Edge e : listEdge) {
				//calculate new weights
				double newWeightComplete = e.getWeightComplete() / maxWeightComplete;

				double newWeightTmh = e.getWeightTmh();
				if (newWeightTmh == 0d) {
					newWeightTmh = minWeightTmh / maxWeightComplete;
				} else {
					newWeightTmh /= maxWeightComplete;
				}

				double newWeightNonTmh = e.getWeightNonTmh();
				if (newWeightNonTmh == 0d) {
					newWeightNonTmh = minWeightNonTmh / maxWeightComplete;
				} else {
					newWeightNonTmh /= maxWeightComplete;
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
