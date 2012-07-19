package predictor.mss;

import interfaces.Sequence;
import interfaces.SequencePosition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author greil
 */
public class MssMod {

	private List<MssResult> listMss;
	private SequencePosition[] seqPos;

	public List<MssResult> mss(Sequence sequence, int minDist) {
		listMss = new ArrayList<MssResult>();
		seqPos = sequence.getSequence();
		int posStart = -1;
		int posCounter = -1;
		boolean posOpen = false;
		double weight = 0;
		for (int i = 0; i < seqPos.length; i++) {
			double hp = seqPos[i].getHydrophobicity();
			if (hp > 0) {
				//positive found
				if (posOpen) {
					posCounter++;
					weight += hp;
				} else {
					posStart = i;
					posCounter = 1;
					weight = hp;
					posOpen = true;
				}

			} else {
				//negative found
				if (posOpen) {
					if (posCounter >= minDist) {
						listMss.add(new MssResult(posStart, (posStart + (posCounter - 1)), weight));
					}
					weight = 0;
					posStart = 0;
					posCounter = 0;
					posOpen = false;

				}
			}
		}
		return listMss;
	}
}
