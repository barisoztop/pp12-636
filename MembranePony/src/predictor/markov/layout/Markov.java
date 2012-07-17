package predictor.markov.layout;

import data.Constants;
import interfaces.Prediction;
import interfaces.Predictor;
import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;
import predictor.markov.graph.Edge;
import predictor.markov.graph.GraphXmlHandler;
import predictor.markov.graph.MarkovDirectedWeightedGraph;
import predictor.markov.graph.MarkovEdgeNameProvider;
import predictor.markov.graph.MarkovVertexNameProvider;
import predictor.markov.graph.Vertex;
import predictor.markov.normalizer.Normalizer;

/**
 *
 * @author greil
 */
public abstract class Markov implements Predictor {

	protected final static Logger logger = Logger.getLogger(Markov.class);
	protected Map<String, Vertex> mapVertex;
	protected MarkovDirectedWeightedGraph wintermute;
	protected Normalizer norm;
	protected double hpSteppingValue = 0.1d;
	protected double hpRoundingValue = 10d;
	protected int hpscaleUsed = -1;
	protected static final double HP_MIN = -5.0d;
	protected static final double HP_MAX = 6.0d;
	protected boolean trained = false;
	protected final int middle = (Constants.WINDOW_LENGTH / 2);
	protected boolean windowNew = false;
	protected double normalizedMin = 0.0001d;
//	protected double[] windowVertexWeight = new double[]{
//				1,
//		2,
//		3,
//		4,
//		5,
//		6,
//		7,
//		8,
//		9,
//		10,
//		10,
//		9,
//		8,
//		7,
//		6,
//		5,
//		4,
//		3,
//		2,
//		1,
//	};

	protected enum SpecialVertex {

		TMH,
		NON_TMH,
		//        OUTSIDE,
		//        INSIDE,
		GECONNYSE,
		NULL
	};
	protected final Vertex TMH = new Vertex(SpecialVertex.TMH, SpecialVertex.NULL, Double.NaN);
	protected final Vertex NON_TMH = new Vertex(SpecialVertex.NON_TMH, SpecialVertex.NULL, Double.NaN);
//    protected final Vertex OUTSIDE = new Vertex(SpecialVertex.OUTSIDE, SpecialVertex.NULL, Double.NaN);
//    protected final Vertex INSIDE = new Vertex(SpecialVertex.INSIDE, SpecialVertex.NULL, Double.NaN);
	protected final Vertex GECONNYSE = new Vertex(SpecialVertex.GECONNYSE, SpecialVertex.NULL, Double.NaN);

	//methods from Predictor interface
	@Override
	public abstract Prediction predict(Sequence sequence);

	@Override
	public abstract void train(Sequence[] trainingCases);

	protected final void pruneNotUsedVertices() {
		long start = System.currentTimeMillis();
		logger.info("pruning vertices with no edges");
		List<String> toBeRemoved = new ArrayList<String>();
		int counter = 0;
		for (Vertex vertex : mapVertex.values()) {
			if (wintermute.inDegreeOf(vertex) == 0 && wintermute.outDegreeOf(vertex) == 0) {
				wintermute.removeVertex(vertex);
				toBeRemoved.add(vertex.toString());
				counter++;
			}
		}


		for (String string : toBeRemoved) {
			mapVertex.remove(string);
		}
		long end = System.currentTimeMillis();
		logger.info("-> " + counter + " vertices in " + (end - start) + " ms");
	}

	@Override
	public final void save(File model) throws Exception {
		long start = System.currentTimeMillis();
		if (!trained) {
			throw new VerifyError("Can not save an empty model! Train it before!");
		}

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
	public final void load(File model) throws Exception {
		long start = System.currentTimeMillis();
		if (trained) {
			throw new VerifyError("Model can not be overloaded! Create new emtpy Instance of markov!");
		}

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
			mapVertex.put(source.toString(), source);

			//target
			String[] trg = parts[1].split(":"); //target=aa:sse:hp
			aa = trg[0].intern();
			sse = trg[1].intern();
			hp = Double.parseDouble(trg[2].intern());
			Vertex target = new Vertex(aa, sse, hp);
			mapVertex.put(target.toString(), target);

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
				edge.setWeightComplete(weightComplete);
				edge.setWeightTmh(weightTmh);
				edge.setWeightNonTmh(weightNonTmh);
				//verify weight
				if ((weightComplete - (weightTmh + weightNonTmh)) >= 0.001d) {
					throw new VerifyError("Edge is corrupted and can not be set! Export new model!"
									+ "\nedge: " + edge);
				}
			}
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

		verify(model);

		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> in " + (end - start) + " ms");
	}
	//end of methods from Predictor interface

