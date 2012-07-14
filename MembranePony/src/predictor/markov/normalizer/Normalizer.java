package markov.normalizer;

import data.Constants;
import java.util.ArrayList;
import markov.graph.Edge;
import markov.graph.MarkovDirectedWeightedGraph;
import markov.graph.Vertex;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DirectedNeighborIndex;

/**
 *
 * @author rgreil
 */
public abstract class Normalizer {


	protected MarkovDirectedWeightedGraph<Vertex, Edge> graph;
	protected DirectedNeighborIndex<Vertex, Edge> neighbor;
	protected static Logger logger = Logger.getLogger(Normalizer.class);

	protected abstract void compute();
}
