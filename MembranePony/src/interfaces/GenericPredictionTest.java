package interfaces;

import input.SequenceImpl;
import input.SequencePositionImpl;

import org.junit.Test;

import data.AminoAcid;
import data.SSE;

public class GenericPredictionTest {

	@Test
	public void test() {
		
		SequencePosition[] seqpos = {
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.TMH),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.TMH),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.TMH),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.TMH),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.INSIDE),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.OUTSIDE),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.OUTSIDE),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.OUTSIDE),
			new SequencePositionImpl(AminoAcid.A, 1, SSE.Helix, 0, Result.TMH)
		};
		
		Result[] predictions = {
				Result.TMH,
				Result.TMH,
				Result.TMH,
				Result.TMH,
				Result.INSIDE,
				Result.INSIDE,	//should be outside
				Result.OUTSIDE,
				Result.OUTSIDE,
				Result.TMH
		};
		
		
		Sequence test = new SequenceImpl("test", seqpos);
		
		GenericPrediction gp = new GenericPrediction(test, predictions);
		
		assert(gp.getInputSequence().equals(test));
		
		for(int i=0; i<predictions.length; i++) {
			assert(predictions[i].equals(gp.getPredictionForResidue(i)));
		}
		
		assert(gp.getPredictedRegions().length == 4);
		
		Region r;
		
		r = gp.getPredictedRegions()[0]; 
		assert(r.start == 0 && r.end==3 && r.type==Result.TMH);
		
		r = gp.getPredictedRegions()[1]; 
		assert(r.start == 4 && r.end==5 && r.type==Result.INSIDE);
		
		r = gp.getPredictedRegions()[2]; 
		assert(r.start == 6 && r.end==7 && r.type==Result.OUTSIDE);
		
		r = gp.getPredictedRegions()[1]; 
		assert(r.start == 8 && r.end==8 && r.type==Result.TMH);
	}

}
