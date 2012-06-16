package evaluation.markov.graph;

/**
 * simple class for represeting a morkov node specified by<br> <li> aminoacid
 * <li> sse <li> hydrophobocity
 *
 * @author rgreil
 */
public class Node {

    private final String aminoacid;
    private final String sse;
    private final Double hydrophobocity;
    private final String id;

    public Node(String aminoacid, String sse, Double hydrophobocity) {
        this.aminoacid = aminoacid;
        this.sse = sse;
        this.hydrophobocity = hydrophobocity;
        id = aminoacid + "-" + sse + "-" + hydrophobocity;
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
        final Node other = (Node) obj;
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
}
