package markov.layout;

import data.*;
import interfaces.Predictor;
import interfaces.Sequence;
import interfaces.SequencePosition;
import interfaces.SlidingWindow;
import java.math.BigDecimal;
import markov.graph.Edge;
import markov.graph.Graph;
import markov.graph.Vertex;

/**
 *
 * @author rgreil
 */
public class Markov implements Predictor {

    private final Graph<Vertex, Edge> markov;
    private double[] hpMatrixMinMax;
    private int hpAllSteps;
    private double hpSteppingValue = 0.1d;
    private int hpscaleUsed = -1;
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
        for (int i = 0; i < Constants.WINDOW_LENGTH; i++) {
            Vertex NULL = new Vertex("NULL", null, null, i);
            markov.addVertex(NULL);
        }
//        matrix = new Vertex[Aminoacids.get().length * SSE.get().length][Constants.WINDOW_LENGTH]; //[rows][columns]
    }

    private void addVertices() {
        System.out.println("addVertices:start");
        //create nodes and add them to the graph
        for (int windowPos = 0; windowPos < matrix[0].length; windowPos++) {
            //windowPos = the position of the vertex in the markov model [0 - (Constants.WINDOW_LENGTH - 1))
            int row = 0;
            for (int sse = 0; sse < SSE.values().length; sse++) {
                //sse = the secondary structure of all available at SSE.values()
                String value_sse = SSE.values()[sse].toString();
                for (int aa = 0; aa < AminoAcid.values().length; aa++) {
                    //aa = the aminoacid of all available at AminoAcid.values()
                    String value_aa = AminoAcid.values()[aa].toString();
//                    for (int hp = 0; hp < hpAllSteps; hp++) {
//                        //hp = the hydrophobocity value from min to max
//                        double value_hp = (hpMatrixMinMax[0] + (double) hp * hpSteppingValue);
//                        Vertex tmp = new Vertex(value_aa, value_sse, value_hp, windowPos);
                        Vertex tmp = new Vertex(value_aa, value_sse, 0d, windowPos);
                        markov.addVertex(tmp);
                        matrix[row][windowPos] = tmp;
                        row++;
//                    }
                }
            }
        }
        System.out.println("addVertices: end");
    }

    private double round3(double value) {
        double result = value * 1000;
        result = Math.round(result);
        result = result / 1000;
        return result;
    }

    private void addEdges() {
        System.out.println("addEdges:start");
        for (Vertex source : markov.vertexSet()) {
            if (source.equals(TMH) || source.equals(OUTSIDE) || source.equals(INSIDE)) {
                continue;
            }
            int posSource = source.getWindowPos();
            if (posSource == (matrix[0].length - 1)) {
                //add edges from source to TMH, OUTSIDE, INSIDE (endvertex)
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
        System.out.println("addEdges:end");
    }

    @Override
    public void predict(Sequence sequence) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void train(Sequence[] trainingCases) {
        checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());
//        matrix = new Vertex[AminoAcid.values().length * SSE.values().length * hpAllSteps][Constants.WINDOW_LENGTH]; //[rows][columns]
        matrix = new Vertex[AminoAcid.values().length * SSE.values().length][Constants.WINDOW_LENGTH]; //[rows][columns]
        addVertices();
        addEdges();
        for (Sequence sequence : trainingCases) {
            for (SlidingWindow slidingWindow : sequence.getWindows()) {
                for (SequencePosition sequencePosition : slidingWindow.getSequence()) {
                    sequencePosition.
                }
            }
        }


    }

    public void setMappingContValuesToNodes(double range) {
        hpSteppingValue = range;
    }

    public Graph getGraph() {
        return markov;
    }

    /**
     * checks hpscale for staying the same during train and test
     *
     * @param scale
     * @throws VerifyError if scale has changed within same instance of class
     */
    private void checkScale(int scale) {
        if (hpscaleUsed == -1) {
            hpscaleUsed = scale;
            hpMatrixMinMax = Hydrophobicity.getMinMax(hpscaleUsed);
            hpAllSteps = (int) ((hpMatrixMinMax[1] - hpMatrixMinMax[0]) / hpSteppingValue);
            System.out.println("hp: min: "+hpMatrixMinMax[0]+" max: "+hpMatrixMinMax[1]+" hpSteppingValue: "+hpSteppingValue+ "-> hpAllSteps: "+hpAllSteps);

        } else if (hpscaleUsed != scale) {
            throw new VerifyError("Hydrophobocity scale has changed during test/train! Create new Instance of class!");
        }
    }
}