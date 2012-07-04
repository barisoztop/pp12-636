package input;

import data.Constants;
import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;
import interfaces.SlidingWindow;

public class SequenceImpl implements Sequence {

    private String id;
    private SequencePosition[] sequence;

    public SequenceImpl(String id, SequencePosition[] sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SequencePosition[] getSequence() {
        return sequence;
    }

    @Override
    public SlidingWindow[] getWindows() {
        SlidingWindow[] result = new SlidingWindow[sequence.length];

        for (int i = 0; i < sequence.length; i++) {

            SequencePosition[] window = new SequencePosition[Constants.WINDOW_LENGTH];

            //half the window sans center (rounded down, so for window 21 it's 10 etc)
            int half = Constants.WINDOW_LENGTH / 2;

            for (int j = 0; j < window.length; j++) {
                int seqPos = j + i - half;

                if (seqPos < 0 || seqPos >= sequence.length) {
                    window[j] = null;
                } else {
                    window[j] = sequence[seqPos];
                }

            }

            result[i] = new SlidingWindowImpl(this, i, window);
        }

        return result;
    }
    
    @Override
    public boolean containsTransmembrane() {
    	for(SequencePosition p : sequence)
    		if(p.getRealClass()==Result.TMH)
    			return true;
    	
    	return false;
    }

    @Override
    public String toString() {
        return "Sequence [id=" + id + ", len=" + sequence.length + "]";
    }
    
    @Override
    public int length() {
    	return sequence.length;
    }
}
