/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package predictor.markov.layout;

import interfaces.Prediction;
import interfaces.Sequence;

/**
 *
 * @author rgreil
 */
public class MultiEdgeNet extends Markov{

	@Override
	public Prediction predict(Sequence sequence) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void train(Sequence[] trainingCases) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void addVertices() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
