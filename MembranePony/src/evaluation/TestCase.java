package evaluation;

import interfaces.Prediction;
import interfaces.Result;
import interfaces.Sequence;

public class TestCase {

	public final Sequence sequence;
	public final Statistics transmembrane;
	public final Statistics inside;
	public final Statistics outside;
	public final Statistics nontmh;
	public final Prediction prediction;

	public TestCase(Sequence sequence, Prediction prediction, Statistics transmembrane, Statistics nontmh, Statistics inside, Statistics outside) {
		this.sequence = sequence;
		this.prediction = prediction;
		this.transmembrane = transmembrane;
		this.nontmh = nontmh;
		this.inside = inside;
		this.outside = outside;
	}
	
	@Override
	public String toString() {
		String str = "SEQUENCE "+sequence.getId()+"\n\n";
		
		String[] lines = {"","","",""};
		
		for(int i=0; i<sequence.length(); i++) {
			lines[0] += sequence.getSequence()[i].getAminoAcid();
			lines[1] += sequence.getSequence()[i].getSecondaryStructure().getSingleLetterCode();
			
			lines[2] += sequence.getSequence()[i].getRealClass().getSingleLetterCode();
			
			lines[3] += prediction.getPredictionForResidue(i).getSingleLetterCode();

		}
		
		
		for(int i=0; i<sequence.length(); i+=80) {
			str+="       "+i+"\n";
			
			str+="AA     ";
//			for(int j=0; j<6-(""+i).length(); j++) str+=" ";
			str += lines[0].substring(i, (i+80>=lines[0].length() ? lines[0].length() : i+80));
			str += "\n";
			
			str+="SS     ";
//			for(int j=0; j<6; j++) str+=" ";
			str += lines[1].substring(i, (i+80>=lines[1].length() ? lines[1].length() : i+80));
			str += "\n";
			
			str+="Real   ";
//			for(int j=0; j<6; j++) str+=" ";
			str += lines[2].substring(i, (i+80>=lines[2].length() ? lines[2].length() : i+80));
			str += "\n";
			
			str+="Pred   ";
//			for(int j=0; j<6; j++) str+=" ";
			str += lines[3].substring(i, (i+80>=lines[3].length() ? lines[3].length() : i+80));
			str += "\n";
			str += "\n";
		}
		
		str+="TMH: "+transmembrane+"\n";
		str+="NON: "+nontmh+"\n";
		
		return str;
	}
}
