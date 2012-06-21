/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package markov;

import input.RandomSequenceGenerator;
import java.io.File;
import markov.layout.Markov;

/**
 *
 * @author rgreil
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Markov m = new Markov();
//        System.out.println("vertex: "+m.getGraph().vertexSet().size());
//        System.out.println("edges: "+m.getGraph().edgeSet().size());
//        System.out.println(RandomSequenceGenerator.generate(10));
//        long start = System.currentTimeMillis();
        m.train(RandomSequenceGenerator.generate(100));
//        m.train(RandomSequenceGenerator.generate(100));
        m.save(new File("markov.graph"));
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