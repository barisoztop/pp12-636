package markov.graph;

import org.jgrapht.ext.VertexNameProvider;

/**
 *
 * @author rgreil
 */
public class MarkovVertexNameProvider implements VertexNameProvider<Vertex> {

    @Override
    public String getVertexName(Vertex vertex) {
        return vertex.toString() + ":" + vertex.getRealClassInside() + ":"
                + vertex.getRealClassOutside() + ":" + vertex.getRealClassNonTmh()
                + ":" + vertex.getRealClassTmh();
    }
}
