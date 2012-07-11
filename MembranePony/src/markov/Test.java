/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package markov;

import data.Hydrophobicity;
import evaluation.Evaluation;
import evaluation.EvaluationResult;
import input.DataReader;
import interfaces.Sequence;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import markov.layout.Markov;
import markov.layout.TripleNet;
import org.apache.log4j.Logger;

/**
 *
 * @author rgreil
 */
public class Test {

    private static final Logger logger = Logger.getLogger(Test.class);

    public static void main(String[] args) throws Exception {
        Sequence[] seqs;

//        Markov m = new MarkovOneNet();
        Markov m = new TripleNet();

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
//		Sequence[] sequences = DataReader.readTransmembranes(dataFolderOld,structFile, table, false);

		Collections.shuffle(Arrays.asList(sequences));

		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());
		EvaluationResult result = eval.evaluate();
		System.out.println(result);

//		m.train(sequences);
//		m.predict(sequences[23]);
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