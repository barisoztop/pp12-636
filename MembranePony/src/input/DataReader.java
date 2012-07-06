package input;

import input.ReadStruct.Struct;
import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import data.AminoAcid;
import data.Hydrophobicity;
import data.SSE;
import java.util.Arrays;

/**
 * reads the dataset for training and testing.<p> currently works for impOutput
 * only, no solubles
 *
 * @author Felix
 *
 */
public class DataReader {

	/*
	 * The type of REGION can be 1, 2, B, H, C, I, L and U for
	 * Side1, Side2, Beta-strand, alpha-helix, coil, membrane-inside,
	 * membrane-loop and unknown localizations, respectively. Side1
	 * and Side2 refers to the two sides of the membrane (we do not
	 * know which is inside or outside using the information only from
	 * the pdb file). Membrane-inside is the inside part of a beta
	 * barrel. Membrane- loop correspond to a region of the polypeptide
	 * chain which does not cross the membrane, just dips into the
	 * membrane (for example in aquaporins or potassium-channels).
	 */
	private static Logger logger = Logger.getLogger(DataReader.class);

	public static void main(String[] args) throws IOException {

//		File dataFolder = new File("N:\\temp\\ppdata\\mini-dataset\\impOutput");
//		File structFile = new File("N:\\temp\\ppdata\\lean-dataset\\imp_struct.fasta");
		int table = Hydrophobicity.KYTE_DOOLITTLE;

//		readSequences(dataFolder, structFile, table);

//		readAll(new File("N:\\temp\\lean-dataset\\"), table, true);
		readAll(new File("/Users/felixsappelt/temp/lean-dataset"), table, true);

	}

	/**
	 * solubles and transmembranes
	 *
	 * @param dataset
	 * @param hydrophobicityTable
	 * @return
	 * @throws IOException
	 */
	public static Sequence[] readAll(File dataset, int hydrophobicityTable, boolean hydroWindow) throws IOException {
		File dataFolder = new File(dataset.getAbsolutePath() + File.separator + "impOutput");
		File structFile = new File(dataset.getAbsolutePath() + File.separator + "imp_struct.fasta");

		LinkedList<Sequence> result = new LinkedList<Sequence>();

		Sequence[] structseqs = readTransmembranes(dataFolder, structFile, hydrophobicityTable, hydroWindow);
		result.addAll(Arrays.asList(structseqs));

		File solublesFolder = new File(dataset.getAbsolutePath() + File.separator + "solOutput");

		Sequence[] solseqs = readSolubles(solublesFolder, hydrophobicityTable, hydroWindow);
		result.addAll(Arrays.asList(solseqs));

		logger.info("TOTAL: Read " + structseqs.length + " transmembranes and " + solseqs.length + " solubles.");

		Sequence[] sequences = result.toArray(new Sequence[]{});

		//done in the other two methods already
//		if(hydroWindow)
//			hydrophobicityWindowCalculation(sequences);

		return sequences;
	}

	public static Sequence[] readSolubles(File solublesFolder, int hydrophobicityTable, boolean hydroWindow) throws IOException {
		LinkedList<Sequence> result = new LinkedList<Sequence>();

		for (File f : solublesFolder.listFiles()) {
			if (f.isDirectory()) {
				String id = f.getName();

				logger.info("Reading SOLUBLE sequence " + id + "...");

				String sequence = readFasta(f.getAbsolutePath() + File.separator + "query.fasta");
				String sse = readProfRdb(f.getAbsolutePath() + File.separator + "query.profRdb", sequence);

				LinkedList<SequencePosition> seqPos = new LinkedList<SequencePosition>();
				for (int i = 0; i < sequence.length(); i++) {
					AminoAcid aa;
					try {
						aa = AminoAcid.valueOf(sequence.charAt(i) + "");
					} catch (Exception e) {
						continue;
					}
					SSE secstr = SSE.forProfRdb(sse.charAt(i));
					double hydrophobicity = Hydrophobicity.get(aa, hydrophobicityTable);
					Result realClass = Result.NON_TMH;

					seqPos.add(new SequencePositionImpl(aa, hydrophobicity, secstr, hydrophobicityTable, realClass));


				}

				logger.trace("|- whole seq        " + sequence);
				logger.trace("|- whole sse        " + sse);
				logger.trace("|- whole seq.len    " + sequence.length());
				logger.trace("|- whole sse.len    " + sse.length());

				Sequence s = new SequenceImpl(id, seqPos.toArray(new SequencePosition[]{}));
				result.add(s);
			}
		}

		Sequence[] sequences = result.toArray(new Sequence[]{});

		if (hydroWindow) {
			hydrophobicityWindowCalculation(sequences);
		}

		return sequences;
	}

