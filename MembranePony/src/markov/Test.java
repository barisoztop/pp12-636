/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package markov;

import input.RandomSequenceGenerator;
import interfaces.Sequence;
import java.io.File;
import java.util.Arrays;
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
//        System.out.println("vertex: "+m.getGraph().vertexSet().size());
//        System.out.println("edges: "+m.getGraph().edgeSet().size());
//        System.out.println(RandomSequenceGenerator.generate(10));
//        long start = System.currentTimeMillis();

        Sequence[] generated = RandomSequenceGenerator.generate(333);
//        for (Sequence sequence : generated) {
//            logger.info(sequence.getId() + "\t=>\t" + Arrays.toString(sequence.getSequence()));
//        }
        m.train(generated);
//        m.train(RandomSequenceGenerator.generate(100));
        m.save(new File("markov.graph"));
        m = new Markov();
        m.load(new File("markov.graph"));
//        m.save(new File("markov_NEW.graph"));
//        for (Object object : m.getGraph().incomingEdgesOf(m.OUTSIDE)) {
//            System.out.println(object);
//        }
//        for (Object object : m.getGraph().incomingEdgesOf(m.INSIDE)) {
//            System.out.println(object);
//        }
//        for (Object object : m.getGraph().incomingEdgesOf(m.TMH)) {
//            System.out.println(object);
//        }
//        long end = System.currentTimeMillis();
//
//        System.out.println("time needed: "+(end-start)+" ms");
//
//        for (int i = 0; i <= 5; i++) {
//            double[] tmp = Hydrophobicity.getMinMax(i);
//            System.out.println("scale: "+i+": "+tmp[0]+" : "+tmp[1]);
//        }
    }
}