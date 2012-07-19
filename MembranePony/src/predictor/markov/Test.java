/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package predictor.markov;

import data.Hydrophobicity;
import evaluation.Evaluation;
import evaluation.EvaluationResult;
import input.DataReader;
import interfaces.Sequence;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.layout.CombinedNet;
import predictor.markov.layout.Markov;
import predictor.mss.MssResult;

/**
 *
 * @author rgreil
 */
public class Test {

	private static final Logger logger = Logger.getLogger(Test.class);

	public static void main(String[] args) throws Exception {
		Sequence[] seqs;

		Markov m = new CombinedNet();
//        Markov m = new TripleNet();

		File dataFolderOld = new File("E:\\CONFIG\\coding\\data\\protein_prediction\\lean-dataset\\impOutput");
		File dataFolder = new File("E:\\CONFIG\\coding\\data\\protein_prediction\\lean-dataset");
		File structFile = new File("E:\\CONFIG\\coding\\data\\protein_prediction\\lean-dataset\\imp_struct.fasta");
		int table = Hydrophobicity.KYTE_DOOLITTLE;

//        seqs = DataReader.readSequences(dataFolder, structFile, table);
////////        seqs = RandomSequenceGenerator.generate(1000);
//        m.train(seqs);
//        m.predict(seqs[27]);
//        m = new Markov();
//        seqs = RandomSequenceGenerator.generate(1000);
//        m.train(seqs);
//        m = new Markov();
//        seqs = RandomSequenceGenerator.generate(1000);
//        m.train(seqs);

//        m.predict(seqs[2]);
//        m.save(new File("markovREALDATA_10seqs.graph"));
//        m.save(new File("markovNEWREALDATA.txt"));
		Sequence[] sequences = DataReader.readAll(dataFolder, table, false);
//		Sequence[] sequences = DataReader.readTransmembranes(dataFolderOld, structFile, table, false);


		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());
		EvaluationResult result = eval.evaluate();
		System.out.println(result);

//		int seqNumber = 74;
//		int seqNumber = 23;
//		int seqNumber = 53;
//		int seqNumber = 43;
//
//		m.train(sequences);
//		m.predict(sequences[seqNumber]);
//		System.out.println(sequences[seqNumber].getId() + " - > is SOLUBLE " + !sequences[seqNumber].containsTransmembrane());
//		MssMod mss = new MssMod();
//		int length = sequences[seqNumber].length();
//		List<MssResult> al = mss.mss(sequences[seqNumber], 1);
//		List<MssResult> al2 = mss.mss(sequences[seqNumber], 2);
//		List<MssResult> al3 = mss.mss(sequences[seqNumber], 3);
//		List<MssResult> al4 = mss.mss(sequences[seqNumber], 4);
//		List<MssResult> al5 = mss.mss(sequences[seqNumber], 5);
//		mergeMss(al, 1, length);
//		mergeMss(al2, 2, length);
//		mergeMss(al3, 3, length);
//		mergeMss(al4, 4, length);
//		mergeMss(al5, 5, length);

//		for (Vertex vertex : m.getGraph().vertexSet()) {
//			System.out.println(vertex+" -> edges (out): "+m.getGraph().outDegreeOf(vertex)+" | in: "+m.getGraph().inDegreeOf(vertex));
//			for (Edge e : m.getGraph().outgoingEdgesOf(vertex)) {
//				System.out.println("\t"+e);
//			}
//		}





//			for (MssResult mssResult : al) {
//				System.out.println("mssResult: " + mssResult.getValue() + " [" + mssResult.getPositionStart() + "->" + mssResult.getPositionEnd() + "]");
//				System.out.print("MSSr:\t");
//				for (int i = 0; i < sequences[seqNumber].getSequence().length; i++) {
//					if (i >= mssResult.getPositionStart() && i <= mssResult.getPositionEnd()) {
//						System.out.print("!");
//					} else {
//						System.out.print("_");
//					}
//				}
//				System.out.println();
//			}
//		m.save(new File("TripleNet.txt"));
//		m = new Markov();
//		m.load(new File("TEST.txt"));
//        int max = 333;
//        Random rnd = new Random();
//        Sequence[] generated = RandomSequenceGenerator.generate(max);
////        for (Sequence sequence : generated) {
////            logger.info(sequence.getId() + "\t=>\t" + Arrays.toString(sequence.getSequence()));
////        }
//        m.train(generated);
////        Vertex v = m.getVertexReference(AminoAcid.L.toString()+":"+SSE.Helix.toString()+":"+(5.6d)+":"+6);
////        for (Edge edge : m.getGraph().edgeSet()) {
////            Vertex source = m.getGraph().getEdgeSource(edge);
////            System.out.println("vertex: "+source+" -> "+m.getGraph().outDegreeOf(source));
////            for (Edge edge1 : m.getGraph().outgoingEdgesOf(source)) {
////                System.out.println("\tedge: "+edge1);
////            }
////        }
//        int scale = generated[0].getSequence()[0].getHydrophobicityMatrix();
//        generated = RandomSequenceGenerator.generate(max);
//        while (scale!=generated[0].getSequence()[0].getHydrophobicityMatrix()) {
//            generated = RandomSequenceGenerator.generate(max);
//        }
//        for (int i = 0; i < (max/10); i++) {
//            m.predict(generated[rnd.nextInt((max-1))]);
//
//        }
////        m.train(RandomSequenceGenerator.generate(100));
//        m.save(new File("markov.graph"));
////        m = new Markov();
////        m.load(new File("markov.graph"));
////        m.save(new File("markov_NEW.graph"));
	}

	public static void mergeMss(List<MssResult> al, int dist, int length) {
		System.out.print("MSS"+dist+":\t");
		String[] mssMerged = new String[length];

		for (MssResult mssResult : al) {
			for (int i = mssResult.getPositionStart(); i <= mssResult.getPositionEnd(); i++) {
				mssMerged[i] = "!";
			}
		}
		for (String string : mssMerged) {
			if (string == null) {
				System.out.print(".");
			} else {
				System.out.print(string);
			}
		}
	}
}
