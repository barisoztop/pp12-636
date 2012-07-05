package markov.layout;

import markov.normalizer.Normalizer;
import data.AminoAcid;
import data.Constants;
import data.SSE;
import interfaces.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import markov.classificator.Classificator;
import markov.classificator.ClassificatorBayes;
import markov.classificator.ClassificatorModBayes;
import markov.classificator.ClassificatorRatio;
import markov.graph.*;
import markov.normalizer.NormalizerHighestWeighToOne;
import markov.normalizer.NormalizerSumOfWeightsToOne;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;

/**
 *
 * @author rgreil
 */
public final class Markov implements Predictor {

	private static final Logger logger = Logger.getLogger(Markov.class);
	private Map<String, Vertex> mapVertex;
	private final MarkovDirectedWeightedGraph<Vertex, Edge> wintermute;
	private Normalizer norm;
	private double hpSteppingValue = 0.1d;
	private double hpRoundingValue = 10d;
	private int hpscaleUsed = -1;
	private static final double HP_MIN = -5.0d;
	private static final double HP_MAX = 6.0d;
	private boolean trained = false;
	private final int middle = (Constants.WINDOW_LENGTH / 2);
	private boolean windowNew = false;
	private double normalizedMin = 0.000001d;
	private double[] windowVertexWeight = new double[]{
		10,
		9,
		8,
		7,
		6,
		5,
		4,
		3,
		2,
		1,
		1,
		2,
		3,
		4,
		5,
		6,
		7,
		8,
		9,
		10,};

	private enum SpecialVertex {

		TMH,
		NON_TMH,
		//        OUTSIDE,
		//        INSIDE,
		GECONNYSE,
		NULL
	};
	private final Vertex TMH = new Vertex(SpecialVertex.TMH, SpecialVertex.NULL, Double.NaN);
	private final Vertex NON_TMH = new Vertex(SpecialVertex.NON_TMH, SpecialVertex.NULL, Double.NaN);
//    private final Vertex OUTSIDE = new Vertex(SpecialVertex.OUTSIDE, SpecialVertex.NULL, Double.NaN);
//    private final Vertex INSIDE = new Vertex(SpecialVertex.INSIDE, SpecialVertex.NULL, Double.NaN);
	private final Vertex GECONNYSE = new Vertex(SpecialVertex.GECONNYSE, SpecialVertex.NULL, Double.NaN);

	public Markov() {
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new MarkovDirectedWeightedGraph<Vertex, Edge>(Edge.class);
	}

