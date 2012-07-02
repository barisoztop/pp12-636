/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package markov;

import data.Hydrophobicity;
import evaluation.Evaluation;
import evaluation.EvaluationResult;
import input.DataReader;
import input.RandomSequenceGenerator;
import interfaces.Sequence;
import java.io.File;
import java.util.Random;
import markov.layout.Markov;
import org.apache.log4j.Logger;

/**
 *
 * @author rgreil
 */
public class Test {

    private static final Logger logger = Logger.getLogger(Test.class);

    public static void main(String[] args) throws Exception {
        Markov m = new Markov();
        File dataFolder = new File("Z:\\CONFIG\\coding\\data\\protein_prediction\\lean-dataset\\impOutput");
        File structFile = new File("Z:\\CONFIG\\coding\\data\\protein_prediction\\lean-dataset\\imp_struct.fasta");
        int table = Hydrophobicity.KYTE_DOOLITTLE;


//        Sequence[] seqs = DataReader.readSequences(dataFolder, structFile, table);
//        m.train(seqs);
//        m.predict(seqs[4]);
//        m.save(new File("markovREALDATA_10seqs.graph"));
//        m.save(new File("markovNEWREALDATA.txt"));

		Sequence[] sequences = DataReader.readSequences(dataFolder, structFile, table);

		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());

		EvaluationResult result = eval.evaluate();
//
		System.out.println(result);



//        int max = 333;
//        Random rnd = new Random();
//        Sequence[] generated = RandomSequenceGenerator.generate(max);
////        for (Sequence sequence : generated) {
////            logger.info(sequence.getId() + "\t=>\t" + Arrays.toString(sequence.getSequence()));
////        }
//        m.train(generated);
//        int scale = generated[0].getSequence()[0].getHydrophobicityMatrix();
//        generated = RandomSequenceGenerator.generate(max);
//        while (scale!=generated[0].getSequence()[0].getHydrophobicityMatrix()) {
//            generated = RandomSequenceGenerator.generate(max);
//        }
//        for (int i = 0; i < (max/10); i++) {
//            m.predict(generated[rnd.nextInt((max-1))]);
//
//        }
//        m.train(RandomSequenceGenerator.generate(100));
//        m.save(new File("markov.graph"));
//        m = new Markov();
//        m.load(new File("markov.graph"));
//        m.save(new File("markov_NEW.graph"));
    }
}