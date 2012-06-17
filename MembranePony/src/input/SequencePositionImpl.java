package input;

import interfaces.SequencePosition;
import data.AminoAcid;
import data.SSE;

public class SequencePositionImpl implements SequencePosition {

	
	private SSE sse;
	private double hydrophobicity;
	private AminoAcid aa;
	private int hydrophobicityMatrix;

	public SequencePositionImpl(AminoAcid aa, double hydrophobicity, SSE sse, int hydrophobicityMatrix) {
		this.aa = aa;
		this.hydrophobicity = hydrophobicity;
		this.sse = sse;
		this.hydrophobicityMatrix = hydrophobicityMatrix;
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


	@Override
	public int getHydrophobicityMatrix() {
		return hydrophobicityMatrix;
	}

	
	@Override
	public String toString() {
		return "("+aa+", "+sse+", hydrop="+hydrophobicity+"/scale="+hydrophobicityMatrix+")";
	}

}
