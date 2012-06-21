package markov.graph;

/**
 * simple class for represeting a morkov vertex specified by<br> <li> aminoacid
 * <li> sse <li> hydrophobocity
 *
 * @author rgreil
 */
public class Vertex {

    private String aminoacid;
    private String sse;
    private final Double hydrophobocity;
    private final int windowPos;
    private final String id;

    public Vertex(String aminoacid, String sse, Double hydrophobocity, int windowPos) {
        this.aminoacid = aminoacid.intern();
//        String tmp = "aa:"+this.aminoacid;
        String tmp = "aa:"+this.aminoacid;
        if (sse == null) {
            this.sse = null;
            tmp += "_sse:" + this.sse;
        } else {
            this.sse = sse.intern();
//            tmp += "|sse:" + this.sse;
            tmp += "_sse:" + this.sse;
        }
        if (hydrophobocity == null) {
            this.hydrophobocity = null;
            tmp += "_hp:" + this.hydrophobocity;
        } else {
            this.hydrophobocity = hydrophobocity;
//            tmp += "|hp:" + this.hydrophobocity;
            tmp += "_hp:" + this.hydrophobocity;
        }
        if (windowPos == -1) {
            this.windowPos = -1;
            tmp += "_@" + windowPos;
        } else {
            this.windowPos = windowPos;
//            tmp += "|@" + windowPos;
            tmp += "_@" + windowPos;
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
