package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ParseIn {
	
	public static void main (String[] args) throws IOException {
		String SS=parse("/Users/zuzi4ka/Dropbox/SS2012/Protein Prediction/ACCN2_CHICK/query.in");
		System.out.println(SS);
	}
	
	
	public static String parse(String file) throws IOException{
		BufferedReader bruni = new BufferedReader(new FileReader(file));
		String line;
		String seq="";
		
		
		
		while ((line=bruni.readLine())!=null){
			if(line.startsWith(">")) continue;
			
			seq+=line;
				
			
		}
//		System.out.println(SSE);
		return seq;
	}
}
