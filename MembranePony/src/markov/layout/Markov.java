package markov.layout;

import data.AminoAcid;
import data.Constants;
import data.SSE;
import interfaces.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.Scanner;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import markov.graph.*;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;

/**
 *
 * @author rgreil
 */
public class Markov implements Predictor {

    private static final Logger logger = Logger.getLogger(Markov.class);
    private Graph<Vertex, Edge> wintermute;
    private double hpSteppingValue = 0.1d;
    private double hpRoundingValue = 10d;
    private int hpscaleUsed = -1;
    private static final double HP_MIN = -5.0d;
    private static final double HP_MAX = 6.0d;
    private boolean trained = false;
    private Vertex[][] matrix;
    public final Vertex TMH = new Vertex("TMH", "null", Double.NaN, -1);
    public final Vertex OUTSIDE = new Vertex("OUTSIDE", "null", Double.NaN, -1);
    public final Vertex INSIDE = new Vertex("INSIDE", "null", Double.NaN, -1);
    public final Vertex GECONNYSE = new Vertex("GECONNYSE", "null", Double.NaN, -1);

    public Markov() {
        logger.info("spawning new Markov Instance");
        wintermute = new Graph<Vertex, Edge>(Edge.class);
        wintermute.addVertex(TMH);
        wintermute.addVertex(OUTSIDE);
        wintermute.addVertex(INSIDE);
        wintermute.addVertex(GECONNYSE);
        int hpSteps = (int) ((HP_MAX - HP_MIN) / hpSteppingValue) + 1;
        matrix = new Vertex[AminoAcid.values().length * SSE.values().length * hpSteps][Constants.WINDOW_LENGTH]; //[rows][columns]
    }

