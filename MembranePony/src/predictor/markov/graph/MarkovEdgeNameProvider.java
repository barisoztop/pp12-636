package predictor.markov.graph;

import java.text.NumberFormat;
import java.util.Locale;
import org.jgrapht.ext.EdgeNameProvider;

/**
 *
 * @author rgreil
 */
public class MarkovEdgeNameProvider implements EdgeNameProvider<Edge>{
	private NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);

	public MarkovEdgeNameProvider() {
		nf.setMinimumFractionDigits(0);
		nf.setMinimumIntegerDigits(1);
		nf.setGroupingUsed(false);
	}

    @Override
    public String getEdgeName(Edge edge) {
        return nf.format(edge.getWeightComplete())+":"+nf.format(edge.getWeightTmh())+":"+nf.format(edge.getWeightNonTmh());
    }

}
