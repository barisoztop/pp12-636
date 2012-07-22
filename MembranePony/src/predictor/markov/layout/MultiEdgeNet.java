package predictor.markov.layout;

import data.AminoAcid;
import data.Constants;
import data.Hydrophobicity;
import data.SSE;
import interfaces.GenericPrediction;
import interfaces.Prediction;
import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;
import interfaces.SlidingWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import predictor.markov.classifier.Classifier;
import predictor.markov.classifier.ClassifierBayes;
import predictor.markov.classifier.ClassifierModBayes;
import predictor.markov.classifier.ClassifierRatio;
import predictor.markov.graph.Edge;
import predictor.markov.graph.GraphMultiDirected;
import predictor.markov.graph.Vertex;

/**
 *
 * @author rgreil
 */
public class MultiEdgeNet extends Base {

	private GraphMultiDirected fallback;

	public MultiEdgeNet() {
		logger = Logger.getLogger(MultiEdgeNet.class);
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new GraphMultiDirected();
		fallback = new GraphMultiDirected();

		mapVertex = new HashMap<String, Vertex>();
	}

	@Override
	protected void addVertices() {
		long start = System.currentTimeMillis();
		logger.info("creating vertices");
		Vertex[] vArray = new Vertex[]{TMH, NON_TMH};
		for (Vertex v : vArray) {
			wintermute.addVertex(v);
			mapVertex.put(v.toString(), v);
		}
		//create nodes and add them to the graph
		for (AminoAcid aa : AminoAcid.values()) {
			//aa = the aminoacid of all available at AminoAcid.values()
			for (SSE sse : SSE.values()) {
				//sse = the secondary structure of all available at SSE.values()
				Vertex tmp = new Vertex(aa, sse, Hydrophobicity.get(aa, hpscaleUsed));
				logger.trace("created vertex: " + tmp);
				mapVertex.put(tmp.toString(), tmp);
				wintermute.addVertex(tmp);
			}
		}
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.vertexSet().size() + " vertices in " + (end - start) + " ms");
	}

	@Override
	public void train(Sequence[] trainingCases) {
		long start = System.currentTimeMillis();
		if (trained) {
			throw new VerifyError("Model can not be overtrained! Create new empty Instance!");
		}
		checkScale(trainingCases[0].getSequence()[0].getHydrophobicityMatrix());
		addVertices();
		logger.info("training " + trainingCases.length + " sequences");



		for (Sequence sequence : trainingCases) {
			if (!sequence.containsTransmembrane()) {
				continue;
			}
			for (SlidingWindow slidingWindow : sequence.getWindows()) {
				int check = slidingWindow.getWindowIndex() % (Constants.WINDOW_LENGTH - 1);

//				if (check == 0) {
				windowNew = true;
//				} else {
//					windowNew = false;
//				}
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
//						Double sourceHp = round(spSource.getHydrophobicity());
						Double sourceHp = spSource.getHydrophobicity();
						vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
					}

					if (i == Constants.WINDOW_MIDDLE_POSITION) {
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
//						Double targetHp = round(spTarget.getHydrophobicity());
						Double targetHp = spTarget.getHydrophobicity();
						vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
					}

					//link the source and target vertices
					addEdge(vertexSource, spSource, vertexTarget, spTarget, false, i);
				}
				//link the middle node to the RealClass (TMH, NON_TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				addEdge(vertexMiddle, spMiddle, null, null, true, Constants.WINDOW_MIDDLE_POSITION);

			}
		}
		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.edgeSet().size() + " edges in " + (end - start) + " ms");

//		pruneVerticesWithoutEdges();
		addFinalMissingNullEdges();
	}

	@Override
	public Prediction predict(Sequence sequence) {
		if (!trained) {
			throw new VerifyError("Can not predict with an empty model! Train it before!");
		}
		checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());
		List<Result> prediction = new ArrayList<Result>();

