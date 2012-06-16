package interfaces;

public interface PredictorFactory {

	/**
	 * spawn a new instance of predictor
	 * @return
	 */
	public Predictor getInstance();

        public void setRangeForMappingContinousValuesToNodes(double range);
}