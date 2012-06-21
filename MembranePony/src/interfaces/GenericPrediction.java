package interfaces;

import java.util.LinkedList;

public class GenericPrediction implements Prediction {

	
	private Sequence input;
	private Result[] predictions;

	public GenericPrediction(Sequence input, Result[] predictions) {
		this.input = input;
		this.predictions = predictions;
	}
	
	
	@Override
	public Sequence getInputSequence() {
		return input;
	}

	@Override
	public Result getPredictionForResidue(int residueNr) {
		return predictions[residueNr];
	}

	@Override
	public Region[] getPredictedRegions() {
		
		LinkedList<Region> regions = new LinkedList<Region>();
		
		Result current = predictions[0];
		int currentStart = 0;
		
		for(int i=1; i<predictions.length; i++) {
			if(predictions[i]!=current) {
				Region r = new Region(currentStart, i-1, current);
				regions.add(r);
				
				current = predictions[i];
				currentStart = i;
			}
		}
				
		return regions.toArray(new Region[] {});
	}

}
