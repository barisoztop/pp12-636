package predictor.markov.graph;

import org.jgrapht.ext.VertexNameProvider;

/**
 *
 * @author rgreil
 */
public class VertexNameProviderBase implements VertexNameProvider<Vertex> {

    @Override
    public String getVertexName(Vertex vertex) {
        return vertex.toString();
    }
}
