package markov.graph;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * class for represeting a markov graph (DefaultDirectedWeightedGraph)<br>
 * A directed weighted graph. A directed weighted graph is a non-simple directed
 * graph in which multiple edges between any two vertices are not permitted, but loops are. The graph has weights on its edges.
 *
 * @author rgreil
 */
public class MarkovDirectedWeightedGraph<V, E> extends DefaultDirectedWeightedGraph<V, E> {

    public MarkovDirectedWeightedGraph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    public MarkovDirectedWeightedGraph(EdgeFactory<V, E> ef) {
        super(ef);
    }
}
