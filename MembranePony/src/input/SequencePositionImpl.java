package input;

import interfaces.SequencePosition;
import data.AminoAcid;
import data.SSE;

public class SequencePositionImpl implements SequencePosition {

	
	private SSE sse;
	private double hydrophobicity;
	private AminoAcid aa;

	public SequencePositionImpl(AminoAcid aa, double hydrophobicity, SSE sse) {
		this.aa = aa;
		this.hydrophobicity = hydrophobicity;
		this.sse = sse;
	}
	
	
	@Override
	public AminoAcid getAminoAcid() {
		return aa;
	}

	@Override
	public double getHydrophobicity() {
		return hydrophobicity;
	}

	@Override
	public SSE getSecondaryStructure() {
		return sse;
	}


}
