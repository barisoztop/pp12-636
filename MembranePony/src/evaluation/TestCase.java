package evaluation;

import interfaces.Sequence;

public class TestCase {
	
	public final Sequence sequence;
	public final Statistics transmembrane;
	public final Statistics inside;
	public final Statistics outside;
	
	
	public TestCase(Sequence sequence, Statistics transmembrane, Statistics inside, Statistics outside) {
		this.sequence = sequence;
		this.transmembrane = transmembrane;
		this.inside = inside;
		this.outside = outside;
	}
	
	
	
}
