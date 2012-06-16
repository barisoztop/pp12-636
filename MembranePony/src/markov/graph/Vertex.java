package markov.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * simple class for represeting a morkov vertex specified by<br> <li> aminoacid
 * <li> sse <li> hydrophobocity
 *
 * @author rgreil
 */
public class Vertex {

    private static final Logger logger = Logger.getLogger(Vertex.class.getSimpleName());
    private final String aminoacid;
    private final String sse;
    private final Double hydrophobocity;
    private final int windowPos;
    private final String id;

    public Vertex(String aminoacid, String sse, Double hydrophobocity, int windowPos) {
        if (!aminoacid.isEmpty() && sse == null && hydrophobocity == null && windowPos == -1) {
            this.aminoacid = aminoacid;
            this.sse = null;
            this.hydrophobocity = null;
            this.windowPos = -1;
            id = aminoacid;
            logger.log(Level.INFO, "created: {0}", id);
        } else {
            this.aminoacid = aminoacid.intern();
            this.sse = sse.intern();
            this.hydrophobocity = hydrophobocity;
            this.windowPos = windowPos;
            id = aminoacid + "-" + sse + "-" + hydrophobocity + "@" + windowPos;
        }
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Vertex other = (Vertex) obj;
        if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.id.hashCode();
        return hash;
    }

    public String getAminoacid() {
        return aminoacid;
    }

    public Double getHydrophobocity() {
        return hydrophobocity;
    }

    public String getId() {
        return id;
    }

    public String getSse() {
        return sse;
    }

    public int getWindowPos() {
        return windowPos;
    }
}
