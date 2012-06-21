package input;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Helpers {

	public static String readFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(path));
		
		StringBuilder sb = new StringBuilder();
		
		int c;
		while( (c = br.read() ) != -1 )
			sb.append((char)c);
		
		return sb.toString();
		
	}
	
	
	public static String readFromURL(String urlString) throws IOException {
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
