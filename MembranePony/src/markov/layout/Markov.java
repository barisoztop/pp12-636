package markov.layout;

import data.AminoAcid;
import data.Constants;
import data.SSE;
import interfaces.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import javax.xml.transform.TransformerConfigurationException;
import markov.graph.*;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;
import org.xml.sax.SAXException;

/**
 *
 * @author rgreil
 */
public class Markov implements Predictor {

    private static final Logger logger = Logger.getLogger(Markov.class);
    private final Graph<Vertex, Edge> ideetler;
//    private double[] hpMatrixMinMax;
    private final File out = new File("markov.graph");
    private double hpSteppingValue = 0.1d;
    private double hpRoundingValue = 10d;
    private int hpscaleUsed = -1;
    private static final double HP_MIN = -5.0d;
    private static final double HP_MAX = 6.0d;
    private boolean trained = false;
    private Vertex[][] matrix;
//    private final Vertex TMH_TRUE = new Vertex("TMH_TRUE", null, null, -1);
//    private final Vertex TMH_FALSE = new Vertex("TMH_FALSE", null, null, -1);
    public final Vertex TMH = new Vertex("TMH", null, null, -1);
    public final Vertex OUTSIDE = new Vertex("OUTSIDE", null, null, -1);
    public final Vertex INSIDE = new Vertex("INSIDE", null, null, -1);

    public Markov() {
        logger.info("spawning new Markov Instance");
        ideetler = new Graph<Vertex, Edge>(Edge.class);
        ideetler.addVertex(TMH);
        ideetler.addVertex(OUTSIDE);
        ideetler.addVertex(INSIDE);
        int hpSteps = (int) ((HP_MAX - HP_MIN) / hpSteppingValue) + 1;
        matrix = new Vertex[AminoAcid.values().length * SSE.values().length * hpSteps][Constants.WINDOW_LENGTH]; //[rows][columns]
        addVertices();
    }

