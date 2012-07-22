package input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestExec {
	public static void main(String[] args) throws IOException {
		Process p = Runtime.getRuntime().exec("ruby -e \"10.times do puts 1; sleep 1 end\"");
		
		int c;
		while( (c=p.getInputStream().read())!=-1)
			System.out.print((char)c);
		
		System.out.println("started");
		
		BufferedReader brCleanUp =
				new BufferedReader (new InputStreamReader (p.getInputStream()));
		String line;
		while ((line = brCleanUp.readLine ()) != null) {
			System.out.println(line);
		}
		brCleanUp.close();
	}
}
