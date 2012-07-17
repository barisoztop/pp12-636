package predictor.markov.graph;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * class for represeting a markov graph (DefaultDirectedWeightedGraph)<br> A
 * directed weighted graph. A directed weighted graph is a non-simple directed
 * graph in which multiple edges between any two vertices are not permitted, but
 * loops are. The graph has weights on its edges.
 *
 * @author rgreil
 */
public class MarkovDirectedWeightedGraph extends DefaultDirectedWeightedGraph<Vertex, Edge> {

		public MarkovDirectedWeightedGraph() {
		super(new ClassBasedEdgeFactory(Edge.class));
	}

//	public MarkovDirectedWeightedGraph(Class<? extends Edge> edgeClass) {
//		super(new ClassBasedEdgeFactory(edgeClass));
//	}
//
//	public MarkovDirectedWeightedGraph(EdgeFactory ef) {
//		super(ef);
//	}

	@Override
	public void setEdgeWeight(Edge edge, double weight) {
		edge.setWeightComplete(weight);
	}

	@Override
	public double getEdgeWeight(Edge e) {
		return e.getWeightComplete();
	}
}