    private void addVertices() {
        long start = System.currentTimeMillis();
        logger.info("VERTICES: creating vertices");
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
                        logger.debug("VERTICES: DEBUG: created vertex: " + tmp);
                        value_hp += hpSteppingValue;
                        wintermute.addVertex(tmp);
                        matrix[row][windowPos] = tmp;
                        row++;
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("VERTICES: " + wintermute.vertexSet().size() + " vertices in " + (end - start) + " ms");
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
            throw new VerifyError("WARNING: TRAIN: Model can not be overtrained! Create new empty Instance of markov!");
        }
        addVertices();
        long start = System.currentTimeMillis();
        logger.info("TRAIN: training "+ trainingCases.length + " sequences");
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
                        //spSource is more important, because it always holds the middle
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
                logger.debug("TRAIN: DEBUG: SequencePosition: middle " + spMiddle);
                checkEdge(vertexMiddle, spMiddle, null, true);
            }
        }
        trained = true;
        long end = System.currentTimeMillis();
        logger.info("TRAIN: "+wintermute.edgeSet().size()+" edges in " + (end - start) + " ms");
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
        Edge edge = wintermute.getEdge(source, target);
        if (edge == null) {
            if (!wintermute.containsVertex(source)) {
                logger.fatal("WARNING: vertex source NOT contained: " + source);
            }
            if (!wintermute.containsVertex(target)) {
                logger.fatal("WARNING: vertex target NOT contained: " + target);
            }
            wintermute.addEdge(source, target);
        } else {
            wintermute.setEdgeWeight(edge, (wintermute.getEdgeWeight(edge) + 1));
        }
    }

    public void setMappingContValuesToNodes(double range) {
        hpSteppingValue = range;
        hpRoundingValue = 1 / range;
    }

    public Graph getGraph() {
        return wintermute;
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
            throw new VerifyError("Hydrophobocity scale has changed! Create new Instance of markov!");
        }
    }

    @Override
    public void save(File model) throws Exception {
        if (!trained) {
            throw new VerifyError("SAVE: Can not save an empty model! Train it before!");
        }
        long start = System.currentTimeMillis();
        logger.info("SAVE: "+ model.getAbsolutePath()+" (v: " + wintermute.vertexSet().size() + " | e: " + wintermute.edgeSet().size() + " | " + (model.length() / 1024) + " kb)");
        BufferedWriter bw = new BufferedWriter(new FileWriter(model));
        GraphMLExporter g = new GraphMLExporter(new MarkovVertexNameProvider(), null, new MarkovEdgeNameProvider(), null);
        g.export(bw, wintermute);

        //verify begin
        bw.write("<!-- ");
        bw.write("wintermute:" + ((double) wintermute.vertexSet().size() / (double) wintermute.edgeSet().size()) + ":");
        bw.write(" -->");
        //verify end

        bw.flush();
        bw.close();
        long end = System.currentTimeMillis();
        logger.info("SAVE: in "+ (end - start) + " ms");
    }

    @Override
    public void load(File model) throws Exception {
        if (trained) {
            throw new VerifyError("LOAD: Model can not be overloaded! Create new emtpy Instance of markov!");
        }

        long start = System.currentTimeMillis();
        logger.info("READ: "+model.getAbsolutePath()+ " (" + (model.length() / 1024) + " kb)");

        GraphXmlHandler graphXmlHandler = new GraphXmlHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setXIncludeAware(true);

        SAXParser parser = factory.newSAXParser();
        parser.parse(model, graphXmlHandler);


        logger.info("READ: adding " + graphXmlHandler.getListVertex().size() + " vertices");
        for (String vertex : graphXmlHandler.getListVertex()) {
            String[] parts = vertex.split(":");
            String aa = parts[0].intern();
            String sse = parts[1].intern();
            Double hp = Double.valueOf(parts[2].intern());
            int wp = Integer.valueOf(parts[3].intern());
            wintermute.addVertex(new Vertex(aa, sse, hp, wp));
        }

        logger.info("READ: adding " + graphXmlHandler.getListEdge().size() + " edges");
        for (String edge : graphXmlHandler.getListEdge()) {
            String[] parts = edge.split(";");

            //source
            String[] src = parts[0].split(":");
            String aa = src[0].intern();
            String sse = src[1].intern();
            Double hp = Double.valueOf(src[2].intern());
            int wp = Integer.valueOf(src[3].intern());
            Vertex source = new Vertex(aa, sse, hp, wp);

            //target
            String[] trg = parts[1].split(":");
            aa = trg[0].intern();
            sse = trg[1].intern();
            hp = Double.valueOf(trg[2].intern());
            wp = Integer.valueOf(trg[3].intern());
            Vertex target = new Vertex(aa, sse, hp, wp);

            //weight
            String[] wgt = parts[2].split(":");
            Double weight = Double.valueOf(wgt[1]);
            wintermute.addEdge(source, target);
            Edge e = wintermute.getEdge(source, target);
            wintermute.setEdgeWeight(e, weight);
        }

        //verify start
        String shc = tail(model);
        if (shc.startsWith("<!-- ")) {
            String[] split = shc.split(" ")[1].split(":");
            if (split[0].equals("wintermute")) {
                double old = Double.parseDouble(split[1]);
                double act = ((double) wintermute.vertexSet().size() / (double) wintermute.edgeSet().size());
                if (old == act) {
                    logger.info("READ: model OK..");
                } else {
                    throw new VerifyError("READ: Model is corrupted and can not be read! Export new model!");
                }
            }
            //verify end

            trained = true;
            long end = System.currentTimeMillis();
            logger.info("READ: in " + (end - start) + " ms");

        }
    }

    public String tail(File file) throws FileNotFoundException, IOException {
        RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
        long fileLength = file.length() - 1;
        StringBuilder sb = new StringBuilder();

        for (long filePointer = fileLength; filePointer != -1; filePointer--) {
            fileHandler.seek(filePointer);
            int readByte = fileHandler.readByte();

            if (readByte == 0xA) {
                if (filePointer == fileLength) {
                    continue;
                } else {
                    break;
                }
            } else if (readByte == 0xD) {
                if (filePointer == fileLength - 1) {
                    continue;
                } else {
                    break;
                }
            }

            sb.append((char) readByte);
        }

        String lastLine = sb.reverse().toString();
        return lastLine;
    }
}