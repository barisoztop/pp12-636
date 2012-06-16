package markov.graph;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * simple class for represeting a full markov graph
 *
 * @author rgreil
 */
public class Graph<V, E> extends DefaultDirectedWeightedGraph<V, E> {

    public Graph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    public Graph(EdgeFactory<V, E> ef) {
        super(ef);
    }
}
