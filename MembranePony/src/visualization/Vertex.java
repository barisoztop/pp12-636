package visualization;

public class Vertex {
	public final float hp;
	public final String aa;
	public final String sse;
	
	public Vertex(String aa, String sse, float hp) {
		this.aa = aa; this.sse = sse; this.hp = hp;
	}
	
	public Vertex(String vertexCode) {
		String[] parts = vertexCode.split(":");
		aa = parts[0];
		sse = parts[1];
		hp = Float.parseFloat(parts[2]);
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Vertex) {
			Vertex v = (Vertex)arg0;
			return v.hp==hp && v.aa.equals(aa) && v.sse.equals(sse);
		}			
		return super.equals(arg0);
	}
	
	@Override
	public int hashCode() {
		return (aa+":"+sse+":"+hp).hashCode();
	}
	
	@Override
	public String toString() {
		return aa+" : "+sse+" : "+hp;
	}
}