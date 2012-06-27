package markov.layout;

import data.AminoAcid;
import data.Constants;
import data.SSE;
import interfaces.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<String, Vertex> mapVertex = new HashMap<String, Vertex>();
    private Graph<Vertex, Edge> wintermute;
    private Normalizer norm;
    private double hpSteppingValue = 0.1d;
    private double hpRoundingValue = 10d;
    private int hpscaleUsed = -1;
    private static final double HP_MIN = -5.0d;
    private static final double HP_MAX = 6.0d;
    private boolean trained = false;
//    private Vertex[][] matrix;
    private final int middle = (Constants.WINDOW_LENGTH / 2);
    public final Vertex TMH = new Vertex("TMH", "null", Double.NaN, -1);
    public final Vertex NON_TMH = new Vertex("NON_TMH", "null", Double.NaN, -1);
    public final Vertex OUTSIDE = new Vertex("OUTSIDE", "null", Double.NaN, -1);
    public final Vertex INSIDE = new Vertex("INSIDE", "null", Double.NaN, -1);
    public final Vertex GECONNYSE = new Vertex("GECONNYSE", "null", Double.NaN, -1);
    private double normalizedMin = 0.000001d;

    public Markov() {
        logger.info("spawning new Markov Instance");
        wintermute = new Graph<Vertex, Edge>(Edge.class);
        wintermute.addVertex(TMH);
        wintermute.addVertex(NON_TMH);
        wintermute.addVertex(OUTSIDE);
        wintermute.addVertex(INSIDE);
        wintermute.addVertex(GECONNYSE);
//        int hpSteps = (int) ((HP_MAX - HP_MIN) / hpSteppingValue) + 1;
//        matrix = new Vertex[AminoAcid.values().length * SSE.values().length * hpSteps][Constants.WINDOW_LENGTH]; //[rows][columns]
    }

    private void addVertices() {
        long start = System.currentTimeMillis();
        logger.info("creating vertices");
        //create nodes and add them to the graph
//        for (int windowPos = 0; windowPos < matrix[0].length; windowPos++) {
        for (int windowPos = 0; windowPos < Constants.WINDOW_LENGTH; windowPos++) {
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
                        logger.trace("created vertex: " + tmp);
                        value_hp += hpSteppingValue;
                        mapVertex.put(value_aa.intern() + ":" + value_sse.intern() + ":" + round(value_hp) + ":" + windowPos, tmp);
                        wintermute.addVertex(tmp);
//                        matrix[row][windowPos] = tmp;
                        row++;
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("-> " + wintermute.vertexSet().size() + " vertices in " + (end - start) + " ms");
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
        if (!trained) {
            throw new VerifyError("Can not predict with an empty model! Train it before!");
        }
        checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());
        List<Result> pred = new ArrayList<Result>();
        Result[] predictions = new Result[1];


        //normalize

        //classify

        for (SlidingWindow slidingWindow : sequence.getWindows()) {
            Vertex vertexMiddle = null;
            SequencePosition spMiddle = null;
            double value = 0d;

            for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
                SequencePosition spSource = slidingWindow.getSequence()[i];
                SequencePosition spTarget = slidingWindow.getSequence()[i + 1];

                //source
                if (spSource == null) {
                    continue;
                }

                String sourceAa = spSource.getAminoAcid().toString().intern();
                String sourceSse = spSource.getSecondaryStructure().toString().intern();
                Double sourceHp = round(spSource.getHydrophobicity());
                Vertex vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp + ":" + i);
//
//                //check if vertex source is in the middle of the window
//                //if yes -> backup
                if (i == middle) {
                    vertexMiddle = vertexSource;
                    spMiddle = spSource;
                }

                //target
                if (spTarget != null) {
//                    //if null, just ignore the lame target
                    String targetAa = spTarget.getAminoAcid().toString().intern();
                    String targetSse = spTarget.getSecondaryStructure().toString().intern();
                    Double targetHp = round(spTarget.getHydrophobicity());
                    Vertex vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp + ":" + (i + 1));
//
//                    //link the source and target vertices
                    Edge e = wintermute.getEdge(vertexSource, vertexTarget);
//                    System.out.println(spSource + " | " + spTarget + " window: " + slidingWindow.getWindowIndex());
//                    System.out.println("edge: " + e.getWeight());
                    if (e == null) {
                        value += normalizedMin;
                    } else {
                        value += e.getWeight();
                    }
                }


            }
//            System.out.print("value: " + value + " -> ");
//            System.out.println("middle: " + vertexMiddle.toString() + ":" + vertexMiddle.getRealClassInside() + ":"
//                    + vertexMiddle.getRealClassOutside() + ":" + vertexMiddle.getRealClassNonTmh()
//                    + ":" + vertexMiddle.getRealClassTmh());

            int[] out = new int[4];
