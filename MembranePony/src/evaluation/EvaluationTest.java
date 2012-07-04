package evaluation;

import static org.junit.Assert.assertEquals;
import input.RandomSequenceGenerator;
import interfaces.GenericPrediction;
import interfaces.Prediction;
import interfaces.Predictor;
import interfaces.PredictorFactory;
import interfaces.Result;
import interfaces.Sequence;

import java.io.File;

import org.junit.Test;

public class EvaluationTest {

	
	@Test
	public void correctTest() {
		
		Sequence[] sequences = RandomSequenceGenerator.generate(25);
		
		Evaluation eval = new Evaluation(sequences, new TestFactory(CorrectPredictor.class));
		
		EvaluationResult result = eval.evaluate();
		
		assertEquals(1.0, result.getStatisticsTMH().getPrecision(), 0);
		assertEquals(1.0, result.getStatisticsTMH().getRecall(), 0);
		assertEquals(1.0, result.getStatisticsTMH().getSpecificity(), 0);
		assertEquals(1.0, result.getStatisticsTMH().getFMeasure(), 0);
		
		
		
	}

	
	
	
	
	public static class CorrectPredictor extends TestPredictor {
		@Override
		public Prediction predict(Sequence sequence) {
			Result[] results = new Result[sequence.length()];
			for(int i=0; i<sequence.length(); i++)
				results[i] = sequence.getSequence()[i].getRealClass();
			return new GenericPrediction(sequence, results);
		}
		
	}
	
	
	
	
	
	
}








class TestFactory implements PredictorFactory {
	private Class<?> clazz;

	public TestFactory(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public Predictor getInstance() {
		try {
			return (Predictor) clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}

abstract class TestPredictor implements Predictor {
	@Override
	public void train(Sequence[] trainingCases) {}
	@Override
	public void save(File model) throws Exception {}
	@Override
	public void load(File model) throws Exception {}		
}

