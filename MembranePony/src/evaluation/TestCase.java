package evaluation;

import interfaces.Sequence;

public class TestCase {

	public final Sequence sequence;
	public final Statistics transmembrane;
	public final Statistics inside;
	public final Statistics outside;
	public final Statistics nontmh;

	public TestCase(Sequence sequence, Statistics transmembrane, Statistics nontmh, Statistics inside, Statistics outside) {
		this.sequence = sequence;
		this.transmembrane = transmembrane;
		this.nontmh = nontmh;
		this.inside = inside;
		this.outside = outside;
	}
}
