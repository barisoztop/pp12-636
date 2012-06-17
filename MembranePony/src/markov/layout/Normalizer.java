package markov.layout;

import java.util.ArrayList;
import markov.graph.Graph;

/**
 *
 * @author rgreil
 */
public class Normalizer {

    private final Graph g;

    public Normalizer(Markov markov) {
        g = markov.getGraph();
    }

    public void normalize() {
        double[][] matrixEdgeWeights = markovGraph.getMatrixEdgeWeights();
        boolean[][] matrixSafety = markovGraph.getMatrixSafety();
        for (int r = 0; r < matrixEdgeWeights.length; r++) {
            double sum = 0d;
            ArrayList<Integer> tmpPos = new ArrayList<>();
            for (int c = 0; c < matrixEdgeWeights[0].length; c++) {
                if (matrixSafety[r][c]) {
                    sum += matrixEdgeWeights[r][c];
                    tmpPos.add(c);
                }
            }
            for (int i = 0; i < tmpPos.size(); i++) {
                double value = (matrixEdgeWeights[r][tmpPos.get(i)] / sum);
                markovGraph.getMatrixEdgeWeights()[r][tmpPos.get(i)] = value;
                markovGraph.getGraph().setEdgeWeight(markovGraph.getEdge(r, tmpPos.get(i)), value);
            }
        }
    }
    }
}
