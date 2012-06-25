package markov.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * simple class for representing a markovedge
 *
 * @author rgreil
 */
public class Edge extends DefaultWeightedEdge {

    private int overInside = 0;
    private int overOutside = 0;

    @Override
    public String toString() {
        return "(" + getSource() + " : " + getTarget() + ")_w:" + getWeight();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    public void setOverInside(int i) {
        overInside = i;
    }

    public int getOverInside() {
        return overInside;
    }

    public void setOverOutside(int i) {
        overOutside = i;
    }

    public int getOverOutside() {
        return overOutside;
    }
}
