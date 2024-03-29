package predictor.markov.layout;

import data.Constants;
import data.SSE;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.graph.AbstractBaseGraph;
import predictor.markov.graph.Edge;
import predictor.markov.graph.GraphXmlHandler;
import predictor.markov.graph.GraphDirected;
import predictor.markov.graph.EdgeNameProviderBase;
import predictor.markov.graph.VertexNameProviderBase;
import predictor.markov.graph.Vertex;
import predictor.markov.normalizer.Normalizer;

/**
 *
 * @author greil
 */
public abstract class Base implements Predictor {

	protected static Logger logger = Logger.getLogger(Base.class);
	protected Map<String, Vertex> mapVertex;
	protected AbstractBaseGraph<Vertex, Edge> wintermute;
	protected double hpSteppingValue = 0.1d;
	protected double hpRoundingValue = 10d;
	protected int hpscaleUsed = -1;
	protected static final double HP_MIN = -5.0d;
	protected static final double HP_MAX = 6.0d;
	protected boolean trained = false;
	protected boolean windowNew = false;

	protected enum SpecialVertex {

		FINAL_TMH,
		FINAL_NON_TMH,
		FINAL_GECONNYSE,
		NULL
	};
	protected final Vertex TMH = new Vertex(SpecialVertex.FINAL_TMH, SpecialVertex.NULL, Double.NaN);
	protected final Vertex NON_TMH = new Vertex(SpecialVertex.FINAL_NON_TMH, SpecialVertex.NULL, Double.NaN);
	protected final Vertex GECONNYSE = new Vertex(SpecialVertex.FINAL_GECONNYSE, SpecialVertex.NULL, Double.NaN);

	//methods from Predictor interface
	@Override
	public abstract Prediction predict(Sequence sequence);

	@Override
	public abstract void train(Sequence[] trainingCases);

