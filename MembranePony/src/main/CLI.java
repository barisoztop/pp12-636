package main;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class CLI {

	
	
	
	@Option(name="-buildmodel", usage="Build model from given training data and save it to the specified" +
			"output file.")
	private boolean buildModel;

	@Option(name="-evaluate", usage="Evaluate model using given test data and write the results to the" +
			"specified outout file")
	private boolean evaluate;

	@Option(name="-output",usage="Output to this file",metaVar="<FILE>")
	private File output = null;
	
	@Option(name="-train",usage="Training set; either a directory that adheres strictly to the structure of the " +
			"set supplied to us (imp_struct.fasta and sol.fasta will be used), or a fasta file with alignments " +
			"(like imp_struct.fasta); PredictProtein " +
			"output folders must lie in the same directory or in a subdirectory named impOutput or solOutput, else " +
			"PredictProtein will be invoked to compute fresh ones.",
			metaVar="<FILE/DIR>")
	private File train = null; 

	@Option(name="-test",usage="Test set; this must be a fasta file with alignments like imp_struct.fasta, PredictProtein " +
			"output folders must lie in the same directory or in a subdirectory named impOutput or solOutput, else " +
			"PredictProtein will be invoked to compute fresh ones.",
			metaVar="<FILE>")
	private File test = null;
	
	@Option(name="-modelfile",usage="Model file to be used with -test. Must be a file generated via -buildmodel."
			,metaVar="<FILE>")
	private File modelFile = null;
	
	
	public void check() throws CmdLineException {
		if(output==null)
			throw(new CmdLineException("output file is mandatory"));
		
		if(buildModel) {
			if(train==null) throw(new CmdLineException("train is mandatory for buildmodel"));
		}
		
		if(evaluate) {
			if(train==null && modelFile==null) throw(new CmdLineException("either modelfile or train is mandatory"));
			if(test==null) throw(new CmdLineException("test is mandatory for buildmodel"));
		}
	}
	
	public void printUsage() {
		System.out.println("GROUP 636 TRANSMEMBRANE REGION PREDICTOR AND FORTUNE TELLER");
		System.out.println();
		System.out.println("USAGE:");
		System.out.println(" -buildmodel -train <DIR> -output <FILE>");
		System.out.println(" -evaluate (-train <DIR>|-modelfile <FILE>) -test <DIR> -output <FILE>");
		System.out.println();
		System.out.println("PARAMETERS:");
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		parser.printUsage(System.out);
		System.out.println();
		System.out.println("MAGIC EIGHT BALL:");
		System.out.println(" Your answer is:  "+Magic8Ball.shake()+".");
	}
	
	public boolean buildModel() {
		return buildModel;
	}
	
	public boolean evaluate() {
		return evaluate;
	}
	
	public File getTrain() {
		return train;
	}
	
	public File getTest() {
		return test;
	}
	
	public File getOutput() {
		return output;
	}
	
	public File getModelFile() {
		return modelFile;
	}
}
