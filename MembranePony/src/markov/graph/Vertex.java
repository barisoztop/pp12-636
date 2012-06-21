package markov.graph;

/**
 * simple class for represeting a morkov vertex specified by<br>
 * <li> aminoacid:String
 * <li> sse:String <li> hydrophobocity:Double <li> windowPos:int
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
        //aminoAcid:Sting
        this.aminoacid = aminoacid.intern();
        String tmp = this.aminoacid;

        //secondaryStructure:String
//        if (sse == null) {
//            this.sse = null;
//        } else {
            this.sse = sse.intern();
//        }
        tmp += ":" + this.sse;

        //hydrophobocity:Double
//        if (hydrophobocity == null) {
//            this.hydrophobocity = null;
//
//        } else {
            this.hydrophobocity = hydrophobocity;
//        }
        tmp += ":" + this.hydrophobocity.toString().intern();

        //windowPosition:int
//        if (windowPos == -1) {
//            this.windowPos = -1;
//        } else {
            this.windowPos = windowPos;
//        }
        tmp += ":" + windowPos;

        //create internal id
        id = tmp;
    }

    @Override
    public String toString() {
//        String result = "aa:" + getAminoacid() + "_sse:" + getSse() + "_hp:" + getHydrophobocity() + "_@:" + getWindowPos();
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

    public String getSse() {
        return sse;
    }

//    public String getId() {
//        return id;
//    }

    public int getWindowPos() {
        return windowPos;
    }
}
