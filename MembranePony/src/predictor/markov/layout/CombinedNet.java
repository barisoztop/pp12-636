package predictor.markov.layout;

import data.AminoAcid;
import data.Constants;
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
import org.apache.log4j.Logger;
import predictor.markov.classifier.Classifier;
import predictor.markov.classifier.ClassifierBayes;
import predictor.markov.classifier.ClassifierModBayes;
import predictor.markov.classifier.ClassifierRatio;
import predictor.markov.graph.Edge;
import predictor.markov.graph.GraphDirected;
import predictor.markov.graph.Vertex;

/*
 * @author rgreil
 */
public class CombinedNet extends Base {

	public CombinedNet() {
		logger = Logger.getLogger(CombinedNet.class);
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new GraphDirected();
		mapVertex = new HashMap<String, Vertex>();
	}

	@Override
	protected void addVertices() {
		long start = System.currentTimeMillis();
		logger.info("creating vertices");
		Vertex[] vArray = new Vertex[]{TMH, NON_TMH, GECONNYSE};
		for (Vertex v : vArray) {
			wintermute.addVertex(v);
		}
		//create nodes and add them to the graph
		for (AminoAcid aa : AminoAcid.values()) {
			//aa = the aminoacid of all available at AminoAcid.values()
			for (SSE sse : SSE.values()) {
				//sse = the secondary structure of all available at SSE.values()
				double value_hp = HP_MIN;
				while (value_hp < HP_MAX) {
					//hp = the hydrophobocity value from min to max
					Vertex tmp = new Vertex(aa, sse, round(value_hp));
					logger.trace("created vertex: " + tmp);
					mapVertex.put(tmp.toString(), tmp);
					wintermute.addVertex(tmp);
					value_hp += hpSteppingValue;
				}
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
		addVertices();
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
						Double targetHp = round(spTarget.getHydrophobicity());
						vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
					}

					//link the source and target vertices
					addEdge(vertexSource, spSource, vertexTarget, spTarget, false, -1);
				}
				//link the middle node to the RealClass (TMH, NON_TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				addEdge(vertexMiddle, spMiddle, null, null, true, Constants.WINDOW_MIDDLE_POSITION);

			}
		}
		trained = true;
		long end = System.currentTimeMillis();
		logger.info("-> " + wintermute.edgeSet().size() + " edges in " + (end - start) + " ms");

		pruneNotUsedVertices();
		addFinalMissingNullEdges();
	}

	@Override
	public Prediction predict(Sequence sequence) {
		if (!trained) {
			throw new VerifyError("Can not predict with an empty model! Train it before!");
		}
		checkScale(sequence.getSequence()[0].getHydrophobicityMatrix());
		List<Result> pred = new ArrayList<Result>();
		List<Result> real = new ArrayList<Result>();
		for (SequencePosition seas : sequence.getSequence()) {
			real.add(seas.getRealClass());
		}
		int counterFalsePredicted = 0;
		//debug end

		Result[] predictions = new Result[1];


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
					Double sourceHp = round(spSource.getHydrophobicity());
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
					Double targetHp = round(spTarget.getHydrophobicity());
					vertexTarget = mapVertex.get(targetAa + ":" + targetSse + ":" + targetHp);
				}

				Edge e = wintermute.getEdge(vertexSource, vertexTarget);
				if (e != null) {
					e = (Edge) e.clone();
				}
				listWindowClonedEdges.add(e);

			}


