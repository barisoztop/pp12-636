package predictor.markov.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple class for represeting a morkov vertex specified by<br> <li>
 * aminoacid:String <li> sse:String <li> hydrophobocity:Double <li>
 * windowPos:int
 *
 * @author rgreil
 */
public class Vertex {

	private String aminoacid;
	private String sse;
	private final Double hydrophobocity;
//    private final int windowPos;
	private final String id;
	private Map<String, List<Edge[]>> windows;
	//    private int realClassInside = 0;
//    private int realClassOutside = 0;
//    private int realClassTmh = 0;
//    private int realClassNonTmh = 0;

//    public Vertex(Enum aminoacid, Enum sse, Double hydrophobocity, int windowPos) {
//        //aminoAcid:Sting
//        this.aminoacid = aminoacid.toString().intern();
//        String tmp = this.aminoacid;
//
//        //secondaryStructure:String
//        this.sse = sse.toString().intern();
//        tmp += ":" + this.sse;
//
//        //hydrophobocity:Double
//        this.hydrophobocity = hydrophobocity;
//        tmp += ":" + this.hydrophobocity.toString().intern();
//
//        //windowPosition:int
//        this.windowPos = windowPos;
//        tmp += ":" + this.windowPos;
//
//        //create internal id
//        id = tmp;
//    }
	public Vertex(Enum aminoacid, Enum sse, Double hydrophobocity) {
		windows = new HashMap<String, List<Edge[]>>();

		//aminoAcid:String
		this.aminoacid = aminoacid.toString().intern();
		String tmp = this.aminoacid;

		//secondaryStructure:String
		this.sse = sse.toString().intern();
		tmp += ":" + this.sse;

		//hydrophobocity:Double
		this.hydrophobocity = hydrophobocity;
		tmp += ":" + this.hydrophobocity.toString().intern();

		//create internal id
		id = tmp;
	}

	public Vertex(String aminoacid, String sse, Double hydrophobocity) {
		windows = new HashMap<String, List<Edge[]>>();
		//aminoAcid:String
		this.aminoacid = aminoacid.intern();
		String tmp = this.aminoacid;

		//secondaryStructure:String
		this.sse = sse.intern();
		tmp += ":" + this.sse;

		//hydrophobocity:Double
		this.hydrophobocity = hydrophobocity;
		tmp += ":" + this.hydrophobocity.toString().intern();

		//create internal id
		id = tmp;
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

	public String getSse() {
		return sse;
	}

	public void addWindowEdge(Edge[] windowEdge) {
		String query = "";
		for (Edge edge : windowEdge) {
			query+=edge.getSource().toString()+edge.getTarget().toString();
		}
		if (windows.containsKey(query)) {
			windows.get(query).add(windowEdge);
		} else {
			List<Edge[]> list = new ArrayList<Edge[]>();
			list.add(windowEdge);
			windows.put(query, list);
		}
	}

	public Map<String, List<Edge[]>> getWindowEdge() {
		return windows;
	}

	public List<Edge[]> getWindowEdge(String key) {
		return windows.get(key);
	}

//    public String getId() {
//        return id;
//    }
//    public int getWindowPos() {
//        return windowPos;
//    }
//    public int getRealClassInside() {
//        return realClassInside;
//    }
//
//    public void setRealClassInside(int realClassInside) {
//        this.realClassInside = realClassInside;
//    }
//
//    public int getRealClassNonTmh() {
//        return realClassNonTmh;
//    }
//
//    public void setRealClassNonTmh(int realClassNonTmh) {
//        this.realClassNonTmh = realClassNonTmh;
//    }
//
//    public int getRealClassOutside() {
//        return realClassOutside;
//    }
//
//    public void setRealClassOutside(int realClassOutside) {
//        this.realClassOutside = realClassOutside;
//    }
//
//    public int getRealClassTmh() {
//        return realClassTmh;
//    }
//
//    public void setRealClassTmh(int realClassTmh) {
//        this.realClassTmh = realClassTmh;
//    }
}
