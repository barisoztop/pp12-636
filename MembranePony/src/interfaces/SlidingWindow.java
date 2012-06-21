package interfaces;

public interface SlidingWindow {

	/**
	 *
	 * @return number of the current window in the sequence it was taken from, starting at 0
	 */
	public int getWindowIndex();

	/**
	 *
	 * @return the sequence of the current window
	 */
	public SequencePosition[] getSequence();

	/**
	 *
	 * @return the full protein sequence this window was taken out of
	 */
	public Sequence getParentSequence();
}

