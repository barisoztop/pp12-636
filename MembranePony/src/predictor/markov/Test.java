/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package predictor.markov;

import data.Hydrophobicity;
import input.DataReader;
import interfaces.Sequence;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.layout.CombinedNet;
import predictor.markov.layout.Markov;
import predictor.mss.MssMod;
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

		Collections.shuffle(Arrays.asList(sequences));

//		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());
//		EvaluationResult result = eval.evaluate();
//		System.out.println(result);

		int seqNumber = 50;

		m.train(sequences);
		m.predict(sequences[seqNumber]);
		System.out.println(sequences[seqNumber].getId()+" - > is SOLUBLE "+!sequences[seqNumber].containsTransmembrane());
		MssMod mss = new MssMod();
		List<MssResult> al = mss.mss(sequences[seqNumber], 2);
		String[] mssMerged = new String[sequences[seqNumber].length()];

		System.out.print("MSSm:\t");
		for (MssResult mssResult : al) {
			for (int i = mssResult.getPositionStart(); i <= mssResult.getPositionEnd(); i++) {
				mssMerged[i] = "!";
			}
		}
		for (String string : mssMerged) {
			if (string == null) {
				System.out.print("_");
			} else {
				System.out.print(string);
			}
		}

			System.out.println("");



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



}