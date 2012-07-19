package predictor.markov.normalizer;

import java.util.List;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DirectedNeighborIndex;
import predictor.markov.graph.Edge;
import predictor.markov.graph.MarkovDirectedWeightedGraph;
import predictor.markov.graph.Vertex;

/**
 *
 * @author rgreil
 */
public abstract class Normalizer {


	protected MarkovDirectedWeightedGraph graph;
	protected DirectedNeighborIndex<Vertex, Edge> neighbor;
	protected static Logger logger = Logger.getLogger(Normalizer.class);

	protected abstract void compute(List<Edge> list);
}
