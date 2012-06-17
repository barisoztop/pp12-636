package markov.layout;

import interfaces.Predictor;
import interfaces.Sequence;
import interfaces.TestTrainingCase;
import markov.graph.Edge;
import markov.graph.Graph;
import markov.graph.Vertex;
import data.Aminoacids;
import data.SSE;

/**
 *
 * @author rgreil
 */
public class Markov implements Predictor {

    private final Graph<Vertex, Edge> markov;
//    private double rangeContValuesToNodes = 0.1;
//    private double hydrophobocityMin = -5.0d;
//    private double hydrophobocityMax = 5.0d;
//    private int sizeSlidingWindow = 21;
    private double rangeContValuesToNodes = 1.0d;
    private double hydrophobocityMin = -2.0d;
    private double hydrophobocityMax = 2.0d;
    private int sizeSlidingWindow = 21;
    private Vertex[][] matrix;
//    private final Vertex TMH_TRUE = new Vertex("TMH_TRUE", null, null, -1);
//    private final Vertex TMH_FALSE = new Vertex("TMH_FALSE", null, null, -1);
    private final Vertex TMH = new Vertex("TMH", null, null, -1);
    private final Vertex OUTSIDE = new Vertex("OUTSIDE", null, null, -1);
    private final Vertex INSIDE = new Vertex("INSIDE", null, null, -1);

    public Markov() {
        markov = new Graph<Vertex, Edge>(Edge.class);
        markov.addVertex(TMH);
        markov.addVertex(OUTSIDE);
        markov.addVertex(INSIDE);
//        int hydro = (int) ((hydrophobocityMax - hydrophobocityMin) / rangeContValuesToNodes);
//        matrix = new Vertex[aminoacids.length * secondaryStructures.length * hydro][sizeSlidingWindow]; //[rows][columns]
        matrix = new Vertex[Aminoacids.get().length * SSE.get().length][sizeSlidingWindow]; //[rows][columns]
        addVertices();
        addEdges();
    }

    private void addVertices() {
        //create nodes and add them to the graph
        for (int windowPos = 0; windowPos < sizeSlidingWindow; windowPos++) {
            int row = 0;
            for (int sse = 0; sse < SSE.get().length; sse++) {
                for (int aa = 0; aa < Aminoacids.get().length; aa++) {
//                    for (double hydrophobocity = hydrophobocityMin; hydrophobocity <= hydrophobocityMax; hydrophobocity += rangeContValuesToNodes) {
//                        Vertex tmp = new Vertex(aminoacids[aa], secondaryStructures[sse], hydrophobocity, windowPos);
                    Vertex tmp = new Vertex(Aminoacids.get()[aa], SSE.get()[sse], 0d, windowPos);
                    markov.addVertex(tmp);
                    matrix[row][windowPos] = tmp;
                    row++;
//                }
                }
            }
        }
    }

    private void addEdges() {
        for (Vertex source : markov.vertexSet()) {
            if (source.equals(TMH) || source.equals(OUTSIDE) || source.equals(INSIDE)) {
                continue;
            }
            int posSource = source.getWindowPos();
            if (posSource == (matrix[0].length - 1)) {
                //add edges from source to TMH_TRUE and TMH_FALSE (endvertex)
                markov.addEdge(source, TMH);
                markov.addEdge(source, OUTSIDE);
                markov.addEdge(source, INSIDE);
            } else {
                //add edges from source (windowPos) to target (windowPos+1)
                for (int r = 0; r < matrix.length; r++) {
                    Vertex target = matrix[r][posSource + 1];
                    markov.addEdge(source, target);
                }
            }
        }
    }

    @Override
    public void predict(Sequence sequence) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void train(TestTrainingCase[] trainingCases) {
    	throw new UnsupportedOperationException("Not supported yet.");

    }

    public void setMappingContValuesToNodes(double range) {
        rangeContValuesToNodes = range;
    }

    public Graph getGraph() {
        return markov;
    }
}