    private void addVertices() {
        long start = System.currentTimeMillis();
        logger.info("creating vertices..");
        //create nodes and add them to the graph
        for (int windowPos = 0; windowPos < matrix[0].length; windowPos++) {
            //windowPos = the position of the vertex in the markov model [0 - (Constants.WINDOW_LENGTH - 1))
            int row = 0;
            for (int sse = 0; sse < SSE.values().length; sse++) {
                //sse = the secondary structure of all available at SSE.values()
                String value_sse = SSE.values()[sse].toString().intern();
                for (int aa = 0; aa < AminoAcid.values().length; aa++) {
                    //aa = the aminoacid of all available at AminoAcid.values()
                    String value_aa = AminoAcid.values()[aa].toString().intern();
                    double value_hp = HP_MIN;
                    while (value_hp < HP_MAX) {
                        //hp = the hydrophobocity value from min to max
                        Vertex tmp = new Vertex(value_aa, value_sse, round(value_hp), windowPos);
                        logger.debug("created vertex: " + tmp);
                        value_hp += hpSteppingValue;
                        ideetler.addVertex(tmp);
                        matrix[row][windowPos] = tmp;
                        row++;
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("created " + ideetler.vertexSet().size() + " vertices in " + (end - start) + " ms");
    }

    private double round(double value) {
        double result = value * hpRoundingValue;
        result = Math.round(result);
        result = result / hpRoundingValue;
        return result;
    }

//    @Deprecated
//    private void addEdges() {
//        System.out.println("addEdges:start");
//        for (Vertex source : ideetler.vertexSet()) {
//            if (source.equals(TMH) || source.equals(OUTSIDE) || source.equals(INSIDE)) {
//                continue;
//            }
//            int posSource = source.getWindowPos();
//            if (posSource == (matrix[0].length - 1)) {
//                //add edges from source to TMH, OUTSIDE, INSIDE (endvertex)
//                ideetler.addEdge(source, TMH);
//                ideetler.addEdge(source, OUTSIDE);
//                ideetler.addEdge(source, INSIDE);
//            } else {
//                //add edges from source (windowPos) to target (windowPos+1)
//                for (int r = 0; r < matrix.length; r++) {
//                    Vertex target = matrix[r][posSource + 1];
//                    ideetler.addEdge(source, target);
//                }
//            }
//        }
//        System.out.println("addEdges:end");
//    }
    @Override
    public Prediction predict(Sequence sequence) {
        checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void train(Sequence[] trainingCases) {
        if (trained) {
            throw new VerifyError("Model can't be overtrained! Create new Instance of markov!");
        }
        long start = System.currentTimeMillis();
        logger.info("training sequences..");
        checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());
        int middle = (Constants.WINDOW_LENGTH / 2);
        for (Sequence sequence : trainingCases) {
            for (SlidingWindow slidingWindow : sequence.getWindows()) {
                Vertex vertexMiddle = null;
                SequencePosition spMiddle = null;

                for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
                    SequencePosition spSource = slidingWindow.getSequence()[i];
                    SequencePosition spTarget = slidingWindow.getSequence()[i + 1];

                    //source
                    if (spSource == null) {
                        //spSource is more important, because it holds always the middle
                        //because we are going from left to right inside the window..
                        continue;
                    }
                    String sourceAa = spSource.getAminoAcid().toString().intern();
                    String sourceSse = spSource.getSecondaryStructure().toString().intern();
                    Double sourceHp = round(spSource.getHydrophobicity());
                    Vertex vertexSource = new Vertex(sourceAa, sourceSse, sourceHp, i);

                    //check if vertex source is in the middle of the window
                    //if yes -> backup
                    if (i == middle) {
                        vertexMiddle = vertexSource;
                        spMiddle = spSource;
                    }

                    //target
                    if (spTarget != null) {
                        //if null, just ignore the lame target
                        String targetAa = spTarget.getAminoAcid().toString().intern();
                        String targetSse = spTarget.getSecondaryStructure().toString().intern();
                        Double targetHp = round(spTarget.getHydrophobicity());
                        Vertex vertexTarget = new Vertex(targetAa, targetSse, targetHp, i + 1);

                        //link the source and target vertices
                        checkEdge(vertexSource, null, vertexTarget, false);
                    }
                }
                //link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
                logger.debug("SequencePosition: middle " + spMiddle);
                checkEdge(vertexMiddle, spMiddle, null, true);
            }
        }
        trained = true;
        long end = System.currentTimeMillis();
        logger.info("trained " + trainingCases.length + " sequences in " + (end - start) + " ms");
    }

    private void checkEdge(Vertex source, SequencePosition spSource, Vertex target, boolean middle) {
        if (middle) {
            Result result = spSource.getRealClass();
            if (result.equals(Result.INSIDE)) {
                target = INSIDE;
            } else if (result.equals(Result.OUTSIDE)) {
                target = OUTSIDE;
            } else if (result.equals(Result.TMH)) {
                target = TMH;
            }
        }
        Edge edge = ideetler.getEdge(source, target);
        if (edge == null) {
            if (!ideetler.containsVertex(source)) {
                logger.fatal("vertex source NOT contained: " + source);
            }
            if (!ideetler.containsVertex(target)) {
                logger.fatal("vertex target NOT contained: " + target);
            }
            ideetler.addEdge(source, target);
        } else {
            ideetler.setEdgeWeight(edge, (ideetler.getEdgeWeight(edge) + 1));
        }
    }

    public void setMappingContValuesToNodes(double range) {
        hpSteppingValue = range;
        hpRoundingValue = 1 / range;
    }

    public Graph getGraph() {
        return ideetler;
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
        } else if (hpscaleUsed != scale) {
            throw new VerifyError("Hydrophobocity scale has changed during test/train! Create new Instance of markov!");
        }
    }

    @Override
    public void save(File model) throws Exception {
        if (!trained) {
            throw new VerifyError("Can't save an empty model! Train it before!");
        }
        long start = System.currentTimeMillis();
        logger.info("saving model to " + model.getAbsolutePath());
        BufferedWriter bw = new BufferedWriter(new FileWriter(model));
        GraphMLExporter g = new GraphMLExporter(new MarkovVertexNameProvider(), null, new MarkovEdgeNameProvider(), null);
        g.export(bw, ideetler);
        bw.flush();
        bw.close();
        long end = System.currentTimeMillis();
        long filesize = model.length() / 1024;
        logger.info("saved model (" + filesize + " kb) in " + (end - start) + " ms");
    }

    @Override
    public void load(File model) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}