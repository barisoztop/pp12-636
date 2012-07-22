package visualization;

public class Edge {
	public final Vertex from, to;
	public final int[] weights;
	
	public Edge(Vertex from, Vertex to, int[] weights) {
		this.from = from; this.to = to; this.weights = weights;
	}
	
	@Override
	public String toString() {
		return from+" => "+to;
	}
}