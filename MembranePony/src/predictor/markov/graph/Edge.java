package predictor.markov.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * simple class for representing a markovedge
 *
 * @author rgreil
 */
public class Edge extends DefaultWeightedEdge {

	private double weightTmh = 0d;
	private double weightNonTmh = 0d;
	private double weightComplete = 0d;

	@Override
	public String toString() {
		return "(" + getSource() + " : " + getTarget() + ")_w:" + getWeightComplete() + "_tmh:" + getWeightTmh() + "_nontmh:" + getWeightNonTmh();
	}

	public double getWeightTmh() {
		return weightTmh;
	}

	public void setWeightTmh(double d) {
		weightTmh = d;
	}

	public double getWeightComplete() {
		return weightComplete;
	}

	public void setWeightComplete(double d) {
		weightComplete = d;
	}

	public double getWeightNonTmh() {
		return weightNonTmh;
	}

	public void setWeightNonTmh(double d) {
		weightNonTmh = d;
	}

	@Override
	public Vertex getSource() {
		return (Vertex) super.getSource();
	}

	@Override
	public Vertex getTarget() {
		return (Vertex) super.getTarget();
	}
}
