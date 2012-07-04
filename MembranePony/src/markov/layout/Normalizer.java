package markov.layout;

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
public class Normalizer {

    private static final Logger logger = Logger.getLogger(Normalizer.class);
    private final MarkovDirectedWeightedGraph<Vertex, Edge> graph;
    private final DirectedNeighborIndex<Vertex, Edge> neighbor;
    private double min = Double.POSITIVE_INFINITY;

    public Normalizer(MarkovDirectedWeightedGraph graph) {
        this.graph = graph;
        neighbor = new DirectedNeighborIndex(this.graph);
    }

    public void normalize() {
        long start = System.currentTimeMillis();
        for (Vertex source : graph.vertexSet()) {
            ArrayList<Edge> listEdge = new ArrayList<Edge>();
            double sumWeightAll = 0d;
            double sumWeightTmh = 0d;
            double sumWeightNonTmh = 0d;
            for (Vertex target : neighbor.successorListOf(source)) {
                Edge e = graph.getEdge(source, target);
                listEdge.add(e);
                sumWeightAll += graph.getEdgeWeight(e);
                sumWeightTmh += e.getWeightTmh();
                sumWeightNonTmh += e.getWeightNonTmh();

            }
            for (Edge e : listEdge) {
                //calculate new weights
                double newWeightAll = graph.getEdgeWeight(e) / sumWeightAll;
                double newWeightTmh = e.getWeightTmh() / sumWeightTmh;
                double newWeightNonTmh = e.getWeightNonTmh() / sumWeightNonTmh;

                //set new weights
                graph.setEdgeWeight(e, newWeightAll);
                e.setWeight(true, newWeightTmh);
                e.setWeight(false, newWeightNonTmh);

                //backup minima
                if (newWeightAll < min) {
                    min = newWeightAll;
                }
            }
        }
        logger.info("NORMALIZING ALL: weightAll && weightTmh && weightNonTmh");
        long end = System.currentTimeMillis();
        logger.info("normalized in " + (end - start) + " ms");
    }

    public double getNormalizedMin() {
        return min;
    }
}