	protected abstract void addVertices();

	protected final double round(double value) {
		double result = value * hpRoundingValue;
		result = Math.round(result);
		result = result / hpRoundingValue;
		return result;
	}

	protected final Edge addEdge(Vertex source, SequencePosition spSource, Vertex target, SequencePosition spTarget, boolean middle) {
		if (!windowNew) {
			return null;
		}
		boolean sourceIsTmh = false;

		Result result = spSource.getRealClass();
		if (result.equals(Result.TMH)) {
			sourceIsTmh = true;
			if (middle) {
				target = TMH;
			}
		} else if (result.equals(Result.NON_TMH)) {
			sourceIsTmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.INSIDE)) {
			sourceIsTmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else if (result.equals(Result.OUTSIDE)) {
			sourceIsTmh = false;
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
			edge.setWeightComplete(1);
			if (sourceIsTmh) {
				edge.setWeightTmh(1);
			} else {
				edge.setWeightNonTmh(1);
			}
			logger.trace("EDGE:CREATED: " + edge);
		} else {
			edge.setWeightComplete(edge.getWeightComplete() + 1);
			if (sourceIsTmh) {
				edge.setWeightTmh(edge.getWeightTmh() + 1);
			} else {
				edge.setWeightNonTmh(edge.getWeightNonTmh() + 1);
			}
			logger.trace("EDGE:PUSHED: " + edge);
		}
		return edge;
	}

	protected final void verify(File model) throws Exception {
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

	/**
	 * checks hpscale for being the same as trained with
	 *
	 * @param scale
	 * @throws VerifyError if scale has changed within same instance of class
	 */
	protected final void checkScale(int scale) {
		if (hpscaleUsed == -1) {
			hpscaleUsed = scale;
		} else if (hpscaleUsed != scale) {
			throw new VerifyError("Hydrophobocity scale has changed! Create new Instance or use data with the same scale!");
		}
	}

	protected final String tail(File file) throws FileNotFoundException, IOException {
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

	public final void setMappingContValuesToNodes(double range) {
		hpSteppingValue = range;
		hpRoundingValue = 1 / range;
	}

	public final double getHpSteppingValue() {
		return hpSteppingValue;
	}

	public final MarkovDirectedWeightedGraph getGraph() {
		return wintermute;
	}

	protected final Vertex getVertexReference(String id) {
		if (mapVertex.containsKey(id)) {
			return mapVertex.get(id);
		} else {
			return null;
		}
	}

	public final String printRealClass(String prefix, List<Result> values) {
		StringBuilder sb = new StringBuilder(prefix);
		for (Result result : values) {
			if (result.equals(Result.TMH)) {
				sb.append("T");

			} else if (result.equals(Result.NON_TMH)) {
				sb.append(".");
			} else {
				sb.append("@");
			}
		}
		return sb.toString();
	}

	public final String printHPalgebraicSign(String prefix, Sequence seq) {
		StringBuilder sb = new StringBuilder(prefix);
		for (SequencePosition sp : seq.getSequence()) {
			if (sp.getHydrophobicity() > 0) {
				sb.append("+");
			} else {
				sb.append("-");
			}
		}
		return sb.toString();
	}

	public final String printHPvalues(String prefix, Sequence seq) {
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
		StringBuilder sb = new StringBuilder(prefix);
		for (SequencePosition sp : seq.getSequence()) {
			double hp = sp.getHydrophobicity();
			String out = nf.format(hp);
			if (out.contains("-")) {
				sb.append(out.substring(1, 2));
			} else {
				sb.append(out);
			}
		}
		return sb.toString();
	}
}