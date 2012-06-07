package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ParseProfAscii {
	
	public static void main (String[] args) throws IOException {
		String seq = ParseIn.parse("/Users/zuzi4ka/Dropbox/SS2012/Protein Prediction/ACCN2_CHICK/query.in");
		System.out.println(seq);
		
		String SS=parse("/Users/zuzi4ka/Dropbox/SS2012/Protein Prediction/ACCN2_CHICK/query.profAscii");
		System.out.println(SS);
		
	}
	
	
	public static String parse(String file) throws IOException{
		BufferedReader bruni = new BufferedReader(new FileReader(file));
		String line;
		String SSE="";
		String Sequence="";
		
		
		
		while ((line=bruni.readLine())!=null){
			System.out.println(line);
			if(line.matches("^\\s+PROF_sec\\s+\\|.*")){
				
				line=line.trim();
				int pos = line.indexOf("|")+1;
				int pos2 = line.indexOf("|", pos);
				String subStr=line.substring(pos,pos2);
//				System.out.println(subStr);
				SSE+=subStr;
				
			}
			else if (line.matches("^\\s+AA\\s+\\|.*")){
				line=line.trim();
				int pos = line.indexOf("|")+1;
				int pos2 = line.indexOf("|", pos);
				String subStr=line.substring(pos,pos2);
				Sequence+=subStr;
			}
			else if (line.startsWith("TIE|")){
				break;
				
			}
		}
	System.out.println(Sequence);
		return SSE;
	}
}
