package input;

import interfaces.Sequence;
import interfaces.SequencePosition;

import java.util.Arrays;
import java.util.Random;

import data.AminoAcid;
import data.Hydrophobicity;
import data.SSE;

public class RandomSequenceGenerator {

	
	public static void main(String[] args) {
		Sequence[] seqs = generate(10);
		
		System.out.println("Generated "+seqs.length+" sequences:");
		System.out.println(Arrays.toString(seqs));
		
		System.out.println();
		
		for(Sequence seq : seqs)
			System.out.println(seq.getId()+"\t=>\t"+Arrays.toString(seq.getSequence()));
	}
	
	
	
	public static Sequence[] generate(int num) {

		AminoAcid[] aas = AminoAcid.values();

		SSE[] sses = SSE.values();

		Random ralf = new Random();

		int scale = ralf.nextInt(6);

		Sequence[] result = new Sequence[num];


		for(int k=0; k<num; k++) {

			//between 50 and 150
			int len = 50 + ralf.nextInt(100)+1;

			SequencePosition[] seqPosArr = new SequencePosition[len];


			for(int i=0; i<len; i++) {
				AminoAcid aa = aas[ralf.nextInt(aas.length)];

				double hydrophobicity = Hydrophobicity.get(aa, scale);

				SSE sse = sses[ralf.nextInt(sses.length)]; 

				SequencePositionImpl seqPos = new SequencePositionImpl(aa, hydrophobicity, sse, scale);

				seqPosArr[i] = seqPos;
			}


			result[k] = new SequenceImpl("#"+k, seqPosArr);

		}

		return result;
	}

}
