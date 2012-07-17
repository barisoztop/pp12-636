package predictor.mss;

/**
 *
 * @author greil
 */
public class MssResult {

	private final int positionStart;
	private final int positionEnd;
	private final double value;

	public MssResult(int start, int end, double value) {
		this.positionStart = start;
		this.positionEnd = end;
		this.value = value;
	}

	public int getPositionStart() {
		return positionStart;
	}

	public int getPositionEnd() {
		return positionEnd;
	}

	public double getValue() {
		return value;
	}
}