//            out[0] = (int) (vertexMiddle.getRealClassInside() / value);
//            out[1] = (int) (vertexMiddle.getRealClassOutside() / value);
            out[2] = (int) (vertexMiddle.getRealClassNonTmh() / value);
            out[3] = (int) (vertexMiddle.getRealClassTmh() / value);
            int pos = -1;
            int max = Integer.MIN_VALUE;
            for (int i = 0; i < out.length; i++) {
                if (out[i] > max) {
                    max = out[i];
                    pos = i;
                }
            }
            Result tmp = null;
            if (pos == 0) {
                tmp = Result.INSIDE;
            } else if (pos == 1) {
                tmp = Result.OUTSIDE;
            } else if (pos == 2) {
                tmp = Result.NON_TMH;
            } else if (pos == 3) {
                tmp = Result.TMH;
            }
            pred.add(tmp);
//            Edge eIn = wintermute.getEdge(vertexMiddle, INSIDE);
//             if (eIn==null) System.out.println("eIn=null");
//            Edge eOut = wintermute.getEdge(vertexMiddle, OUTSIDE);
//            if (eOut==null) System.out.println("eOut=null");
//            Edge eNon = wintermute.getEdge(vertexMiddle, NON_TMH);
//            if (eNon == null) {
//                System.out.println("eNon=null");
//            }
//            Edge eTmh = wintermute.getEdge(vertexMiddle, TMH);
//            if (eTmh == null) {
//                System.out.println("eTmh=null");
//            }


//            System.out.println("\tREAL: " + spMiddle.getRealClass());
//            System.out.println("\tPRED: " + tmp);
////            System.out.println("\tINSIDE: " + ((int) (vertexMiddle.getRealClassInside() / value))+" -> edge: w:"+eIn.getWeight()+":"+eIn.getOverInside()+":"+eIn.getOverOutside());
////            System.out.println("\tOUTSIDE: " + ((int) (vertexMiddle.getRealClassOutside() / value))+" -> edge: w:"+eOut.getWeight()+":"+eOut.getOverInside()+":"+eOut.getOverOutside());
//            if (eNon == null) {
//                System.out.println("\tNON_TMH: IGNORED -> NULL");
//            } else {
//                System.out.println("\tNON_TMH: " + ((int) (vertexMiddle.getRealClassNonTmh() / value)) + " -> edge: w:" + eNon.getWeight() + ":" + eNon.getOverInside() + ":" + eNon.getOverOutside());
//            }
//            if (eTmh == null) {
//                System.out.println("\tTMH: IGNORED -> NULL");
//            } else {
//                System.out.println("\tTMH: " + ((int) (vertexMiddle.getRealClassTmh() / value)) + " -> edge: w:" + eTmh.getWeight() + ":" + eTmh.getOverInside() + ":" + eTmh.getOverOutside());
//            }
            //link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
//            logger.trace("SequencePosition: middle " + spMiddle);
//            checkEdge(vertexMiddle, spMiddle, null, null, true);

        }

        //if edge does not exist -> weight = 0
        predictions = pred.toArray(predictions);
//        System.out.print("REAL: ");
//        for (SequencePosition sequencePosition : sequence.getSequence()) {
//            System.out.print(sequencePosition.getRealClass()+" ");
//        }
//        System.out.print("\nPRED: ");
//        for (Result result : predictions) {
//            System.out.print(result+" ");
//        }
        int cTp = 0;
        int cFp = 0;
        for (int i = 0; i < predictions.length; i++) {
            Result real = sequence.getSequence()[i].getRealClass();
            Result predi = predictions[i];
            if (real == Result.INSIDE || real == Result.OUTSIDE) {
                real = Result.NON_TMH;
            }
            if (real == predi) {
                cTp++;
            } else {
                cFp++;
            }
        }


