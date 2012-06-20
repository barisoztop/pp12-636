package evaluation;

import interfaces.Prediction;
import interfaces.Predictor;
import interfaces.PredictorFactory;
import interfaces.Result;
import interfaces.Sequence;

import java.util.Arrays;
import java.util.LinkedList;


/**
 * tenfold crossvalidation
 * 
 * @author Felix
 *
 */
public class Evaluation {
	
	private Sequence[] sequences;
	private PredictorFactory factory;
	
	
	public Evaluation(Sequence[] sequences, PredictorFactory factory) {
		this.sequences = sequences;
		this.factory = factory;
	}

	
	/**
	 * run the evaluation
	 * @return the result of the evaluation
	 */
	public EvaluationResult evaluate() {
		
		EvaluationRun[] runs = new EvaluationRun[10];
		
		for(int i=0; i<10; i++) {
			Predictor priscilla = factory.getInstance();
			
			//combine the 9 training sets into one big set
			LinkedList<Sequence> trainSet = new LinkedList<Sequence>();
			
			for(int j=0; j<10; j++) {
				if(i==j) continue;
				trainSet.addAll(Arrays.asList(getSet(j)));
			}
			
			Sequence[] trainSetArr = trainSet.toArray(new Sequence[trainSet.size()]);
			
			//train the classifier
			priscilla.train( trainSetArr );
			
			
			//get the test set
			Sequence[] testSet = getSet(i);
			
			
			
			EvaluationRun run = new EvaluationRun(this);
			
			//test!
			//for each test sequence...
			for(int j=0; j<testSet.length; j++) {
				Sequence test = testSet[j];

				//truepositives et cetera for each class
				Statistics tmh = new Statistics();
				Statistics inside = new Statistics();
				Statistics outside = new Statistics();
				
				//test!
				Prediction result = priscilla.predict(test);
				
				//calculate true positives, false positives, false negatives, true negatives for all three classes
				//look at each sequence position...
				for(int r=0; r<test.length(); r++) {
					//get the experimental (real) class, like, the one got from uniprot or the like for reference...
					Result experimental = test.getSequence()[r].getRealClass();
					//...and the one we just predicted
					Result prediction = result.getPredictionForResidue(r);
					
					//this is monster. do the actual calculation:
					if(experimental==Result.TMH && prediction==Result.TMH) {
						//TMH expected, TMH found => true positive
						tmh.addTruePositive();
						//no outside found, none expected => true negative
						outside.addTrueNegative();
						//no inside found, none expected => true negative
						inside.addTrueNegative();
					}
					else if(experimental==Result.TMH && prediction==Result.INSIDE) {
						//tmh expected but none found => false neg
						tmh.addFalseNegative();
						//no outside expected, none found => true neg
						outside.addTrueNegative();
						//no inside expected, none found => true neg
						inside.addFalsePositive();
					}
					//and so on...
					else if(experimental==Result.TMH && prediction==Result.OUTSIDE) {
						tmh.addFalseNegative();
						outside.addFalsePositive();
						inside.addTrueNegative();
					}
					else if(experimental==Result.INSIDE && prediction==Result.TMH) {
						inside.addFalseNegative();
						outside.addTrueNegative();
						tmh.addFalsePositive();
					}
					else if(experimental==Result.INSIDE && prediction==Result.INSIDE) {
						inside.addTruePositive();
						outside.addTrueNegative();
						tmh.addTrueNegative();
					}
					else if(experimental==Result.INSIDE && prediction==Result.OUTSIDE) {
						inside.addFalseNegative();
						outside.addFalsePositive();
						tmh.addTrueNegative();
					}
					else if(experimental==Result.OUTSIDE && prediction==Result.TMH) {
						outside.addFalseNegative();
						inside.addTrueNegative();
						tmh.addFalsePositive();
					}
					else if(experimental==Result.OUTSIDE && prediction==Result.INSIDE) {
						outside.addFalseNegative();
						inside.addFalsePositive();
						tmh.addTrueNegative();
					}
					else if(experimental==Result.OUTSIDE && prediction==Result.OUTSIDE) {
						outside.addTruePositive();
						inside.addTrueNegative();
						tmh.addTrueNegative();
					}
				}
				
				//log the result for this sequence with the EvaluationRun object
				run.logSequence(test, tmh, inside, outside);
				
			}
			
			//add the current run to the appropriate position in the runs array
			runs[i] = run;
		}
		
		//that's it. oh the hackery.
		return new EvaluationResult(runs);
	}
	
	
	/**
	 * tenfold crossvalidation necessitates that the set of test/training cases be divided
	 * into ten subsets. this gets you the ith subset.
	 * 
	 * @param iteration 0-9
	 * @return the corresponding set. if you have, say, 99 sequences in total, the last set will contain only 9, the others 10 sequences, so watch out.
	 */
	public Sequence[] getSet(int iteration) {
		LinkedList<Sequence> set = new LinkedList<Sequence>();
		
		for(int j=0; j<sequences.length; j++) {
			if(j%10==iteration) 
				set.add(sequences[j]);
		}
		
		return set.toArray(new Sequence[set.size()]);
	}
	
	
	/**
	 * 
	 * @return all the training/test case sequence object thingies. all of them.
	 */
	public Sequence[] getAllSequences() {
		return sequences;
	}
}