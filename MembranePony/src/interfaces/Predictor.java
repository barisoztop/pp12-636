package interfaces;

/**
 * This interfaces should be used to make available the most basic prediction functionality. Everything
 * implementation-specific (e.g. classifier parameters or the like) should go in implementing classes.
 * See {@link PredictorFactory} on how to provide pre-configured predictors for, say, the evaluation
 * code.
 * 
 */
public interface Predictor {

		//TODO review your code! this has changed
		/**
		 * note: this method has changed a lot! review your code please!<p>
		 * 
		 * you usually don't have {@link Result} class values in the {@link SequencePosition} objects here.
		 * 
		 * @param sequence
		 * @returns Prediction
		 */
		public Prediction predict(Sequence sequence);

		//TODO review your code! this has changed
		/**
		 * note: this method has changed considerably. Please check if you can work with this.<p>
		 * 
		 * the {@link Sequence} objects passed to this method are guaranteed to have results annotated to the
		 * individual {@link SequencePosition} objects.
		 * 
		 * @param trainingCases
		 */
		public void train(Sequence[] trainingCases);
	

}
