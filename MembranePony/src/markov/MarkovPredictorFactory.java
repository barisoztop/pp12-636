package markov;

import interfaces.Predictor;
import interfaces.PredictorFactory;
import markov.layout.CombinedNet;
import markov.layout.Markov;

/**
 * predictor factory for markov predictor. everyone except robert should use
 * this to spawn new instances of the predictor class.
 *
 * @author Felix
 *
 */
public class MarkovPredictorFactory implements PredictorFactory {

    private Double range = null;

    @Override
    public Predictor getInstance() {
        Markov mareike = new CombinedNet();
//        Markov mareike = new TripleNet();

        if (range != null) {
            mareike.setMappingContValuesToNodes(range);
        }

        return mareike;
    }

    //TODO please add some real documentation here
    /**
     * Performs arcane magic, not the hat-and-bunny-sort of magic, but real,
     * steak-frying, fries-salting, salad-munching kind. The one that transforms
     * a boring BBQ into a veritable munchfest.
     *
     * @param range
     */
    public void setRangeForMappingContinousValuesToNodes(double range) {
        this.range = range;
    }
}
