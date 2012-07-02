package markov.layout;

import java.util.ArrayList;
import markov.graph.Edge;
import markov.graph.Graph;
import markov.graph.Vertex;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DirectedNeighborIndex;

/**
 *
 * @author rgreil
 */
public class Normalizer {

    private static final Logger logger = Logger.getLogger(Normalizer.class);
    private final Graph<Vertex, Edge> graph;
//    private final DirectedNeighborIndex<Vertex, Edge> neighbor;
    private DirectedNeighborIndex<Vertex, Edge> neighbor;
    private double min = Double.POSITIVE_INFINITY;

    public Normalizer(Graph graph) {
        this.graph = graph;
        neighbor = new DirectedNeighborIndex(this.graph);
    }

    public void normalize() {
        long start = System.currentTimeMillis();
        int counter = 0;
        logger.warn("NORMALIZER IS CURRENTLY AT DEBUG STATE -> ALL EDGES WITH WEIGHT <= 1.0 WILL BE DELETED1");
//        System.out.println("EDGES BEFORE: " + graph.edgeSet().size());
        for (Vertex outerVertex : graph.vertexSet()) {
//            ArrayList<Edge> listEdge = new ArrayList<Edge>();
//            double sum = 0d;
            for (Vertex target : neighbor.successorListOf(outerVertex)) {
                Edge e = graph.getEdge(outerVertex, target);
                if (e.getWeight() <= 1.0) {
                    counter++;
                    graph.removeEdge(e);
                }
//                listEdge.add(e);
//                sum += graph.getEdgeWeight(e);
            }
//            for (Edge e : listEdge) {
//                double value = graph.getEdgeWeight(e) / sum;
//                if (value < min) {
//                    min = value;
//                }
//                graph.setEdgeWeight(e, value);
//            }
        }
        neighbor = new DirectedNeighborIndex(this.graph);
//        System.out.println("EDGES REMOVED: " + counter);
//        System.out.println("EDGE AFTER: " + graph.edgeSet().size());
//        long end = System.currentTimeMillis();
//        logger.info("EDGES WITH WEIGHT 1.0 in " + (end - start) + " ms");


//        long start = System.currentTimeMillis();
        for (Vertex outerVertex : graph.vertexSet()) {
            ArrayList<Edge> listEdge = new ArrayList<Edge>();
            double sum = 0d;
            for (Vertex target : neighbor.successorListOf(outerVertex)) {
                Edge e = graph.getEdge(outerVertex, target);
                listEdge.add(e);
                sum += graph.getEdgeWeight(e);
            }
            for (Edge e : listEdge) {
                double value = graph.getEdgeWeight(e) / sum;
                if (value < min) {
                    min = value;
                }
                graph.setEdgeWeight(e, value);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("normalized in " + (end - start) + " ms");


    }

    public double getNormalizedMin() {
        return min;
    }
}
