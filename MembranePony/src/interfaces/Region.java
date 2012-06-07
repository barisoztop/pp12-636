package interfaces;

public class Region {
	
	public final int start;
	public final int end;
	public final Result type;
	
	public Region(int start, int end, Result type) {
		this.start = start;
		this.end = end;
		this.type = type;
	}
	
}
