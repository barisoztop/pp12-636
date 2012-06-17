package interfaces;

/**
 * use implementing classes of this interface to provide an easy way to spawn fully configured
 * instances of a certain predictor class. implementing classes should contain any implementation-
 * specific getters/setters, like parameters for the classifier or the like. These can be set
 * beforehand, then the factory instance can be passed to, say, the evaluation code, which will
 * simply have to invoke getInstance() as often as needed without having to worry about any
 * implementation-specific details.
 *
 */
public interface PredictorFactory {

	/**
	 * spawn a new instance of predictor
	 * @return
	 */
	public Predictor getInstance();

	//felix: this is markov-specific and i think it would better be placed in a 
	//markovpredictorfactory class. i've created one and moved it there.
	
//        public void setRangeForMappingContinousValuesToNodes(double range);
}