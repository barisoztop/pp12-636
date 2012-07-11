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
import markov.normalizer.NormalizerHighestWeightToOne;
import markov.normalizer.NormalizerMarkov;
import org.apache.log4j.Logger;
import org.jgrapht.ext.GraphMLExporter;

/**
 *
 * @author rgreil
 */
public class CombinedNet extends Markov {

	public CombinedNet() {
//		logger = Logger.getLogger(MarkovOneNet.class);
		logger.info("spawning new " + this.getClass().getSimpleName());
		wintermute = new MarkovDirectedWeightedGraph<Vertex, Edge>(Edge.class);
	}

	@Override
	protected void addVertices() {
		long start = System.currentTimeMillis();
		mapVertex = new HashMap<String, Vertex>();
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

				if (i == middle) {
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
					addEdge(vertexSource, spSource, vertexTarget, spTarget, false);

				}
				//link the middle node to the RealClass (OUTSIDE, INSIDE, TMH)
				logger.trace("SequencePosition: middle " + spMiddle);
				addEdge(vertexMiddle, spMiddle, null, null, true);
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
}
