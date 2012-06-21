package interfaces;

/**
 * Result of a {@link Predictor} run. Add any statistics we may want to have.
 *
 */
public interface Prediction {
	
	/**
	 * 
	 * @return the protein sequence that the prediction pertains to
	 */
	public Sequence getInputSequence();
	
	/**
	 * 
	 * @param residueNr, starting at 0
	 * @return the prediction (in TMH, outside/inside of cell) for each residue of the input sequence
	 */
	public Result getPredictionForResidue(int residueNr);
	
	/**
	 * 
	 * @return the predicted regions; each continuous subsequence of a single type is a region
	 */
	public Region[] getPredictedRegions();
}
