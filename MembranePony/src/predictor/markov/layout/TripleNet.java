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
import predictor.markov.classifier.Classifier;
import predictor.markov.classifier.ClassifierBayes;
import predictor.markov.graph.Edge;
import predictor.markov.graph.MarkovDirectedGraph;
import predictor.markov.graph.Vertex;
import predictor.markov.normalizer.Normalizer;
import predictor.markov.normalizer.NormalizerMarkov;

/**
 *
 * @author greil
 */
public class TripleNet extends Markov {

	public TripleNet() {
//		logger = Logger.getLogger(TripleNet.class);
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new MarkovDirectedGraph();
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
		Vertex tmp;
		//create nodes and add them to the graph
		for (AminoAcid aa : AminoAcid.values()) {
			//aa = the aminoacid of all available at AminoAcid.values()
			tmp = new Vertex(aa, SpecialVertex.NULL, Double.NaN);
			mapVertex.put(tmp.toString(), tmp);
			wintermute.addVertex(tmp);
		}

		for (SSE sse : SSE.values()) {
			//sse = the secondary structure of all available at SSE.values()
			tmp = new Vertex(SpecialVertex.NULL, sse, Double.NaN);
			mapVertex.put(tmp.toString(), tmp);
			wintermute.addVertex(tmp);
		}

		double value_hp = HP_MIN;
		while (value_hp < HP_MAX) {
			//hp = the hydrophobocity value from min to max
			tmp = new Vertex(SpecialVertex.NULL, SpecialVertex.NULL, round(value_hp));
			logger.trace("created vertex: " + tmp);
			mapVertex.put(tmp.toString(), tmp);
			wintermute.addVertex(tmp);
			value_hp += hpSteppingValue;
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
			Vertex vertexMiddleAa = null;
			Vertex vertexMiddleSse = null;
			Vertex vertexMiddleHp = null;
			SequencePosition spMiddle = null;

			List<Edge> listWindowClonedEdgesAa = new ArrayList<Edge>(Constants.WINDOW_LENGTH);
			List<Edge> listWindowClonedEdgesSse = new ArrayList<Edge>(Constants.WINDOW_LENGTH);
			List<Edge> listWindowClonedEdgesHp = new ArrayList<Edge>(Constants.WINDOW_LENGTH);

			for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
				SequencePosition spSource = slidingWindow.getSequence()[i];
				Vertex vertexSourceAa;
				Vertex vertexSourceSse;
				Vertex vertexSourceHp;

				SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
				Vertex vertexTargetAa;
				Vertex vertexTargetSse;
				Vertex vertexTargetHp;

				//source
				if (spSource == null) {
					//deactivate, if using position based window weighting
					continue;
				} else {
					String sourceAa = spSource.getAminoAcid().toString().intern();
					vertexSourceAa = mapVertex.get(sourceAa + ":" + SpecialVertex.NULL + ":" + Double.NaN);

					String sourceSse = spSource.getSecondaryStructure().toString().intern();
					vertexSourceSse = mapVertex.get(SpecialVertex.NULL + ":" + sourceSse + ":" + Double.NaN);

					Double sourceHp = round(spSource.getHydrophobicity());
					vertexSourceHp = mapVertex.get(SpecialVertex.NULL + ":" + SpecialVertex.NULL + ":" + sourceHp);
				}

				if (i == middle) {
					//if source vertex == middle vertex
					vertexMiddleAa = vertexSourceAa;
					vertexMiddleSse = vertexSourceSse;
					vertexMiddleHp = vertexSourceHp;
					spMiddle = spSource;
				}

				//target
				if (spTarget == null) {
					//deactivate, if using position based window weighting
					continue;
				} else {
					String targetAa = spTarget.getAminoAcid().toString().intern();
					vertexTargetAa = mapVertex.get(targetAa + ":" + SpecialVertex.NULL + ":" + Double.NaN);

					String targetSse = spTarget.getSecondaryStructure().toString().intern();
					vertexTargetSse = mapVertex.get(SpecialVertex.NULL + ":" + targetSse + ":" + Double.NaN);

					Double targetHp = round(spTarget.getHydrophobicity());
					vertexTargetHp = mapVertex.get(SpecialVertex.NULL + ":" + SpecialVertex.NULL + ":" + targetHp);
				}

				Edge edgeAa = wintermute.getEdge(vertexSourceAa, vertexTargetAa);
				if (edgeAa != null) {
					edgeAa = (Edge) edgeAa.clone();
					listWindowClonedEdgesAa.add(edgeAa);
				}


				Edge edgeSse = wintermute.getEdge(vertexSourceSse, vertexTargetSse);
				if (edgeSse != null) {
					edgeSse = (Edge) edgeSse.clone();
					listWindowClonedEdgesSse.add(edgeSse);
				}


				Edge edgeHp = wintermute.getEdge(vertexSourceHp, vertexTargetHp);
				if (edgeHp != null) {
					edgeHp = (Edge) edgeHp.clone();
					listWindowClonedEdgesHp.add(edgeHp);
				}


			}

			{
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


				double aa = 0;
				double sse = 0;
				double hp = 0;
				{
					//meaning
					aa = 0;
					for (Edge edge : listWindowClonedEdgesAa) {
						aa += edge.getWeightComplete();
					}

					sse = 0;
					for (Edge edge : listWindowClonedEdgesSse) {
						sse += edge.getWeightComplete();
					}

					hp = 0;
					for (Edge edge : listWindowClonedEdgesHp) {
						hp += edge.getWeightComplete();
					}


//					System.out.println("MEAN:"
//							+ "\n\tAA: "+(aa/(double)listWindowClonedEdgesAa.size())
//							+"\n\tSSE: "+(sse/(double) listWindowClonedEdgesSse.size()
//							+"\n\tHP: "+(hp/(double)listWindowClonedEdgesHp.size())));

				}






				Classifier crrAa = new ClassifierBayes(listWindowClonedEdgesAa);
				Classifier crrSse = new ClassifierBayes(listWindowClonedEdgesSse);
				Classifier crrHp = new ClassifierBayes(listWindowClonedEdgesHp);

//				Classificator crmbAa = new ClassificatorModBayes(listWindowClonedEdgesAa);
//				Classificator crmbSse = new ClassificatorModBayes(listWindowClonedEdgesSse);
//				Classificator crmbHp = new ClassificatorModBayes(listWindowClonedEdgesHp);

//				System.out.println("crbAa: "+crbAa);
//				System.out.println("crbSse: "+crbSse);
//				System.out.println("crbHp: "+crbHp);

				Edge aaTmh = wintermute.getEdge(vertexMiddleAa, TMH);
				Edge sseTmh = wintermute.getEdge(vertexMiddleSse, TMH);
				Edge hpTmh = wintermute.getEdge(vertexMiddleHp, TMH);
				Edge aaNonTmh = wintermute.getEdge(vertexMiddleAa, NON_TMH);
				Edge sseNonTmh = wintermute.getEdge(vertexMiddleSse, NON_TMH);
				Edge hpNonTmh = wintermute.getEdge(vertexMiddleHp, NON_TMH);



//				System.out.println("AA"
//						+ "\n\tTMH: "+aaTmh
//						+"\n\tNON_TMH: "+aaNonTmh);
//				System.out.println("SSE"
//						+ "\n\tTMH: "+sseTmh
//						+"\n\tNON_TMH: "+sseNonTmh);
//				System.out.println("HP"
//						+ "\n\tTMH: "+hpTmh
//						+"\n\tNON_TMH: "+hpNonTmh);


				double aaT = normalizedMin;
				double aaN = normalizedMin;
				double sseT = normalizedMin;
				double sseN = normalizedMin;
				double hpT = normalizedMin;
				double hpN = normalizedMin;


				if (aaTmh != null) {
					aaT = aaTmh.getWeightTmh();
				}
				if (aaNonTmh != null) {
					aaN = aaNonTmh.getWeightNonTmh();
				}
				if (sseTmh != null) {
					sseT = sseTmh.getWeightTmh();
				}
				if (sseNonTmh != null) {
					sseN = sseNonTmh.getWeightNonTmh();
				}
				if (hpTmh != null) {
					hpT = hpTmh.getWeightTmh();
				}
				if (hpNonTmh != null) {
					hpN = hpNonTmh.getWeightNonTmh();
				}


//				double weightAaTmh = crrAa.getClassRateTmh() * crmbAa.getClassRateComplete();
//				double weightAaNonTmh = crrAa.getClassRateNonTmh() * crmbAa.getClassRateComplete();
//
//				double weightSseTmh = crrAa.getClassRateTmh() * crmbSse.getClassRateComplete();
//				double weightSseNonTmh = crrAa.getClassRateNonTmh() * crmbSse.getClassRateComplete();
//
//				double weightHpTmh = crrAa.getClassRateTmh() * crmbHp.getClassRateComplete();
//				double weightHpNonTmh = crrAa.getClassRateNonTmh() * crmbHp.getClassRateComplete();

				double weightAaTmh = aa * aaT;
				double weightAaNonTmh = aa * aaN;

				double weightSseTmh = sse * sseT;
				double weightSseNonTmh = sse * sseN;

				double weightHpTmh = hp * hpT;
				double weightHpNonTmh = hp * hpN;

//				System.out.println("AA:"
//						+ "\n\tweightAaTmh: " + weightAaTmh
//						+ "\n\tweightAaNonTmh: " + weightAaNonTmh);
//				System.out.println("SSE:"
//						+ "\n\tweightSseTmh: " + weightSseTmh
//						+ "\n\tweightSseNonTmh: " + weightSseNonTmh);
//				System.out.println("HP:"
//						+ "\n\tweightHpTmh: " + weightHpTmh
//						+ "\n\tweightHpNonTmh: " + weightHpNonTmh);
//
////				System.out.println("REALCLASS: "+spMiddle.getRealClass());
//
//				System.exit(1);



				double foundTmh = 1;
				double foundNonTmh = 1;

				if (weightAaTmh > weightAaNonTmh) {
					foundTmh *= aaT;
				} else if (weightAaTmh < weightAaNonTmh) {
					foundNonTmh *= aaN;
				}

				if (weightSseTmh > weightSseNonTmh) {
					foundTmh *= sseT;
				} else if (weightSseTmh < weightSseNonTmh) {
					foundNonTmh *= sseN;
				}

				if (weightHpTmh > weightHpNonTmh) {
					foundTmh *= hpT;
				} else if (weightHpTmh < weightHpNonTmh) {
					foundNonTmh *= hpN;
				}

//				System.out.println("PRED: TMH: " + foundTmh + " | NON_TMH: " + foundNonTmh);

				Result predicted = null;
				if (foundTmh > foundNonTmh) {
					predicted = Result.TMH;
				} else if (foundTmh < foundNonTmh) {
					predicted = Result.NON_TMH;
				} else {
					logger.fatal("WARNING: probability for prediction of TMH (" + foundTmh + ") and NON_TMH (" + foundNonTmh + ") are equal. Prediction set to: " + Result.OUTSIDE);
					predicted = Result.OUTSIDE;
				}






//				Classificator crmb = new ClassificatorModBayes(listWindowClonedEdgesAa);
//				Classificator crr = new ClassificatorRatio(listWindowClonedEdgesAa);
//
//				double weightTmh = crmb.getClassRateTmh() * crr.getClassRateTmh();
//				double weightNonTmh = crmb.getClassRateNonTmh() * crr.getClassRateNonTmh();

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

//				Result predicted = null;
//				if (weightTmh > weightNonTmh) {
//					predicted = Result.TMH;
//				} else if (weightTmh < weightNonTmh) {
//					predicted = Result.NON_TMH;
//				} else {
//					logger.fatal("WARNING: probability for prediction of TMH (" + weightTmh + ") and NON_TMH (" + weightNonTmh + ") are equal. Prediction set to: " + Result.OUTSIDE);
//					predicted = Result.OUTSIDE;
//				}

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
			logger.trace("FALSE PREDICTION: " + counterFalsePredicted + " (" + ((int) (100d / (double) sequence.length() * counterFalsePredicted)) + "%) (id: " + sequence.getId() + " -> length: " + sequence.length() + ")");
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

				Vertex vertexMiddleAa = null;
				Vertex vertexMiddleSse = null;
				Vertex vertexMiddleHp = null;
				SequencePosition spMiddle = null;

				for (int i = 0; i < slidingWindow.getSequence().length - 1; i++) {
					SequencePosition spSource = slidingWindow.getSequence()[i];
					Vertex vertexSourceAa;
					Vertex vertexSourceSse;
					Vertex vertexSourceHp;

					SequencePosition spTarget = slidingWindow.getSequence()[i + 1];
					Vertex vertexTargetAa;
					Vertex vertexTargetSse;
					Vertex vertexTargetHp;

					//source
					if (spSource == null) {
						continue;
					} else {
						String sourceAa = spSource.getAminoAcid().toString().intern();
						vertexSourceAa = mapVertex.get(sourceAa + ":" + SpecialVertex.NULL + ":" + Double.NaN);

						String sourceSse = spSource.getSecondaryStructure().toString().intern();
						vertexSourceSse = mapVertex.get(SpecialVertex.NULL + ":" + sourceSse + ":" + Double.NaN);

						Double sourceHp = round(spSource.getHydrophobicity());
						vertexSourceHp = mapVertex.get(SpecialVertex.NULL + ":" + SpecialVertex.NULL + ":" + sourceHp);
					}

					if (i == middle) {
						//if source vertex == middle vertex
						vertexMiddleAa = vertexSourceAa;
						vertexMiddleSse = vertexSourceSse;
						vertexMiddleHp = vertexSourceHp;
						spMiddle = spSource;
					}

					//target
					if (spTarget == null) {
						continue;
					} else {
						String targetAa = spTarget.getAminoAcid().toString().intern();
						vertexTargetAa = mapVertex.get(targetAa + ":" + SpecialVertex.NULL + ":" + Double.NaN);

						String targetSse = spTarget.getSecondaryStructure().toString().intern();
						vertexTargetSse = mapVertex.get(SpecialVertex.NULL + ":" + targetSse + ":" + Double.NaN);

						Double targetHp = round(spTarget.getHydrophobicity());
						vertexTargetHp = mapVertex.get(SpecialVertex.NULL + ":" + SpecialVertex.NULL + ":" + targetHp);
					}

					//link the source and target vertices
					addEdge(vertexSourceAa, spSource, vertexTargetAa, spTarget, false);
					addEdge(vertexSourceSse, spSource, vertexTargetSse, spTarget, false);
					addEdge(vertexSourceHp, spSource, vertexTargetHp, spTarget, false);

				}
				//link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				addEdge(vertexMiddleAa, spMiddle, null, null, true);
				addEdge(vertexMiddleSse, spMiddle, null, null, true);
				addEdge(vertexMiddleHp, spMiddle, null, null, true);
			}
		}
		trained = true;
		long end = System.currentTimeMillis();

//						Normalizer norm = new NormalizerHighestWeightToOne(wintermute);
//		Normalizer norm = new NormalizerMarkov(wintermute);

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
		pruneNotUsedVertices();
	}
}
