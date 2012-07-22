package input;

import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import data.AminoAcid;
import data.Hydrophobicity;
import data.SSE;

public class UniversalDataReader {
	
	private static Logger logger = Logger.getLogger(UniversalDataReader.class);
	
	public static void main(String[] args) throws IOException {
//		Sequence[] s1 = readDatabase(new File("N:/temp/lean-dataset/imp_struct.fasta"), 1);
//		
//		System.out.println(Arrays.toString(s1));
//		
//		Sequence[] s2 = readDatabase(new File("N:/temp/lean-dataset/sol.fasta"), 1);
//		
//		System.out.println(Arrays.toString(s2));
		
		Sequence[] s3 = readDatabase(new File(args[0]), 1);
		System.out.println(Arrays.toString(s3));
		
		for(Sequence s : s3)
			System.out.println(s.getId()+"\n"+s.getSequence()+"\n");
	}
	
	
	
	public static Sequence[] readDatabase(File databaseFile, int table) throws IOException {
		LinkedList<Sequence> sequencesList = new LinkedList<Sequence>();
		
		BufferedReader br = new BufferedReader(new FileReader(databaseFile));
		
		String line;
		String next = br.readLine();
		
		while( next!=null ) {
			line = next;
			next = br.readLine();
			
			if(line.startsWith(">")) {
				//>sp|P21396|AOFA_RAT|NCBI_TaxID=10116(rodents)|PDB:1O5W:A/B/C/D
				String id = line.replaceAll("^>","");
				String[] splits = line.split("\\|");
				if(splits.length >= 3)
					id = splits[2];
				
				String sequence = next.trim();
				
				String path = getPredictProteinPath(databaseFile, id);
				
				if(path==null) {
					runPP(databaseFile.getAbsoluteFile().getParentFile().getAbsolutePath()+File.separator+id, id, sequence);
					
					path = getPredictProteinPath(databaseFile, id);
				}
				
				String sse = DataReader.readProfRdb(path+File.separator+"query.profRdb", sequence);
				
				LinkedList<String> alignment = new LinkedList<String>();
				
				while(next != null && !next.matches("\\s*") && !next.startsWith(">")) {
					alignment.add(next);
					
					next = br.readLine();
				}
				
				if(alignment.size()==1) {
					//soluble
					
					LinkedList<SequencePosition> seqPos = new LinkedList<SequencePosition>();
					
					for(int i=0; i<sequence.length(); i++) {
						//TODO continue if invalid aa
						
						AminoAcid aa = null;
						try {
							 aa = AminoAcid.valueOf(sequence.charAt(i)+"");
						} catch(Exception e) {logger.debug("skipping aa "+sequence.charAt(i)); continue;}
						
						SSE ssePos = SSE.forProfRdb(sse.charAt(i));
						
						double hydrophobicity = Hydrophobicity.get(aa, table);
						
						seqPos.add(new SequencePositionImpl(aa, hydrophobicity, ssePos, table, Result.NON_TMH));
					}
					
					sequencesList.add(new SequenceImpl(id, seqPos.toArray(new SequencePosition[]{})));
				}
				else if(alignment.size()==3) {
					//tmh
					
					String shortSeq = alignment.get(1);
					
					int startOffset = 0;
					for(int j=0; j<shortSeq.length(); j++) {
						if(shortSeq.charAt(j)!=' ') {
							startOffset = j;
							break;
						}
					}

					int endOffset = 0;
					for(int j=shortSeq.length()-1; j>=0; j--) {
						if(shortSeq.charAt(j)!=' ') {
							endOffset = j;
							break;
						}
					}
					
					shortSeq = shortSeq.trim();
					
					String shortSSE = sse.substring(startOffset, endOffset + 1);
					
					LinkedList<SequencePosition> seqPos = new LinkedList<SequencePosition>();
					
					String realClassesString = alignment.get(2).trim();
					Result[] realClasses = ReadStruct.parseClassesString(realClassesString);
					
					for(int i=0; i<shortSeq.length(); i++) {
						//TODO continue if invalid aa
						
						AminoAcid aa = null;
						try {
							 aa = AminoAcid.valueOf(shortSeq.charAt(i)+"");
						} catch(Exception e) {System.err.println("skipping aa "+shortSeq.charAt(i)); continue;}
						
						SSE ssePos = SSE.forProfRdb(shortSSE.charAt(i));
						
						double hydrophobicity = Hydrophobicity.get(aa, table);
						
						seqPos.add(new SequencePositionImpl(aa, hydrophobicity, ssePos, table, realClasses[i]));
					}
					
					sequencesList.add(new SequenceImpl(id, seqPos.toArray(new SequencePosition[]{})));
				}
				else
					throw(new RuntimeException("Cannot parse alignment for "+id+": "+alignment));
				
				
			}
		}
		
		return sequencesList.toArray(new Sequence[] {});
	}
	

	private static void runPP(String string, String id, String sequence) throws IOException {
		new File(string).mkdirs();

		BufferedWriter bw = new BufferedWriter(new FileWriter(string+"/query.fasta"));
		bw.write(">"+id+"\n");
		bw.write(sequence+"\n");
		bw.close();

		String cmd = "/usr/bin/predictprotein " +
				"--seqfile "+string+"/query.fasta " +
				"--target=all " +
				"--target=optional " +
				"--output-dir "+string+" " +
				"--nouse-cache";

		System.out.println(cmd);
		
		Process p = Runtime.getRuntime().exec(cmd);

		System.out.println("started");
		
		BufferedReader brCleanUp =
				new BufferedReader (new InputStreamReader (p.getInputStream()));
		String line;
		while ((line = brCleanUp.readLine ()) != null) {
			logger.debug ("[PredictProtein OUT] " + line);
		}
		brCleanUp.close();
		
		brCleanUp =
				new BufferedReader (new InputStreamReader (p.getErrorStream()));
		while ((line = brCleanUp.readLine ()) != null) {
			logger.debug ("[PredictProtein ERR] " + line);
			System.err.println("[PredictProtein ERR] " + line);
		}
		brCleanUp.close();
	}



	private static String getPredictProteinPath(File databaseFile, String id) {
		File wd = databaseFile.getAbsoluteFile().getParentFile();
		
		if(new File(wd.getAbsolutePath()+File.separator+id).exists())
			return wd.getAbsolutePath()+File.separator+id;
		
		if(new File(wd.getAbsolutePath()+File.separator+"impOutput"+File.separator+id).exists())
			return wd.getAbsolutePath()+File.separator+"impOutput"+File.separator+id;
		
		if(new File(wd.getAbsolutePath()+File.separator+"solOutput"+File.separator+id).exists())
			return wd.getAbsolutePath()+File.separator+"solOutput"+File.separator+id;
		
		if(new File("/tmp/g636pp/"+id).exists())
			return "/tmp/g636pp/"+id;
		
		return null;
	}

	
}
