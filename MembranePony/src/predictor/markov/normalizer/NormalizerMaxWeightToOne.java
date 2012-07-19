package predictor.markov.normalizer;

import data.Constants;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;
import predictor.markov.graph.MarkovDirectedGraph;
import predictor.markov.graph.Vertex;

/**
 * normalizes all weights of all outgoing edges to be in accordance to weight
 * with highes value<br> sets edges without weight to smalles weight found
 *
 * @author rgreil
 */
public final class NormalizerMaxWeightToOne extends Normalizer {

	public NormalizerMaxWeightToOne(MarkovDirectedGraph graph) {
		long start = System.currentTimeMillis();
		this.graph = graph;
//		neighbor = new DirectedNeighborIndex(this.graph);
		logger = Logger.getLogger(NormalizerMaxWeightToOne.class);
		for (Vertex source : graph.vertexSet()) {
			List<Edge> outgoingEdges = new ArrayList(graph.outgoingEdgesOf(source));
			List<Edge> listNormalEdge = new ArrayList<Edge>();
			List<Edge> listSpecialEdge = new ArrayList<Edge>();
			for (Edge edge : outgoingEdges) {
				if (edge.getTarget().toString().toLowerCase().contains("final")) {
					//do special calculation with final edges
					listSpecialEdge.add(edge);
				} else {
					listNormalEdge.add(edge);
				}
			}
			compute(listNormalEdge);
			compute(listSpecialEdge);
		}
		logger.info("NORMALIZING ALL: weightAll && weightTmh && weightNonTmh");
		long end = System.currentTimeMillis();
		logger.trace("normalized in " + (end - start) + " ms");
	}

	@Override
	protected void compute(List<Edge> list) {
//		double sumWeightComplete = 0d;
//		double sumWeightTmh = 0d;
//		double sumWeightNonTmh = 0d;
		double minWeightComplete = Double.MAX_VALUE;
		double minWeightTmh = Double.MAX_VALUE;
		double minWeightNonTmh = Double.MAX_VALUE;
		double countNullComplete = 0d;
		double countNullTmh = 0d;
		double countNullNonTmh = 0d;
		double maxWeightComplete = Double.MIN_VALUE;
		double maxWeightTmh = Double.MIN_VALUE;
		double maxWeightNonTmh = Double.MIN_VALUE;

		for (Edge e : list) {
			//sum complete
			double weightComplete = e.getWeightComplete();
			if (weightComplete == 0d) {
				//complete null weight
				countNullComplete++;
			} else {
				if (weightComplete < minWeightComplete) {
					minWeightComplete = weightComplete;
				}
				if (weightComplete > maxWeightComplete) {
					maxWeightComplete = weightComplete;
				}
			}

			//sum tmh
			double weightTmh = e.getWeightTmh();
			if (weightTmh == 0d) {
				//tmh null weight
				countNullTmh++;
			} else {
				if (weightTmh < minWeightTmh) {
					minWeightTmh = weightTmh;
				}
				if (weightTmh > maxWeightTmh) {
					maxWeightTmh = weightTmh;
				}
			}

			//sum nonTmh
			double weightNonTmh = e.getWeightNonTmh();
			if (weightNonTmh == 0) {
				//nonTmh null weight
				countNullNonTmh++;
			} else {
				if (weightNonTmh < minWeightNonTmh) {
					minWeightNonTmh = weightNonTmh;
				}
				if (weightNonTmh > maxWeightNonTmh) {
					maxWeightNonTmh = weightNonTmh;
				}
			}
		}

		if (minWeightComplete == Double.MAX_VALUE) {
			minWeightComplete = Constants.MIN_EDGE_WEIGHT;
		}
		if (minWeightTmh == Double.MAX_VALUE) {
			minWeightTmh = Constants.MIN_EDGE_WEIGHT;
		}
		if (minWeightNonTmh == Double.MAX_VALUE) {
			minWeightNonTmh = Constants.MIN_EDGE_WEIGHT;
		}

		for (Edge e : list) {
			//calculate new weights

			//new complete
			double newWeightComplete = e.getWeightComplete();
			if (newWeightComplete == 0d) {
				if (maxWeightComplete == 0d) {
					newWeightComplete = minWeightComplete * (1d / countNullComplete);
				} else {
					newWeightComplete = (minWeightComplete / Math.pow(maxWeightComplete, 2)) * (1d / countNullComplete);
				}
			} else {
				newWeightComplete /= maxWeightComplete;
			}

			//new tmh
			double newWeightTmh = e.getWeightTmh();
			if (newWeightTmh == 0d) {
				if (maxWeightTmh == 0d) {
					newWeightTmh = minWeightTmh * (1d / countNullTmh);
				} else {
					newWeightTmh = (minWeightTmh / Math.pow(maxWeightTmh, 2)) * (1d / countNullTmh);
				}
			} else {
				newWeightTmh /= maxWeightTmh;
			}

			//new nonTmh
			double newWeightNonTmh = e.getWeightNonTmh();
			if (newWeightNonTmh == 0d) {
				if (maxWeightNonTmh == 0d) {
					newWeightNonTmh = minWeightNonTmh * (1d / countNullNonTmh);
				} else {
					newWeightNonTmh = (minWeightNonTmh / Math.pow(maxWeightNonTmh, 2)) * (1d / countNullNonTmh);
				}
			} else {
				newWeightNonTmh /= maxWeightNonTmh;
			}

			//set new weights
			e.setWeightComplete(newWeightComplete);
			e.setWeightTmh(newWeightTmh);
			e.setWeightNonTmh(newWeightNonTmh);
		}
	}
}
