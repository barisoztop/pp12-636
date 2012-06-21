package main;

import input.DataReader;
import interfaces.Sequence;

import java.io.File;
import java.io.IOException;

import markov.MarkovPredictorFactory;

import data.Hydrophobicity;
import evaluation.Evaluation;
import evaluation.EvaluationResult;

public class Evaluate {
	
	public static void main(String[] args) throws IOException {
			
		File dataFolder = new File("N:\\temp\\ppdata\\dataset\\impOutput");
		File structFile = new File("N:\\temp\\ppdata\\dataset\\imp_struct.fasta");
		int table = Hydrophobicity.KYTE_DOOLITTLE;
		
		Sequence[] sequences = DataReader.readSequences(dataFolder, structFile, table);
		
		Evaluation eval = new Evaluation(sequences, new MarkovPredictorFactory());
		
		EvaluationResult result = eval.evaluate();
		
		System.out.println(result);
		
	}
	
}