	protected void addVertices() {
		long start = System.currentTimeMillis();
		mapVertex = new HashMap<String, Vertex>();
		logger.info("creating vertices");
		Vertex[] vArray = new Vertex[]{TMH, NON_TMH, GECONNYSE};
		for (Vertex v : vArray) {
			wintermute.addVertex(v);
		}
		//create nodes and add them to the graph
		for (int sse = 0; sse < SSE.values().length; sse++) {
			//sse = the secondary structure of all available at SSE.values()
			for (int aa = 0; aa < AminoAcid.values().length; aa++) {
				//aa = the aminoacid of all available at AminoAcid.values()
				double value_hp = HP_MIN;
				while (value_hp < HP_MAX) {
					//hp = the hydrophobocity value from min to max
					Vertex tmp = new Vertex(AminoAcid.values()[aa], SSE.values()[sse], round(value_hp));
					logger.trace("created vertex: " + tmp);
					mapVertex.put(tmp.getAminoacid() + ":" + tmp.getSse() + ":" + tmp.getHydrophobocity(), tmp);
					wintermute.addVertex(tmp);
					value_hp += hpSteppingValue;
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.vertexSet().size() + " vertices in " + (end - start) + " ms");
	}

	@Override
	public Prediction predict(Sequence sequence) {
		if (!trained) {
			throw new VerifyError("Can not predict with an empty model! Train it before!");
		}
		checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());
		List<Result> pred = new ArrayList<Result>();
		Result[] predictions = new Result[1];

		int counterFalsePredicted = 0;
		for (SlidingWindow slidingWindow : sequence.getWindows()) {
			Vertex vertexMiddle = null;
			SequencePosition spMiddle = null;

			List<Edge> listWindowClonedEdges = new ArrayList<Edge>(Constants.WINDOW_LENGTH);

			for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
				SequencePosition spSource = slidingWindow.getSequence()[i];
				Vertex vertexSource = null;
				SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
				Vertex vertexTarget = null;

				//source
				if (spSource == null) {
//					continue;
				} else {
					String sourceAa = spSource.getAminoAcid().toString().intern();
					String sourceSse = spSource.getSecondaryStructure().toString().intern();
					Double sourceHp = round(spSource.getHydrophobicity());
					vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
				}

				if (i == middle) {
					vertexMiddle = vertexSource;
					spMiddle = spSource;
				}

				//target
				if (spTarget == null) {
//					continue;
				} else {
					String targetAa = spTarget.getAminoAcid().toString().intern();
					String targetSse = spTarget.getSecondaryStructure().toString().intern();
					Double targetHp = round(spTarget.getHydrophobicity());
					vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
				}

				Edge e = wintermute.getEdge(vertexSource, vertexTarget);
				if (e != null) {
					e = (Edge) e.clone();
				}
				listWindowClonedEdges.add(e);

			}

			{
//				// weight edges -> auslagern in weighting classes
//				for (int i = 0; i < listWindowClonedEdges.size(); i++) {
//					Edge e = listWindowClonedEdges.get(i);
//					if (e == null) {
//						continue;
//					}
//					e.setWeight(true, e.getWeight(true) * windowVertexWeight[i]);
//					e.setWeight(false, e.getWeight(false) * windowVertexWeight[i]);
//					e.setWeightComplete(e.getWeightTmh() + e.getWeightNonTmh());
//				}

				//classification


				Classificator crb = new ClassificatorBayes(listWindowClonedEdges);
				Classificator crmb = new ClassificatorModBayes(listWindowClonedEdges);
				Classificator crr = new ClassificatorRatio(listWindowClonedEdges);

				double weightTmh = crmb.getClassRateTmh() * crr.getClassRateTmh();
				double weightNonTmh = crmb.getClassRateNonTmh() * crr.getClassRateNonTmh();

//				//old & not so good, do not use!
//				Edge edgeTmh = wintermute.getEdge(vertexMiddle, TMH);
//				if (edgeTmh == null) {
////					weightTmh *= normalizedMin;
//					System.out.println("edgeTmh:NULL");
//				} else {
//					weightTmh *= edgeTmh.getWeightComplete();
//				}
//
//				Edge edgeNonTmh = wintermute.getEdge(vertexMiddle, NON_TMH);
//				if (edgeNonTmh == null) {
////					weightNonTmh *= normalizedMin;
//					System.out.println("edgeNonTmh:NULL");
//				} else {
//					weightNonTmh *= edgeNonTmh.getWeightComplete();
//				}
//				//fin & old and not so good

				//new -> schrott
//				Edge edgeTmh = wintermute.getEdge(vertexMiddle, TMH);
//				double weightTmh = crr.getClassRateTmh() / crmb.getClassRateTmh();
//				if (edgeTmh == null) {
////					weightTmh *= normalizedMin;
//				} else {
//					weightTmh = edgeTmh.getWeight()/weightTmh;
//				}
//
//				Edge edgeNonTmh = wintermute.getEdge(vertexMiddle, NON_TMH);
//				double weightNonTmh = crr.getClassRateNonTmh() / crmb.getClassRateNonTmh();
//				if (edgeNonTmh == null) {
////					weightNonTmh *= normalizedMin;
//				} else {
//					weightNonTmh = edgeNonTmh.getWeight()/weightTmh;
//				}
				//fin new

				Result predicted = null;
				if (weightTmh > weightNonTmh) {
					predicted = Result.TMH;
				} else if (weightTmh < weightNonTmh) {
					predicted = Result.NON_TMH;
				} else {
					logger.fatal("WARNING: probability for prediction of TMH (" + weightTmh + ") and NON_TMH (" + weightNonTmh + ") are equal. Prediction set to: " + Result.OUTSIDE);
					predicted = Result.OUTSIDE;
				}

				if (spMiddle.getRealClass() != predicted) {
					counterFalsePredicted++;
//
//					System.out.println(sequence.getId() + " -> " + vertexMiddle);
//					System.out.println("\tcrRatio:Tmh: " + crr.getClassRateTmh());
//					System.out.println("\tcrRatio:NonTmh: " + crr.getClassRateNonTmh());
//					System.out.println("\tcrRatio:Complete: " + crr.getClassRateComplete());
//					System.out.println("\tcrModBayes:Tmh: " + crmb.getClassRateTmh());
//					System.out.println("\tcrModBayes:NonTmh: " + crmb.getClassRateNonTmh());
//					System.out.println("\tcrModBayes:Complete: " + crmb.getClassRateComplete());
//					System.out.println("\tcrBayes:Tmh: " + crb.getClassRateTmh());
//					System.out.println("\tcrBayes:NonTmh: " + crb.getClassRateNonTmh());
//					System.out.println("\tcrBayes:Complete: " + crb.getClassRateComplete());
//					System.out.println("\t-> edge:Tmh: " + edgeTmh);
//					System.out.println("\t-> edge:NonTmh: " + edgeNonTmh);
//					System.out.println("\t--> probability");
//					System.out.println("\t--> TMH: " + weightTmh);
//					System.out.println("\t--> NON TMH: " + weightNonTmh);
//					System.out.println("\t---> RESULT");
//					System.out.println("\t---> REAL: " + spMiddle.getRealClass());
//					System.out.println("\t---> PRED: " + predicted);
				}
				//TODO: hier noch iwie die wkeit für TMH / NON_TMH in nem array mitspeichern
				pred.add(predicted);
			}
		}

		//TODO: hier nochmal son extra check reinzwiefeln, kA ob das was bringt
		//postprocessing?
		//tmh ist ja mindestens so 16-20 stellen lang, daher die ergebnisse angucken
		//n=nontmh | t=tmh
		//nnnnnnnnnnnnntnnttttttttntttttttttnnnnnnnnnntnnntntntnnnnnnntnnnnnnnnntttttttnttttttnntnttttttnn
		//alle ns rauswerfen die so zwischen ts sind, alle ts raushauen, wenn ihre position totaler müll ist
		//evtl die normale sequenz mit mss nochmal überarbeiten udn anhand davon die stellen gucken wo was sein sollte, bzw.
		//inwieweit das übereinander passt


		predictions = pred.toArray(predictions);
		if (counterFalsePredicted != 0) {
			logger.debug("FALSE PREDICTION: " + counterFalsePredicted + " (" + ((int) (100d / (double) sequence.length() * counterFalsePredicted)) + "%) (id: " + sequence.getId() + " -> length: " + sequence.length() + ")");
		}
		return new GenericPrediction(sequence, predictions);
	}

