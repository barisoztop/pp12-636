package input;

import interfaces.Result;
import interfaces.SequencePosition;
import data.AminoAcid;
import data.SSE;

public class SequencePositionImpl implements SequencePosition {

    private SSE sse;
    private double hydrophobicity;
    private AminoAcid aa;
    private int hydrophobicityMatrix;
    private Result realClass = null;

    public SequencePositionImpl(AminoAcid aa, double hydrophobicity, SSE sse, int hydrophobicityMatrix) {
        this.aa = aa;
        this.hydrophobicity = hydrophobicity;
        this.sse = sse;
        this.hydrophobicityMatrix = hydrophobicityMatrix;
    }

    public SequencePositionImpl(AminoAcid aa, double hydrophobicity, SSE sse, int hydrophobicityMatrix, Result realClass) {
        this(aa, hydrophobicity, sse, hydrophobicityMatrix);
        this.realClass = realClass;
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
        return "(" + aa + ", " + sse + ", hp=" + hydrophobicity + ", hpscale=" + hydrophobicityMatrix + (realClass == null ? "" : ", " + realClass) + ")";
    }

    @Override
    public Result getRealClass() {
        return realClass;
    }

	public void setHydrophobicity(double d) {
		hydrophobicity = d;
	}
}
