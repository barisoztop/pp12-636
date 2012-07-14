package predictor.markov.classificator;

import java.util.List;
import org.apache.log4j.Logger;
import predictor.markov.graph.Edge;

/**
 *
 * @author greil
 */
public abstract class Classificator {

	protected Logger logger = Logger.getLogger(Classificator.class);
	protected Double classRateTmh = Double.NaN;
	protected Double classRateNonTmh = Double.NaN;
	protected Double classRateComplete = Double.NaN;
	protected List<Edge> listEdge;

	protected abstract void compute();

	public Double getClassRateTmh() {
		return classRateTmh;
	}

	public Double getClassRateNonTmh() {
		return classRateNonTmh;
	}

	public Double getClassRateComplete() {
		return classRateComplete;
	}

	@Override
	public String toString() {
		return classRateComplete+":"+classRateTmh+":"+classRateNonTmh;
	}
}
