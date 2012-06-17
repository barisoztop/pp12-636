package interfaces;

import data.AminoAcid;
import data.SSE;

public interface SequencePosition {

	public AminoAcid getAminoAcid();
	public double getHydrophobicity();
	public SSE getSecondaryStructure();

}
