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

}