	/**
	 *
	 * @param dataFolder either solOutput or impOutput folder
	 * @param structFile the imp_struct.fasta file
	 * @param hydrophobiticyTable what table to use for hydrophobicity
	 * ({@link Hydrophobicity}.*)
	 * @return a Sequence object for each protein found in the given folder
	 *
	 * @throws IOException
	 */
	public static Sequence[] readTransmembranes(File dataFolder, File structFile, int hydrophobiticyTable, boolean hydroWindow) throws IOException {

		logger.debug("readSequences dataFolder=>" + dataFolder + " structFile=>" + structFile + " hydroTab=>" + hydrophobiticyTable);


		LinkedList<Sequence> sequences = new LinkedList<Sequence>();


		Struct[] structs = ReadStruct.readStructFile(structFile);

		HashMap<String, Struct> structsMap = new HashMap<String, ReadStruct.Struct>();

		for (Struct s : structs) {
			structsMap.put(s.id, s);
		}

		logger.info("Read " + structs.length + " Structs from file.");

		int processed = 0;

		for (File f : dataFolder.listFiles()) {
			try {
				if (!f.isDirectory()) {
					continue;
				}

				processed++;


				String id = f.getName();

				logger.info("Reading sequence " + id + "...");

				Struct struct = structsMap.get(id);
				if (struct == null) {
					logger.warn("NO STRUCT FOR ID=" + id);
				}

				String wholeseq = struct.wholeSequence;
				logger.trace("|- whole seq        " + wholeseq);

				String wholesse = readProfRdb(f.getAbsolutePath() + File.separator + "query.profRdb", wholeseq);
				logger.trace("|- whole sse        " + wholesse);

				if (wholeseq.length() != wholesse.length()) {
					throw (new IllegalStateException("Whole sequence length differs from whole sse length ("
							+ wholeseq.length() + " <> " + wholesse.length() + ")"));
				}

				String seq = struct.sequence;

				String temp = "";
				temp += "|- seq              ";
				for (int i = 0; i < struct.startOffset; i++) {
					temp += ".";
				}
				temp += seq;
				for (int i = 1; i < wholeseq.length() - struct.endOffset; i++) {
					temp += ".";
				}
				logger.trace(temp);

				temp = "";

				String sse = wholesse.substring(struct.startOffset, struct.endOffset + 1);

				temp += "|- sse              ";
				for (int i = 0; i < struct.startOffset; i++) {
					temp += ".";
				}
				temp += sse;
				for (int i = 1; i < wholeseq.length() - struct.endOffset; i++) {
					temp += ".";
				}
				logger.trace(temp);



				logger.trace("|- whole seq.len    " + wholeseq.length());
				logger.trace("|- whole sse.len    " + wholesse.length());
				logger.trace("|- seq.len          " + seq.length());
				logger.trace("|- sse.len          " + sse.length());


				if (seq.length() != sse.length()) {
					throw (new IllegalStateException("Sequence length differs from sse length (" + seq.length() + " <> " + sse.length() + ")"));
				}


//				SequencePosition[] seqpos = new SequencePosition[seq.length()];
				LinkedList<SequencePosition> seqPos = new LinkedList<SequencePosition>();

				for (int i = 0; i < seq.length(); i++) {
					if (seq.charAt(i) == ' ') {
						logger.trace("CHAR SPACE " + i + " => " + seq);
						continue;
					}

					if (seq.charAt(i) == 'X') {
						logger.trace("CHAR X " + i + " => " + seq);
						continue;
					}

					AminoAcid aa = AminoAcid.valueOf(seq.charAt(i) + "");
					SSE secstr = SSE.forProfRdb(sse.charAt(i));
					double hydrophobicity = Hydrophobicity.get(aa, hydrophobiticyTable);
					Result realClass = struct.realClasses[i];

					seqPos.add(new SequencePositionImpl(aa, hydrophobicity, secstr, hydrophobiticyTable, realClass));
				}


				//remove trailing 'null' elements from seqpos:



				Sequence s = new SequenceImpl(id, seqPos.toArray(new SequencePosition[]{}));
				sequences.add(s);


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		logger.info("Processed membrane protein directories: " + processed);

		Sequence[] sequencesArr = sequences.toArray(new Sequence[]{});

		if (hydroWindow) {
			hydrophobicityWindowCalculation(sequencesArr);
		}

		return sequencesArr;
	}

	private static void hydrophobicityWindowCalculation(Sequence[] sequences) {
		logger.info("Performing hydrophobicity window calculation on " + sequences.length + " sequences.");

		for (SequencePosition p : sequences[0].getSequence()) {
			System.out.print(Math.round(p.getHydrophobicity() * 1000) / 1000d + "\t");
		}

		System.out.println();



		for (Sequence s : sequences) {
			double[] hydrophobicity = new double[s.length()];
			for (int i = 0; i < s.length(); i++) {
				hydrophobicity[i] = s.getSequence()[i].getHydrophobicity();
			}
//			System.out.println();

			for (int i = 0; i < s.length(); i++) {
				SequencePositionImpl pos = (SequencePositionImpl) s.getSequence()[i];

				int divisor = 1;
				double hydrophobicitySum = hydrophobicity[i];

				if (i > 0) {
					hydrophobicitySum += hydrophobicity[i - 1];
					divisor++;
				}

				if (i < s.length() - 1) {
					hydrophobicitySum += hydrophobicity[i + 1];
					divisor++;
				}

				double hydroNew = hydrophobicitySum / (double) divisor;

				pos.setHydrophobicity(hydroNew);
			}
		}


//		for(SequencePosition p : sequences[0].getSequence())
//			System.out.print(Math.round(p.getHydrophobicity()*1000)/1000d+"\t");
//		System.out.println();
	}

	private static String readProfRdb(String path, String sequence) throws IOException {
		String profrdb = Helpers.readFile(path);

		String[] lines = profrdb.split("\n");

		String seq = "";
		String sses = "";

		for (String line : lines) {
			if (line.startsWith("#") || line.startsWith("No")) {
				continue;
			}

			String[] cols = line.split("\t");

			String aa = cols[1];
			seq += aa;

			String pred = cols[3];
			sses += pred;

		}

		if (!seq.replaceAll("[UX]", "").equals(sequence.replaceAll("[UX]", ""))) {
			System.err.println("WARNING: SEQUENCE FROM SSE PROFRDB FILE DOES NOT MATCH FASTA SEQ");
			System.err.println("ProfRdb sequence:");
			System.err.println(seq);
			System.err.println("FASTA sequence:");
			System.err.println(sequence);
			System.err.println("Path: " + path);
		}

		return sses;
	}

	private static String readFasta(String path) throws IOException {
		String fasta = Helpers.readFile(path);

		String[] lines = fasta.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			if (!line.startsWith(">")) {
				sb.append(line.replaceAll("\\s+", ""));
			}
		}

		return sb.toString();
	}
//	private static String fetchUniprotClassification(String id, String sequence) throws IOException {
//		String urlString = "http://www.uniprot.org/uniprot/"+id+".txt";
//
//		String uniprot = Helpers.readFromURL(urlString);
////		System.out.println(uniprot);
//		StringBuilder result = new StringBuilder();
//
//		char curClass = '?';
//		int curStart = -1;
//
//		String[] lines = uniprot.split("\n");
//		for(String line : lines) {
//			if(line.startsWith("FT")) {
//				String[] cols = line.toLowerCase().split("\\s+");
//
//				if(cols.length<4)
//					continue;
//
////				if(cols[1].equals("chain"))
//				if(!cols[1].equals("topo_dom") && !cols[1].equals("transmem"))
//					continue;
//
//				int start, end;
//				try {
//					//corrected for first index 0
//					start = Integer.parseInt(cols[2]) - 1 ;
//					end = Integer.parseInt(cols[3]) - 1 ;
//				}
//				catch(NumberFormatException e) {
//					continue;
//				}
//
////				System.out.println(Arrays.toString(cols));
//
//				if(curStart == -1) curStart = start;
//
//				char cls = '?';
//				if(cols.length>=5) {
//					if(cols[4].contains("cytoplasmic"))
//						cls = 'I';
//					else if(cols[1].contains("transmem"))
//						cls = 'T';
//					else if(cols[4].contains("extracellular"))
//						cls = 'O';
//				}
//
//				if(cls!=curClass && cls!='?') {
//					curClass = cls;
//				}
//
////				System.out.println("CLASS="+curClass);
//				for(int i=0; i<=end-start; i++) {
////					System.out.print(curClass);
//					result.append(curClass);
//				}
////				System.out.println();
//
//
//			}
//		}
//
//		String seq = "";
//		for(int i=0; i<lines.length; i++) {
//			if(lines[i].startsWith("SQ")) {
//				i++;
//
//				while(i<lines.length && !lines[i].startsWith("//")) {
//					seq += lines[i];
//					i++;
//				}
//
//				seq = seq.replaceAll("\\s", "");
//			}
//		}
//
//		if(!seq.equals(sequence)) {
//			System.err.println("WARNING: SEQUENCE FROM UNIPROT DOES NOT MATCH FASTA SEQ");
//			System.err.println("Uniprot sequence:");
//			System.err.println(seq);
//			System.err.println("FASTA sequence:");
//			System.err.println(sequence);
//			System.err.println("Id: "+id);
//		}
//
//
//		return result.toString();
//	}
}
