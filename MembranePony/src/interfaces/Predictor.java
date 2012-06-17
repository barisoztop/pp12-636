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
		 * note: this method has changed a lot! review your code please!
		 * @param sequence
		 */
		public void predict(Sequence sequence);

		//TODO review your code! this has changed
		/**
		 * note: this method has changed considerably. Please check if you can work with this.
		 * @param trainingCases
		 */
		public void train(TestTrainingCase[] trainingCases);
	
		//felix: this is not needed here anymore, i've moved it into Markov
		//i've added a custom markovfactory so it can be accessed.
		
//        public void setMappingContValuesToNodes(double range);

}
