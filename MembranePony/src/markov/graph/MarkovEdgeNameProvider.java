package markov.graph;

import org.jgrapht.ext.EdgeNameProvider;

/**
 *
 * @author rgreil
 */
public class MarkovEdgeNameProvider implements EdgeNameProvider<Edge>{

    @Override
    public String getEdgeName(Edge edge) {
//        return "w:"+edge.getWeight()+";oI:"+edge.getOverInside()+";oO:"+edge.getOverOutside();
        return edge.getWeight()+":"+edge.getOverInside()+":"+edge.getOverOutside();
    }

}
