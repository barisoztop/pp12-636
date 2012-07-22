package main;

import input.DataReader;
import input.UniversalDataReader;
import interfaces.Predictor;
import interfaces.PredictorFactory;
import interfaces.Sequence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import predictor.markov.MarkovPredictorFactory;

import data.Hydrophobicity;
import evaluation.BoringEvaluation;
import evaluation.EvaluationRun;
import evaluation.TestCase;


public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class);
	
	public static PredictorFactory predictorFactory = new MarkovPredictorFactory();
	public static int table = Hydrophobicity.KYTE_DOOLITTLE;
	
	
	
public static void main(String[] args) throws Exception {
		
//		args = new String[] { "-buildmodel", "-output", "test", "-train", "n:/temp/lean-dataset/imp_struct.fasta" };
//		args = new String[] { "-evaluate", "-modelfile", "test", "-output", "test2", "-test", "n:/temp/lean-dataset/imp_struct.fasta" };
		
		CLI cli = new CLI();
		
		if(args.length==0) {
			cli.printUsage();
			System.exit(0);
		}
		
		CmdLineParser parser = new CmdLineParser(cli);
		
		parser.setUsageWidth(80);
		
		try {
			parser.parseArgument(args);
			
			cli.check();
			
			Main.run(cli);
			
			return;

		} catch( CmdLineException e ) {
			System.out.println(e.getLocalizedMessage());
			System.out.println();
			
			return;
		} 
	}
	
	
	
	

	public static void run(CLI cli) throws Exception {
		
		
		if(cli.buildModel()) {
			System.out.println("Building model");
			
			Predictor pred = train(cli.getTrain());
			pred.save(cli.getOutput());
		}
		else if(cli.evaluate()) {
			PrintWriter out = new PrintWriter(new FileWriter(cli.getOutput()));
			
			System.out.println("Evaluating...\n");			
			
			Predictor pred = null;
			
			if(cli.getModelFile()!=null) {
				pred = new MarkovPredictorFactory().getInstance();
				pred.load(cli.getModelFile());
			}
			else {
				pred = train(cli.getTrain());
			}
			
			Sequence[] testSet =  UniversalDataReader.readDatabase(cli.getTest(), table);
			
			BoringEvaluation eval = new BoringEvaluation(pred, testSet);
			
			EvaluationRun run = eval.evaluate();
			
			System.out.println("EVALUATION RESULTS\n\n");
			out.append("EVALUATION RESULTS\n\n");
			
			for(TestCase tc : run.getTestCases()) {
				System.out.println(tc+"\n");
				out.println(tc+"\n");
			}
			
			//write output to $out
			System.out.println();
			out.println();
			System.out.println();
			out.println();
			
			System.out.println("GLOBAL STATISTICS");
			out.println("GLOBAL STATISTICS");
			
			System.out.println();
			out.println();
			
			System.out.println("Transmembrane:       "+run.getRunStatisticsTMH());
			out.println("Transmembrane:       "+run.getRunStatisticsTMH());
			
			System.out.println("Non-Transmembrane:   "+run.getRunStatisticsNonTMH());
			out.println("Non-Transmembrane:   "+run.getRunStatisticsNonTMH());
			
			
			out.flush();
			out.close();
		}
		
	}

	
	private static Predictor train(File trainset) throws IOException {
		Sequence[] sequences = null;
		
		try {
			if(trainset.isDirectory()) {
				//tmh
				Sequence[] sequences1 = UniversalDataReader.readDatabase(new File(trainset.getAbsolutePath()+File.separator+"imp_struct.fasta"), table);
				//solubles
				Sequence[] sequences2 = UniversalDataReader.readDatabase(new File(trainset.getAbsolutePath()+File.separator+"sol.fasta"), table);
				
				ArrayList<Sequence> temp = new ArrayList<Sequence>();
				temp.addAll(Arrays.asList(sequences1));
				temp.addAll(Arrays.asList(sequences2));
				
				sequences = temp.toArray(new Sequence[] {});
			}
			else {
				sequences = UniversalDataReader.readDatabase(trainset, table);
			}
		} catch (IOException e) {
			System.err.println("An error occurred while trying to read the training set folder '"+trainset+"'");
			throw(e);
		}
		
		Predictor pred = predictorFactory.getInstance();
		pred.train(sequences);
		
		return pred;
	}
	
	
	
}
