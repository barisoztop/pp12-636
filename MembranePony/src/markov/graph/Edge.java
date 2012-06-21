package markov.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * simple class for representing a markovedge
 * @author rgreil
 */
public class Edge extends DefaultWeightedEdge {

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget()+")_w:" + getWeight();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }
}
