package interfaces;

import data.AminoAcid;
import data.Hydrophobicity;
import data.SSE;

public interface SequencePosition {

	public AminoAcid getAminoAcid();
	public double getHydrophobicity();
	public SSE getSecondaryStructure();
	
	/**
	 * 
	 * @return the index of the hydrophobicity matrix from {@link Hydrophobicity} that was
	 * used to generate the hydroph. value for this position
	 */
	public int getHydrophobicityMatrix();
	
	/**
	 * 
	 * @return the class the residue really belongs to (in reality, that is, in experimental
	 * data and such); may be null if unknown
	 */
	public Result getRealClass();

}
