package input;

import interfaces.Result;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;


/**
 * reads the imp_struct.fasta file
 *
 * @author Felix
 *
 */
public class ReadStruct {

	public static class Struct {

		public final String id;
		/**
		 * full sequence; first line of fasta data
		 */
		public final String wholeSequence;

		/**
		 * possibly abbreviated; the one we'll be using
		 */
		public final String sequence;

		/**
		 * the "structure" information; {@link Result}.TMH and {@link Result}.NON_TH
		 */
		public final Result[] realClasses;

		/**
		 * if the sequences are abbreviated, start and end character of the sequence with respect to wholesequence
		 */
		public final int startOffset, endOffset;

		public Struct(String id, String wholeSequence, String sequence, Result[] realClasses, int startOffset, int endOffset) {
			this.id = id;
			this.wholeSequence = wholeSequence;
			this.sequence = sequence;
			this.realClasses = realClasses;
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}
	}


	public static Struct[] readStructFile(File file) throws IOException {
		LinkedList<Struct> structs = new LinkedList<ReadStruct.Struct>();

		String[] lines = Helpers.readFile(file.getAbsolutePath()).split("\n");

		for(int i=0; i<lines.length; i++) {
			if(lines[i].startsWith(">")) {
				String id = lines[i].split("\\|")[2];

				String wholeSequence = lines[++i];
				String sequence = lines[++i];
				String classString = lines[++i];

				Result[] classes = parseClassesString(classString);

				int startOffset = 0;
				for(int j=0; j<sequence.length(); j++) {
					if(sequence.charAt(j)!=' ') {
						startOffset = j;
						break;
					}
				}

				int endOffset = 0;
				for(int j=sequence.length()-1; j>=0; j--) {
					if(sequence.charAt(j)!=' ') {
						endOffset = j;
						break;
					}
				}

				sequence = sequence.trim();

				structs.add(new Struct(id, wholeSequence, sequence, classes, startOffset, endOffset));
			}
		}

		return structs.toArray(new Struct[]{});
	}


	/**
	 * as per the exercise wording, L and H are TMH, rest is NON_TMH.
	 *
	 * @param classString
	 * @return
	 */
	public static Result[] parseClassesString(String classString) {

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

		/*
		 * For our purpose, we will consider 'L' (membrane loops) and 'H'
		 * (alpha helix) as transmembrane (TM) and the rest as not
		 * transmembrane (Not TM).
		 */

		classString = classString.trim();

		Result[] result = new Result[classString.length()];

		for(int i=0; i<result.length; i++) {
			char c = classString.toUpperCase().charAt(i);

			if(c=='H' || c=='L')
				result[i] = Result.TMH;
			else
				result[i] = Result.NON_TMH;

		}

		return result;
	}

}
