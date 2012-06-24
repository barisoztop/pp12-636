package markov.layout;

import de.intelliad.attributionmodel.data.LineElement;
import de.intelliad.attributionmodel.markov.graph.MarkovEdge;
import de.intelliad.attributionmodel.markov.graph.MarkovGraph;
import de.intelliad.attributionmodel.markov.graph.MarkovVertex;
import de.intelliad.attributionmodel.markov.layout.Markov;
import de.intelliad.attributionmodel.markov.layout.MarkovGroup;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * implementation of the classification rate of a given testset of userjourneys<br>
 * provides a basic insight of the quality of the trained markovGraph<br>
 * the walked edgeweights of the training userjourneys are saved and
 * the min, max, mean and std. dev are computed. this is done by using the simple naive bayes rule
 *
 * @author rgreil
 */
public class ValidatorClassificationRate extends Validator {

    Map<Integer, SummaryStatistics> mapStats = new HashMap<>();
    Map<Integer, Integer[]> mapTrainTest = new HashMap<>();

    public ValidatorClassificationRate(MarkovGroup mg) {
        this.mg = mg;
    }

    @Override
    public void validate(Markov markovGraph) {
        int tested = 0;
//        DefaultDirectedWeightedGraph<MarkovVertex, DefaultWeightedEdge> graph = markovGraph.getGraph();
        MarkovGraph<MarkovVertex, MarkovEdge> graph = markovGraph.getGraph();
        SummaryStatistics sumStat = new SummaryStatistics();
        int journeyLength = markovGraph.getJourneyLength();

        for (List<LineElement> list : testList) {
            //TODO: check!
            if (list.size() < journeyLength) {
                continue;
            } else if (list.size() > journeyLength) {
                continue;
            }

            List<MarkovVertex> journey = markovGraph.convertLineElementsToCompleteJourney(list);
            double a = 1d;
            double b = 1d;
            for (int i = 0; i < journey.size() - 1; i++) {
                MarkovVertex source = journey.get(i);
                MarkovVertex target = journey.get(i + 1);
//                DefaultWeightedEdge dwe = graph.getEdge(source, target);
                MarkovEdge markovEdge = graph.getEdge(source, target);
                double weight = graph.getEdgeWeight(markovEdge);
                a *= weight;
                b *= (1 - weight);
            }
            //bayes rule
            //http://paulgraham.com/naivebayes.html
            double classificationProbability = a / (a + b);
            sumStat.addValue(classificationProbability);
            tested++;
        }
        mapStats.put(journeyLength, sumStat);
        mapTrainTest.put(journeyLength, new Integer[]{markovGraph.getTrained(), tested});
    }


    @Override
    public void printResultFormattedForExcel() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(1);
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(5);
        StringBuilder sb = new StringBuilder();
//        sb.append("\nlength\tmin\tmean\tmax\tstd.dev\n");
        sb.append("\nlength\ttrained\ttested\tmin\tmean\tmax\tstd.dev\n");
        for (Integer l : mapStats.keySet()) {
            SummaryStatistics stat = mapStats.get(l);
            Integer[] ar = mapTrainTest.get(l);
            sb.append(l).append("\t");
            sb.append(ar[0]).append("\t");
            sb.append(ar[1]).append("\t");
            sb.append(nf.format(stat.getMin())).append("\t");
            sb.append(nf.format(stat.getMean())).append("\t");
            sb.append(nf.format(stat.getMax())).append("\t");
            sb.append(nf.format(stat.getStandardDeviation())).append("\n");
        }
        logger.info(sb);
    }
}
