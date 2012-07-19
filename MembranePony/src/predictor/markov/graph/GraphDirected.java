package predictor.markov.graph;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * class for represeting a directed graph (DefaultDirectedGraph)<br> A
 * directed graph is a non-simple directed graph in which multiple edges between
 * any two vertices are not permitted, but loops are. The Edge class allows three differend weights.
 *
 * @author rgreil
 */
public class GraphDirected extends DefaultDirectedGraph<Vertex, Edge> {

	public GraphDirected() {
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
