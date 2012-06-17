package input;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class DataReader {
	
	
	public static void main(String[] args) throws IOException {
		
		//list subfolders in data folder
		//for each:
		//new sequence
		//read .in file
		//parse sse
		//fetch uniprot file
		//combine everything
		
		File dataFolder = new File("N:\\temp\\ppdata");
		
		for(File f : dataFolder.listFiles()) {
			if(!f.isDirectory()) continue;
			
			String id = f.getName();
			
			System.out.println(id);
			
			String seq = readFasta(f.getAbsolutePath()+File.separator+"query.in");
						
			System.out.println("sequence         "+seq);
			System.out.println("sequence.len     "+seq.length());
			
			String sse = readProfRdb(f.getAbsolutePath()+File.separator+"query.profRdb", seq);
			
			System.out.println("sse              "+sse);
			System.out.println("sse.len          "+sse.length());
			
			String realClass = fetchUniprotClassification(id);
			
			System.out.println("classes          "+realClass);
			System.out.println("classes.len      "+realClass.length());
			System.out.println();
			System.out.println();
		}
		
	}
	
	
	private static String readFasta(String path) throws IOException {
		String fasta = readFile(path);
		
		String[] lines = fasta.split("\n");
		StringBuilder sb = new StringBuilder();
		for(String line : lines)
			if(!line.startsWith(">"))
				sb.append(line.trim());
		
		return sb.toString();
	}
	
	private static String readProfRdb(String path, String sequence) throws IOException {
		String profrdb = readFile(path);
		
		String[] lines = profrdb.split("\n");
		
//		for(String line : lines) 
//			if(line.startsWith("No")) System.out.println(line);
		
		String seq = "";
		String sses = "";
		
		for(String line : lines) {
			if(line.startsWith("#") || line.startsWith("No")) continue;
			
			String[] cols = line.split("\t");
			
			String aa = cols[1];
			seq+=aa;
						
			String pred = cols[3];
			sses+=pred;
			
//			System.out.println(aa+"\t=>\t"+pred);
		}
		
		if(!seq.equals(sequence)) {
			System.err.println("WARNING: SEQUENCE FROM SSE PROFRDB FILE DOES NOT MATCH FASTA SEQ");
			System.err.println("ProfRdb sequence:");
			System.err.println(seq);
			System.err.println("FASTA sequence:");
			System.err.println(sequence);
			System.err.println("Path: "+path);
		}
				
		return sses;
	}
	
	
	private static String fetchUniprotClassification(String id) throws IOException {
		String urlString = "http://www.uniprot.org/uniprot/"+id+".txt";
		
		String uniprot = readFromURL(urlString);
		
		StringBuilder result = new StringBuilder();
		
		char curClass = '?';
		int curStart = -1;
		
		String[] lines = uniprot.split("\n");
		for(String line : lines) {
			if(line.startsWith("FT")) {
				String[] cols = line.toLowerCase().split("\\s+");
				
				if(cols.length<4)
					continue;
				
//				if(cols[1].equals("chain"))
				if(!cols[1].equals("topo_dom") && !cols[1].equals("transmem"))
					continue;
				
				int start, end;
				try {
					//corrected for first index 0
					start = Integer.parseInt(cols[2]) - 1 ;
					end = Integer.parseInt(cols[3]) - 1 ;
				}
				catch(NumberFormatException e) {
					continue;
				}
				
//				System.out.println(Arrays.toString(cols));
				
				if(curStart == -1) curStart = start;
				
				char cls = '?';
				if(cols.length>=5) {
					if(cols[4].contains("cytoplasmic"))
						cls = 'I';
					else if(cols[1].contains("transmem"))
						cls = 'T';
					else if(cols[4].contains("extracellular"))
						cls = 'O';
				}
				
				if(cls!=curClass && cls!='?') {
					curClass = cls;
				}
				
//				System.out.println("CLASS="+curClass);
				for(int i=0; i<=end-start; i++) {
//					System.out.print(curClass);
					result.append(curClass);
				}
//				System.out.println();
				
				
			}
		}
		
		return result.toString();
	}
	
	
	private static String readFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		
		StringBuilder sb = new StringBuilder();
		
		int c;
		while( (c = br.read() ) != -1 )
			sb.append((char)c);
		
		return sb.toString();
		
	}
	
	
	private static String readFromURL(String urlString) throws IOException {
		URL url;
		InputStream is = null;
		DataInputStream dis;
		String line;

		StringBuilder result = new StringBuilder();
		
		try {
		    url = new URL(urlString);
		    is = url.openStream();
		    dis = new DataInputStream(new BufferedInputStream(is));
		    
		    int c;
		    
		    while ((c = dis.read()) != -1) {
		        result.append((char)c);		        
		    }

		} finally {
		    try {
		        is.close();
		    } catch (IOException ioe) {
		        // nothing to see here
		    }
		}
		
		return result.toString();
	}
}
