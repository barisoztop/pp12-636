package interfaces;

/**
 * This interface models a whole protein sequence. Implementing classes are expected to store all the available data
 * on the sequence and generate sliding windows for training/testing purposes.
 *  
 * @author Felix
 *
 */
public interface Sequence {

	/**
	 * @return the ID of this sequence, usually an UniProt Identifier.
	 */
	public String getId();
	
	/**
	 * the complete protein sequence
	 * @return
	 */
	public SequencePosition[] getSequence();
	
	
	/**
	 * note: the first ten or so windows will look like this:
	 * <pre>
	 * n n n n n X R R R R R
	 * n n n n R X R R R R R
	 * n n n R R X R R R R R
	 * n n R R R X R R R R R
	 * n R R R R X R R R R R
	 * </pre>
	 * and so on, with n=null, X=residue to be predicted, R=other residues.<br>
	 * That way we ensure that the prediction does start on the very first residue and not on
	 * number 10 or 11 or the like. The same applies for the last few windows! 
	 * 
	 * @return the individual sliding windows
	 */
	public SlidingWindow[] getWindows();
	
	public int length();

	/**
	 * 
	 * @return true if the sequence contains a transmembrane region, false otherwise. will not
	 * work for test sets etc. where no class attributes are present.
	 */
	public boolean containsTransmembrane();
	
}