////				 weight edges -> auslagern in weighting classes
//				for (int i = 0; i < listWindowClonedEdges.size(); i++) {
//					Edge e = listWindowClonedEdges.get(i);
//					if (e == null) {
//						continue;
//					}
//					//majority vote
//					double weight = 1d/(windowVertexWeight[i]*windowVertexWeight[i]);
//
//					e.setWeight(true, e.getWeight(true) * weight);
//					e.setWeight(false, e.getWeight(false) * weight);
//					e.setWeightComplete(e.getWeightTmh() + e.getWeightNonTmh());
//				}

			//classification
			//tmh
			List<Edge> listWindowClonedEdgesTmh = new ArrayList(listWindowClonedEdges);
			listWindowClonedEdgesTmh.add((Edge) wintermute.getEdge(vertexMiddle, TMH).clone());

			//nonTmh
			List<Edge> listWindowClonedEdgesNonTmh = new ArrayList(listWindowClonedEdges);
			listWindowClonedEdgesNonTmh.add((Edge) wintermute.getEdge(vertexMiddle, NON_TMH).clone());

			Classifier crbTmh = new ClassifierBayes(listWindowClonedEdgesTmh);
			Classifier crbNonTmh = new ClassifierBayes(listWindowClonedEdgesNonTmh);
			Classifier cModBayesTmh = new ClassifierModBayes(listWindowClonedEdgesTmh);
			Classifier cModBayesNonTmh = new ClassifierModBayes(listWindowClonedEdgesNonTmh);
			Classifier cRatioTmh = new ClassifierRatio(listWindowClonedEdgesTmh);
			Classifier cRatioNonTmh = new ClassifierRatio(listWindowClonedEdgesNonTmh);

			double weightTmh = cModBayesTmh.getClassRateTmh() * cRatioTmh.getClassRateTmh();
			double weightNonTmh = cModBayesNonTmh.getClassRateNonTmh() * cRatioNonTmh.getClassRateNonTmh();

//			System.out.println("BAYES-TMH: COMPLETE: " + crbTmh.getClassRateComplete());
//			System.out.println("BAYES-TMH: TMH: " + crbTmh.getClassRateTmh());
//			System.out.println("BAYES-TMH: NON_TMH: " + crbTmh.getClassRateNonTmh());
//			System.out.println("BAYES-NON_TMH: COMPLETE: " + crbNonTmh.getClassRateComplete());
//			System.out.println("BAYES-NON_TMH: TMH: " + crbNonTmh.getClassRateTmh());
//			System.out.println("BAYES-NON_TMH: NON_TMH: " + crbNonTmh.getClassRateNonTmh());

//			System.out.println("TMH");
//			System.out.println("weightTmh: "+weightTmh);
//			System.out.println("cModBayesTmh.getClassRateTmh(): "+cModBayesTmh.getClassRateTmh());
//			System.out.println("cRatioTmh.getClassRateTmh(): "+cRatioTmh.getClassRateTmh());
//			for (Edge edge : listWindowClonedEdgesTmh) {
//				System.out.println("\t"+edge);
//			}
//
//			System.out.println("NON_TMH");
//			System.out.println("weightNonTmh: "+weightNonTmh);
//			System.out.println("cModBayesTmh.getClassRateNonTmh(): "+cModBayesNonTmh.getClassRateNonTmh());
//			System.out.println("cRatioNonTmh.getClassRateNonTmh(): "+cRatioNonTmh.getClassRateNonTmh());
//			for (Edge edge : listWindowClonedEdgesNonTmh) {
//				System.out.println("\t"+edge);
//			}




//				//old & not so good, do not use!
//			Edge edgeTmh = wintermute.getEdge(vertexMiddle, TMH);
//			weightTmh *= edgeTmh.getWeightComplete();
//
//			Edge edgeNonTmh = wintermute.getEdge(vertexMiddle, NON_TMH);
//			weightNonTmh *= edgeNonTmh.getWeightComplete();
//				//fin & old and not so good


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
			pred.add(predicted);

		}

//		System.out.println(printRealClass("REAL: \t", real));
//		System.out.println(printRealClass("PRED: \t", pred));
//		System.out.println(printHPalgebraicSign("HPas: \t", sequence));
//		System.out.println(printHPvalues("HPval:\t", sequence));

		predictions = pred.toArray(predictions);
		if (counterFalsePredicted != 0) {
			logger.info("FALSE PREDICTION: " + counterFalsePredicted + " (" + ((int) (100d / (double) sequence.length() * counterFalsePredicted)) + "%) (id: " + sequence.getId() + " -> length: " + sequence.length() + ")");
		}
		return new GenericPrediction(sequence, predictions);
	}
}
