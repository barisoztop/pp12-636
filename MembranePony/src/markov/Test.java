/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package markov;

import markov.layout.Markov;

/**
 *
 * @author rgreil
 */
public class Test {

    public static void main(String[] args) {
        Markov m = new Markov();
        System.out.println("vertex: "+m.getGraph().vertexSet().size());
        System.out.println("edges: "+m.getGraph().edgeSet().size());
    }
}