	@Override
	public void train(Sequence[] trainingCases) {
		if (trained) {
			throw new VerifyError("Model can not be overtrained! Create new empty Instance!");
		}
		addVertices();
		long start = System.currentTimeMillis();
		logger.info("training " + trainingCases.length + " sequences");
		checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());


		for (Sequence sequence : trainingCases) {
			if (!sequence.containsTransmembrane()) {
				continue;
			}
			for (SlidingWindow slidingWindow : sequence.getWindows()) {
				int check = slidingWindow.getWindowIndex() % (Constants.WINDOW_LENGTH - 1);
				if (check == 0) {
					windowNew = true;
				} else {
					windowNew = false;
				}
				logger.trace("slidingWindowIndex: " + slidingWindow.getWindowIndex() + " -> newWindow:" + windowNew + " (check value: " + check + ") --> " + Arrays.toString(slidingWindow.getSequence()));

				Vertex vertexMiddle = null;
				SequencePosition spMiddle = null;

				for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
					SequencePosition spSource = slidingWindow.getSequence()[i];
					Vertex vertexSource = null;
					SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
					Vertex vertexTarget = null;

					//source
					if (spSource == null) {
						continue;
					} else {
						String sourceAa = spSource.getAminoAcid().toString().intern();
						String sourceSse = spSource.getSecondaryStructure().toString().intern();
						Double sourceHp = round(spSource.getHydrophobicity());
						vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
					}

					if (i == middle) {
						//if source vertex == middle vertex
						vertexMiddle = vertexSource;
						spMiddle = spSource;
					}

					//target
					if (spTarget == null) {
						continue;
					} else {
						String targetAa = spTarget.getAminoAcid().toString().intern();
						String targetSse = spTarget.getSecondaryStructure().toString().intern();
						Double targetHp = round(spTarget.getHydrophobicity());
						vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
					}

					//link the source and target vertices
					checkEdge(vertexSource, spSource, vertexTarget, spTarget, false);

				}
				//link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				checkEdge(vertexMiddle, spMiddle, null, null, true);
			}
		}
		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.edgeSet().size() + " edges in " + (end - start) + " ms");