//        System.out.println("ALL: " + predictions.length);
//        System.out.println("TRUE POSITIVE: " + cTp);
//        System.out.println("FALSE POSITIVE: " + cFp);
//        System.out.println("RATIO: " + ((double) cTp / (double) (cTp + cFp)));
        return new GenericPrediction(sequence, predictions);
    }

    @Override
    public void train(Sequence[] trainingCases) {
        if (trained) {
            throw new VerifyError("Model can not be overtrained! Create new empty Instance of markov!");
        }
        addVertices();
        long start = System.currentTimeMillis();
        logger.info("training " + trainingCases.length + " sequences");
        checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());
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
                    Vertex vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp + ":" + i);

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
                        Vertex vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp + ":" + (i + 1));

                        //link the source and target vertices
                        checkEdge(vertexSource, spSource, vertexTarget, spTarget, false);
                    }
                }
                //link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
                logger.trace("SequencePosition: middle " + spMiddle);
                checkEdge(vertexMiddle, spMiddle, null, null, true);
            }
        }
        trained = true;
        long end = System.currentTimeMillis();
        logger.info("-> " + wintermute.edgeSet().size() + " edges in " + (end - start) + " ms");
        norm = new Normalizer(wintermute);
        norm.normalize();
        normalizedMin = norm.getNormalizedMin();
    }

    @Override
    public void save(File model) throws Exception {
        if (!trained) {
            throw new VerifyError("Can not save an empty model! Train it before!");
        }
        long start = System.currentTimeMillis();

        logger.info("saving " + model.getAbsolutePath() + " (v: " + wintermute.vertexSet().size()
                + " | e: " + wintermute.edgeSet().size() + ")");
        BufferedWriter bw = new BufferedWriter(new FileWriter(model));
        GraphMLExporter g = new GraphMLExporter(new MarkovVertexNameProvider(), null, new MarkovEdgeNameProvider(), null);
        g.export(bw, wintermute);

        //verify begin
//        edge.getWeight()+":"+edge.getOverInside()+":"+edge.getOverOutside();
        bw.write("<!-- vertex=aa(enum):sse(enum):hp(Double):windowPos(int):inside(int):outside(int):nontmh(int):tmh(int) -->\n");
        bw.write("<!-- edge_id=weight(double):inside(int):outside(int) | edge_source/target vertex=@vertex -->\n");
        bw.write("<!-- normalizedMin:" + normalizedMin + " -->");
        bw.write("<!-- ");
        bw.write("wintermute:" + ((double) wintermute.edgeSet().size() / (double) wintermute.vertexSet().size()) + ":");
        bw.write(" -->");
        //verify end

        bw.flush();
        bw.close();
        long end = System.currentTimeMillis();
        logger.info("-> in " + (end - start) + " ms (" + (model.length() / 1024) + " kb)");
    }

    @Override
    public void load(File model) throws Exception {
        if (trained) {
            throw new VerifyError("Model can not be overloaded! Create new emtpy Instance of markov!");
        }

        long start = System.currentTimeMillis();
        logger.info("reading " + model.getAbsolutePath() + " (" + (model.length() / 1024) + " kb)");

        GraphXmlHandler graphXmlHandler = new GraphXmlHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setXIncludeAware(true);

        SAXParser parser = factory.newSAXParser();
        parser.parse(model, graphXmlHandler);

        logger.info("adding " + graphXmlHandler.getListVertex().size() + " vertices and " + graphXmlHandler.getListEdge().size() + " edges");
        for (String edgeConfig : graphXmlHandler.getListEdge()) {
            String[] parts = edgeConfig.split(";"); //source;target;weight;overInside;overOutside

            //source
            String[] src = parts[0].split(":"); //source=aa:sse:hp:wp:inside:outside
            String aa = src[0].intern();
            String sse = src[1].intern();
            double hp = Double.parseDouble(src[2].intern());
            int wp = Integer.parseInt(src[3].intern());
            int inside = Integer.parseInt(src[4].intern());
            int outside = Integer.parseInt(src[5].intern());
            int nonTmh = Integer.parseInt(src[6].intern());
            int tmh = Integer.parseInt(src[7].intern());
            Vertex source = new Vertex(aa, sse, hp, wp);
            source.setRealClassInside(inside);
            source.setRealClassOutside(outside);
            source.setRealClassNonTmh(nonTmh);
            source.setRealClassTmh(tmh);


            //target
            String[] trg = parts[1].split(":"); //target=aa:sse:hp:wp:inside:outside
            aa = trg[0].intern();
            sse = trg[1].intern();
            hp = Double.parseDouble(trg[2].intern());
            wp = Integer.parseInt(trg[3].intern());
            inside = Integer.parseInt(trg[4].intern());
            outside = Integer.parseInt(trg[5].intern());
            nonTmh = Integer.parseInt(trg[6].intern());
            tmh = Integer.parseInt(trg[7].intern());
            Vertex target = new Vertex(aa, sse, hp, wp);
            target.setRealClassInside(inside);
            target.setRealClassOutside(outside);
            target.setRealClassNonTmh(nonTmh);
            target.setRealClassTmh(tmh);


            //add vertices, edge
            if (!wintermute.containsVertex(source)) {
                wintermute.addVertex(source);
            }
            if (!wintermute.containsVertex(target)) {
                wintermute.addVertex(target);
            }
            Edge edge = wintermute.addEdge(source, target);

            //split id
            String[] id = parts[2].split(":");

            //weight
            double weight = Double.parseDouble(id[0]);
            wintermute.setEdgeWeight(edge, weight);

            //overInside
            int overInside = Integer.parseInt(id[1]);
            edge.setOverInside(overInside);

            //overOutside
            int overOutside = Integer.parseInt(id[2]);
            edge.setOverOutside(overOutside);
        }

        //missing vertices, which have no edges
        for (String vertex : graphXmlHandler.getListVertex()) {
            String[] parts = vertex.split(":");
            String aa = parts[0].intern();
            String sse = parts[1].intern();
            Double hp = Double.valueOf(parts[2].intern());
            int wp = Integer.valueOf(parts[3].intern());
            Vertex tmp = new Vertex(aa, sse, hp, wp);
            if (!wintermute.containsVertex(tmp)) {
                wintermute.addVertex(tmp);
            }
        }

        //verify start
        String shc = tail(model);
        if (shc.startsWith("<!-- ")) {
            String[] split = shc.split(" ")[1].split(":");
            if (split[0].equals("wintermute")) {
                double old = Double.parseDouble(split[1]);
                double act = ((double) wintermute.edgeSet().size() / (double) wintermute.vertexSet().size());
                if (old == act) {
                    logger.info("model is OK and not corrupted");
                } else {
                    throw new VerifyError("Model is corrupted and can not be read! Export new model!"
                            + "\nvertexSet: " + wintermute.vertexSet().size() + " | edgeSet: " + wintermute.edgeSet().size()
                            + "\nACTUAL (new): " + act
                            + "\nSAVED (old): " + old);
                }
            }
            //verify end

            trained = true;
            long end = System.currentTimeMillis();
            logger.info("-> in " + (end - start) + " ms");

        }
    }

    private double round(double value) {
        double result = value * hpRoundingValue;
        result = Math.round(result);
        result = result / hpRoundingValue;
        return result;
    }

    private void checkEdge(Vertex source, SequencePosition spSource, Vertex target, SequencePosition spTarget, boolean middle) {
        boolean inside = false;
        boolean outside = false;
        if (middle) {
            Result result = spSource.getRealClass();
            if (result.equals(Result.TMH)) {
                target = TMH;
            } else if (result.equals(Result.NON_TMH)) {
                target = NON_TMH;
            } else if (result.equals(Result.INSIDE)) {
                inside = true;
//                target = INSIDE;
                target = NON_TMH;
            } else if (result.equals(Result.OUTSIDE)) {
                outside = true;
//                target = OUTSIDE;
                target = NON_TMH;
            } else {
                logger.fatal("WARNING: result '" + result + "' can not be mapped to a vertex");
            }
        }
        Edge edge = wintermute.getEdge(source, target);
        if (edge == null) {
            if (!wintermute.containsVertex(source)) {
                logger.fatal("WARNING: vertex source NOT contained: " + source
                        + "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
            }
            if (!wintermute.containsVertex(target)) {
                logger.fatal("WARNING: vertex target NOT contained: " + target
                        + "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
            }
            edge = wintermute.addEdge(source, target);
        } else {
            wintermute.setEdgeWeight(edge, (wintermute.getEdgeWeight(edge) + 1));
        }
        if (middle) {
            //FIXME: windows überlappen sich, nicht alles xmal zählen
            //edge labeling
            if (inside) {
                edge.setOverInside(edge.getOverInside() + 1);
            } else if (outside) {
                edge.setOverOutside(edge.getOverOutside() + 1);
            }
        } else {
            source = wintermute.getEdgeSource(edge);
            addToVertexClassCounter(source, spSource);
            target = wintermute.getEdgeTarget(edge);
            addToVertexClassCounter(target, spTarget);
        }
    }

    private void addToVertexClassCounter(Vertex v, SequencePosition sp) {
        if (sp.getRealClass().equals(Result.INSIDE)) {
            v.setRealClassInside(v.getRealClassInside() + 1);
//            if (v.getRealClassInside() != 1) {
//                logger.debug(v.getRealClassInside());
//            }
        } else if (sp.getRealClass().equals(Result.OUTSIDE)) {
            v.setRealClassOutside(v.getRealClassOutside() + 1);
//            if (v.getRealClassOutside() != 1) {
//                logger.debug(v.getRealClassOutside());
//            }
        } else if (sp.getRealClass().equals(Result.NON_TMH)) {
            v.setRealClassNonTmh(v.getRealClassNonTmh() + 1);
//            if (v.getRealClassNonTmh() != 1) {
//                logger.debug(v.getRealClassNonTmh());
//            }
        } else if (sp.getRealClass().equals(Result.TMH)) {
            v.setRealClassTmh(v.getRealClassTmh() + 1);
//            if (v.getRealClassTmh() != 1) {
//                logger.debug(v.getRealClassTmh());
//            }
        } else {
            logger.fatal("WARNING: '" + sp.getRealClass() + "' can not be mapped to a RealClassCounter");
        }
    }

    /**
     * checks hpscale for being the same as trained with
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

    public void setMappingContValuesToNodes(double range) {
        hpSteppingValue = range;
        hpRoundingValue = 1 / range;
    }

    public Graph getGraph() {
        return wintermute;
    }
}