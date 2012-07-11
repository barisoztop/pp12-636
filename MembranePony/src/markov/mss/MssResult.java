package markov.mss;

/**
 *
 * @author greil
 */
public class MssResult {

	private final int start;
	private final int end;
	private final double value;

	public MssResult(int start, int end, double value) {
		this.start = start;
		this.end = end;
		this.value = value;
	}

	public int getPositionStart() {
		return start;
	}

	public int getPositionEnd() {
		return end;
	}

	public double getValue() {
		return value;
	}
}