//		Normalizer nhwto = new NormalizerHighestWeighToOne(wintermute);
//		norm = new NormalizerSumOfWeightsToOne(wintermute);
//		norm.compute();


		//DEBUG
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("MARKOV_DEBUG.txt")));
//            for (Vertex vertex : wintermute.vertexSet()) {
//                bw.write(vertex.toString() + "\n");
//            }
//            bw.write("\n\n\n\n\n");
//            for (Edge edge : wintermute.edgeSet()) {
//                bw.write(edge.toString() + "\n");
//            }
//            bw.flush();
//            bw.close();
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(Markov.class.getName()).log(Level.SEVERE, null, ex);
//        }
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

		{
			//verify
			bw.write("<!-- vertex=aa(enum):sse(enum):hp(Double) -->\n");
			bw.write("<!-- edge_id=weight(double):weightTmh(double):weightNonTmh(double) | edge_source/target vertex=@vertex -->\n");
			bw.write("<!-- normalizedMin:" + normalizedMin + " -->\n");
			bw.write("<!-- wintermute:" + ((double) wintermute.edgeSet().size() / (double) wintermute.vertexSet().size()) + " -->");
		}

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
			String[] parts = edgeConfig.split(";"); //source;target;weight

			//source
			String[] src = parts[0].split(":"); //source=aa:sse:hp
			String aa = src[0].intern();
			String sse = src[1].intern();
			double hp = Double.parseDouble(src[2].intern());
			Vertex source = new Vertex(aa, sse, hp);

			//target
			String[] trg = parts[1].split(":"); //target=aa:sse:hp
			aa = trg[0].intern();
			sse = trg[1].intern();
			hp = Double.parseDouble(trg[2].intern());
			Vertex target = new Vertex(aa, sse, hp);

			//add vertices, edge
			if (!wintermute.containsVertex(source)) {
				wintermute.addVertex(source);
			}
			if (!wintermute.containsVertex(target)) {
				wintermute.addVertex(target);
			}
			Edge edge = wintermute.addEdge(source, target);

			//edge
			String[] edgeId = parts[2].split(":"); //id=weight:weightTmh:weightNonTmh

			double weightComplete = Double.parseDouble(edgeId[0]);
			double weightTmh = Double.parseDouble(edgeId[1]);
			double weightNonTmh = Double.parseDouble(edgeId[2]);
			{
				//verify weight
				if (weightComplete != (weightTmh + weightNonTmh)) {
					throw new VerifyError("Edge is corrupted and can not be set! Export new model!"
							+ "\nedge: " + edge);
				}
			}
			wintermute.setEdgeWeight(edge, weightComplete);
			edge.setWeightComplete(weightComplete);
			edge.setWeight(true, weightTmh);
			edge.setWeight(false, weightNonTmh);
		}

		//missing vertices, which have no edges
		for (String vertex : graphXmlHandler.getListVertex()) {
			String[] parts = vertex.split(":");
			String aa = parts[0].intern();
			String sse = parts[1].intern();
			Double hp = Double.valueOf(parts[2].intern());
			Vertex tmp = new Vertex(aa, sse, hp);
			if (!wintermute.containsVertex(tmp)) {
				wintermute.addVertex(tmp);
			}
		}

		{
			//verify
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
			}
		}

		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> in " + (end - start) + " ms");

	}

	protected double round(double value) {
		double result = value * hpRoundingValue;
		result = Math.round(result);
		result = result / hpRoundingValue;
		return result;
	}

	protected void checkEdge(Vertex source, SequencePosition spSource, Vertex target, SequencePosition spTarget, boolean middle) {
		if (!windowNew) {
			return;
		}
		boolean tmh = false;

		Result result = spSource.getRealClass();
		if (result.equals(Result.TMH)) {
			tmh = true;
			if (middle) {
				target = TMH;
			}
		} else if (result.equals(Result.NON_TMH)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.INSIDE)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.OUTSIDE)) {
			tmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else {
			logger.fatal("WARNING: result '" + result + "' can not be mapped to a vertex");
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
			wintermute.setEdgeWeight(edge, 1);
			edge.setWeight(tmh, 1);
			edge.setWeightComplete(1);
			logger.trace("EDGE:CREATED: " + edge);
		} else {
			wintermute.setEdgeWeight(edge, (wintermute.getEdgeWeight(edge) + 1));
			edge.setWeight(tmh, edge.getWeight(tmh) + 1);
			edge.setWeightComplete(edge.getWeightComplete() + 1);
			logger.trace("EDGE:PUSHED: " + edge);
		}
	}

	/**
	 * checks hpscale for being the same as trained with
	 *
	 * @param scale
	 * @throws VerifyError if scale has changed within same instance of class
	 */
	protected void checkScale(int scale) {
		if (hpscaleUsed == -1) {
			hpscaleUsed = scale;
		} else if (hpscaleUsed != scale) {
			throw new VerifyError("Hydrophobocity scale has changed! Create new Instance or use data with the same scale!");
		}
	}

	protected String tail(File file) throws FileNotFoundException, IOException {
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

	public double getHpSteppingValue() {
		return hpSteppingValue;
	}

	public MarkovDirectedWeightedGraph<Vertex, Edge> getGraph() {
		return wintermute;
	}

	public Vertex getVertexReference(String id) {
		if (mapVertex.containsKey(id)) {
			return mapVertex.get(id);
		} else {
			return null;
		}
	}
}
