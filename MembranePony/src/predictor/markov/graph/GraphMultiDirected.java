package predictor.markov.graph;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * class for represeting a directed multigraph (DirectedMultiGraph)<br> A
 * directed multigraph is a non-simple directed graph in which loops and
 * multiple edges between any two vertices are permitted. The Edge class allows
 * three differend weights.
 *
 * @author rgreil
 */
public class GraphMultiDirected extends DirectedMultigraph<Vertex, Edge> {

	public GraphMultiDirected() {
		super(new ClassBasedEdgeFactory(Edge.class));
	}

	@Override
	public void setEdgeWeight(Edge edge, double weight) {
		edge.setWeightComplete(weight);
	}

	@Override
	public double getEdgeWeight(Edge e) {
		return e.getWeightComplete();
	}
}
