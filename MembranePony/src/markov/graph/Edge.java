package markov.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * simple class for representing a markovedge
 *
 * @author rgreil
 */
public class Edge extends DefaultWeightedEdge {

	private double weightTmh = 0d;
	private double weightNonTmh = 0d;

	@Override
	public String toString() {
		return "(" + getSource() + " : " + getTarget() + ")_w:" + getWeight() + "_tmh:" + getWeightTmh() + "_nontmh:" + getWeightNonTmh();
	}

	public double getWeightTmh() {
		return weightTmh;
	}

	public void setWeight(boolean tmh, double d) {
		if (tmh) {
			weightTmh = d;
		} else {
			weightNonTmh = d;
		}
	}

	@Override
	public double getWeight() {
		return super.getWeight();
	}

	public double getWeight(boolean tmh) {
		double result = -1d;
		if (tmh) {
			result = weightTmh;
		} else {
			result = weightNonTmh;
		}
		return result;
	}

	public double getWeightNonTmh() {
		return weightNonTmh;
	}
}
