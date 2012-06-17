package interfaces;

/**
 * simple class to store information both for training cases and test cases.
 *
 */
public interface TestTrainingCase extends Sequence {
		
	/**
	 * The real-world state of the center residue of the given sequence
	 * @return
	 */
	public Result getClassification();
	
}
