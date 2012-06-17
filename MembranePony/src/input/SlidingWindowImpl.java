package input;

import interfaces.Sequence;
import interfaces.SequencePosition;
import interfaces.SlidingWindow;

public class SlidingWindowImpl implements SlidingWindow {

	private SequencePosition[] sequence;
	private Sequence parentSequence;
	private int index;

	public SlidingWindowImpl(Sequence parentSequence, int index, SequencePosition[] sequence) {
		this.parentSequence = parentSequence;
		this.index = index;
		this.sequence = sequence;
	}
	
	@Override
	public int getWindowIndex() {
		return index;
	}

	@Override
	public SequencePosition[] getSequence() {
		return sequence;
	}

	@Override
	public Sequence getParentSequence() {
		return parentSequence;
	}

}
