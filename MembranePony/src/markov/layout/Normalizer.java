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
    private final DirectedNeighborIndex<Vertex, Edge> neighbor;
    private double min = Double.POSITIVE_INFINITY;

    public Normalizer(Graph graph) {
        this.graph = graph;
        neighbor = new DirectedNeighborIndex(this.graph);
    }

    public void normalize() {
        long start = System.currentTimeMillis();
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

        //OLD
//        double[][] matrixEdgeWeights = markovGraph.getMatrixEdgeWeights();
//        boolean[][] matrixSafety = markovGraph.getMatrixSafety();
//        for (int r = 0; r < matrixEdgeWeights.length; r++) {
//            double sum = 0d;
//            ArrayList<Integer> tmpPos = new ArrayList<>();
//            for (int c = 0; c < matrixEdgeWeights[0].length; c++) {
//                if (matrixSafety[r][c]) {
//                    sum += matrixEdgeWeights[r][c];
//                    tmpPos.add(c);
//                }
//            }
//            for (int i = 0; i < tmpPos.size(); i++) {
//                double value = (matrixEdgeWeights[r][tmpPos.get(i)] / sum);
//                markovGraph.getMatrixEdgeWeights()[r][tmpPos.get(i)] = value;
//                markovGraph.getGraph().setEdgeWeight(markovGraph.getEdge(r, tmpPos.get(i)), value);
//            }
//        }
//    }
        long end = System.currentTimeMillis();
        logger.info("normalized in " + (end - start) + " ms");
    }

    public double getNormalizedMin() {
        return min;
    }
}
