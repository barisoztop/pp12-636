package markov.graph;

import data.AminoAcid;
import data.SSE;
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
    private String aminoacid;
    private String sse;
    private final Double hydrophobocity;
    private final int windowPos;
    private final String id;

    public Vertex(String aminoacid, String sse, Double hydrophobocity, int windowPos) {
        this.aminoacid = aminoacid.intern();
        String tmp = aminoacid;
        if (sse == null) {
            this.sse = null;
        } else {
            this.sse = sse.intern();
            tmp += "_sse:" + this.sse;
        }
        if (hydrophobocity == null) {
            this.hydrophobocity = null;
        } else {
            this.hydrophobocity = hydrophobocity;
            tmp += "_hp:" + this.hydrophobocity;
        }
        if (windowPos == -1) {
            this.windowPos = -1;
        } else {
            this.windowPos = windowPos;
            tmp += "@" + windowPos;
        }
        id = tmp;

//        logger.log(Level.INFO, "created vertex: {0}", id);
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