//		int counterFalsePredicted = 0;
		//debug end

		for (SlidingWindow slidingWindow : sequence.getWindows()) {
			//possible debug
			Vertex vertexMiddle = null;
			SequencePosition spMiddle = null;
			//possible debug end

			List<Edge> listWindowClonedEdges = new ArrayList<Edge>(Constants.WINDOW_LENGTH);

			for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
				SequencePosition spSource = slidingWindow.getSequence()[i];
				Vertex vertexSource;
				SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
				Vertex vertexTarget;

				//source
				if (spSource == null) {
					//deactivate, if using position based window weighting
					continue;
				} else {
					String sourceAa = spSource.getAminoAcid().toString().intern();
					String sourceSse = spSource.getSecondaryStructure().toString().intern();
//					Double sourceHp = round(spSource.getHydrophobicity());
					Double sourceHp = spSource.getHydrophobicity();
					vertexSource = mapVertex.get(sourceAa + ":" + sourceSse + ":" + sourceHp);
				}

				if (i == Constants.WINDOW_MIDDLE_POSITION) {
					vertexMiddle = vertexSource;
					spMiddle = spSource;
				}

				//target
				if (spTarget == null) {
					//deactivate, if using position based window weighting
					continue;
				} else {
					String targetAa = spTarget.getAminoAcid().toString().intern();
					String targetSse = spTarget.getSecondaryStructure().toString().intern();
//					Double targetHp = round(spTarget.getHydrophobicity());
					Double targetHp = spTarget.getHydrophobicity();
					vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
				}

				Edge e = getEdgeOfWindowPos(wintermute, vertexSource, vertexTarget, i);
				if (e == null) {
					//fallback mechanism for null-edges
					e = getEdgeOfWindowPos(fallback, vertexSource, vertexTarget, i);
					if (e == null) {
						fallback.addVertex(vertexSource);
						fallback.addVertex(vertexTarget);
						Set<Edge> outgoingEdgesOf = wintermute.outgoingEdgesOf(vertexSource);
//						Set<Edge> outgoingEdgesOf = wintermute.incomingEdgesOf(vertexTarget);
						double wComplete = 0;
						double wTmh = 0;
						double wNonTmh = 0;
						for (Edge edgeOutgoing : outgoingEdgesOf) {
							if (edgeOutgoing.getTarget() == TMH || edgeOutgoing.getTarget() == NON_TMH) {
								continue;
							} else {
								wComplete += edgeOutgoing.getWeightComplete();
								wTmh += edgeOutgoing.getWeightTmh();
								wNonTmh += edgeOutgoing.getWeightNonTmh();
							}
						}
						e = fallback.addEdge(vertexSource, vertexTarget);
						e.setWindowPos(i);
						e.setWeightComplete(wComplete / (double) outgoingEdgesOf.size());
						e.setWeightTmh(wTmh / (double) outgoingEdgesOf.size());
						e.setWeightNonTmh(wNonTmh / (double) outgoingEdgesOf.size());
					}
				}
				listWindowClonedEdges.add(e);

			}

			//classification
			//tmh
			List<Edge> listWindowClonedEdgesTmh = new ArrayList(listWindowClonedEdges);
			listWindowClonedEdgesTmh.add(wintermute.getEdge(vertexMiddle, TMH));

			//nonTmh
			List<Edge> listWindowClonedEdgesNonTmh = new ArrayList(listWindowClonedEdges);
			listWindowClonedEdgesNonTmh.add(wintermute.getEdge(vertexMiddle, NON_TMH));

//			Classifier crbTmh = new ClassifierBayes(listWindowClonedEdgesTmh);
//			Classifier crbNonTmh = new ClassifierBayes(listWindowClonedEdgesNonTmh);
			Classifier cModBayesTmh = new ClassifierModBayes(listWindowClonedEdgesTmh);
			Classifier cModBayesNonTmh = new ClassifierModBayes(listWindowClonedEdgesNonTmh);
			Classifier cRatioTmh = new ClassifierRatio(listWindowClonedEdgesTmh);
			Classifier cRatioNonTmh = new ClassifierRatio(listWindowClonedEdgesNonTmh);

			double weightTmh = cModBayesTmh.getClassRateTmh() * cRatioTmh.getClassRateTmh();
			double weightNonTmh = cModBayesNonTmh.getClassRateNonTmh() * cRatioNonTmh.getClassRateNonTmh();

			Result predicted;
			if (weightTmh > weightNonTmh) {
				predicted = Result.TMH;
			} else {
//				if (weightTmh < weightNonTmh) {
				predicted = Result.NON_TMH;
			}
			prediction.add(predicted);
		}
//		if (counterFalsePredicted != 0) {
//			logger.trace("ID: " + sequence.getId() + " -> length: " + sequence.length() + " wrong: " + counterFalsePredicted + " (" + ((int) (100d / (double) sequence.length() * counterFalsePredicted)) + "%)");
//		}
		return new GenericPrediction(sequence, prediction.toArray(new Result[]{}));
	}
}