	@Override
	public final void save(File model) throws Exception {
		long start = System.currentTimeMillis();
		if (!trained) {
			throw new VerifyError("Can not save an empty model! Train it before!");
		}

		logger.info("saving " + model.getAbsolutePath()
						+ " ("
						+ "v: " + wintermute.vertexSet().size()
						+ " | "
						+ "e: " + wintermute.edgeSet().size()
						+ ")");
		BufferedWriter bw = new BufferedWriter(new FileWriter(model));
		GraphMLExporter g = new GraphMLExporter(new VertexNameProviderBase(), null, new EdgeNameProviderBase(), null);
		g.export(bw, wintermute);

		{
			//verify
			bw.write("<!-- vertex=aa(enum):sse(enum):hp(Double) -->\n");
			bw.write("<!-- edge_id=weight(double):weightTmh(double):weightNonTmh(double):windowPos(int) | edge_source/target vertex=@vertex -->\n");
//			bw.write("<!-- normalizedMin:" + normalizedMin + " -->\n");
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
			String[] edgeId = parts[2].split(":"); //id=weight:weightTmh:weightNonTmh:windowPos
			double weightComplete = Double.parseDouble(edgeId[0]);
			double weightTmh = Double.parseDouble(edgeId[1]);
			double weightNonTmh = Double.parseDouble(edgeId[2]);
			int windowPos = Integer.parseInt(edgeId[3]);

			{
				edge.setWeightComplete(weightComplete);
				edge.setWeightTmh(weightTmh);
				edge.setWeightNonTmh(weightNonTmh);
				edge.setWindowPos(windowPos);
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

	protected final void pruneVerticesWithoutEdges() {
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

	protected final void addFinalMissingNullEdges() {
		for (Vertex vertex : mapVertex.values()) {
			Edge e = getEdgeOfWindowPos(wintermute, vertex, TMH, Constants.WINDOW_MIDDLE_POSITION);
			if (e == null) {
				Edge tmh = wintermute.addEdge(vertex, TMH);
				tmh.setWeightComplete(1);
				tmh.setWeightTmh(1);
				tmh.setWindowPos(Constants.WINDOW_MIDDLE_POSITION);
			}

			e = getEdgeOfWindowPos(wintermute, vertex, NON_TMH, Constants.WINDOW_MIDDLE_POSITION);
			if (e == null) {
				Edge nonTmh = wintermute.addEdge(vertex, NON_TMH);
				nonTmh.setWeightComplete(1);
				nonTmh.setWeightNonTmh(1);
				nonTmh.setWindowPos(Constants.WINDOW_MIDDLE_POSITION);
			}
		}
	}

	protected final Edge getEdgeOfWindowPos(AbstractBaseGraph<Vertex, Edge> graph, Vertex source, Vertex target, int windowPos) {
		Set<Edge> setAllEdges = graph.getAllEdges(source, target);
		if (setAllEdges == null) {
			return null;
		}
		List<Edge> listAllEdges = new ArrayList<Edge>(setAllEdges);
		for (Edge edge : listAllEdges) {
			if (edge.getWindowPos() == windowPos) {
				return edge;
			}
		}
		return null;
	}

	protected final double round(double value) {
		double result = value * hpRoundingValue;
		result = Math.round(result);
		result = result / hpRoundingValue;
		return result;
	}

	protected final Edge addEdge(Vertex source, SequencePosition spSource, Vertex target, SequencePosition spTarget, boolean middle, int windowPos) {
		if (!windowNew) {
			//overlapping window, ignore
			return null;
		}

		//check source for its realClass
		boolean sourceIsTmh = false;
		Result result = spSource.getRealClass();
		if (result.equals(Result.TMH)) {
			sourceIsTmh = true;
			if (middle) {
				target = TMH;
			}
		} else if (result.equals(Result.NON_TMH) || result.equals(Result.INSIDE) || result.equals(Result.OUTSIDE)) {
			sourceIsTmh = false;
			if (middle) {
				target = NON_TMH;
			}
		} else {
			logger.fatal("WARNING: result '" + result + "' can not be mapped to a vertex");
		}

		//check windowPos for being a multiEdge
		boolean multiEdge = false;
		if (windowPos != -1) {
			multiEdge = true;
		}

		//get edge
		Edge edge = getEdgeOfWindowPos(wintermute, source, target, windowPos);

		if (edge == null) {
			if (!wintermute.containsVertex(source)) {
				logger.fatal("WARNING: source NOT contained: " + source
								+ "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
			}
			if (!wintermute.containsVertex(target)) {
				logger.fatal("WARNING: target NOT contained: " + target
								+ "\nsource: " + source + " | spSource: " + spSource + " | target: " + target + " | middle: " + middle);
			}
			edge = wintermute.addEdge(source, target);
			edge.setWeightComplete(1);
			if (sourceIsTmh) {
				if (middle) {
					edge.setWeightTmh(2);
				} else {
					edge.setWeightTmh(1);
				}
			} else {
				if (middle) {
					edge.setWeightNonTmh(2);
				} else {
					edge.setWeightNonTmh(1);
				}
			}
			if (multiEdge) {
				edge.setWindowPos(windowPos);
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

	public final AbstractBaseGraph<Vertex, Edge> getGraph() {
		return wintermute;
	}

	protected final Vertex getVertexReference(String id) {
		if (mapVertex.containsKey(id)) {
			return mapVertex.get(id);
		} else {
			return null;
		}
	}

	public final String printAA(String prefix, Sequence seq) {
		StringBuilder sb = new StringBuilder(prefix);
		for (SequencePosition sp : seq.getSequence()) {
			sb.append(sp.getAminoAcid().toString());
		}
		return sb.toString();
	}

	public final String printSSE(String prefix, Sequence seq) {
		StringBuilder sb = new StringBuilder(prefix);
		for (SequencePosition sp : seq.getSequence()) {
			String sse = sp.getSecondaryStructure().toString().toLowerCase();
			if (sse.equals("helix")) {
				sb.append("H");
			} else if (sse.equals("coil")) {
				sb.append("c");
			} else {
				sb.append("_");
			}
		}
		return sb.toString();
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
