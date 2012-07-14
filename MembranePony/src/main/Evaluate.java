package main;

import input.DataReader;
import interfaces.Sequence;

import java.io.File;
import java.io.IOException;

import predictor.markov.MarkovPredictorFactory;

import data.Hydrophobicity;
import evaluation.Evaluation;
import evaluation.EvaluationResult;

public class Evaluate {

	public static void main(String[] args) throws IOException {

		File dataFolder = new File("N:\\temp\\lean-dataset\\");
		int table = Hydrophobicity.KYTE_DOOLITTLE;

		Sequence[] sequences = DataReader.readAll(dataFolder, table, true);

		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());

		EvaluationResult result = eval.evaluate();

		System.out.println(result);

	}

}
