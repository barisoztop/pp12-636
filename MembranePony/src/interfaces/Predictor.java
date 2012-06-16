package interfaces;

public interface Predictor {

	public void predict(Position[] sequence);

        public void train(Position[] sequence);

        public void setMappingContValuesToNodes(double range);

}
