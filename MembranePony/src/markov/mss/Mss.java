package markov.mss;

import data.Hydrophobicity;
import interfaces.Sequence;
import interfaces.SequencePosition;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author greil
 */
public class Mss {

	private List<MssResult> listMss = new LinkedList<MssResult>();
	private SequencePosition[] seqPos;

	public List<MssResult> mss(Sequence sequence, int maxMss) {
		seqPos = sequence.getSequence();
		double max = 0;
		double rmax = 0;
		int rstart = 1;
		int l = 0;
		int r = 0;

		for (int i = 0; i < seqPos.length; i++) {
			if (rmax > 0) {
				rmax += seqPos[i].getHydrophobicity();
			} else {
				rmax = seqPos[i].getHydrophobicity();
				rstart = i;
			}

			if (rmax > max) {
				max = rmax;
				l = rstart;
				r = i;

				listMss.add(new MssResult(l, r, max));
				if (listMss.size() > maxMss) {
					findAndRemoveMinimum();
				}
			}
		}

		return listMss;
	}

	private void findAndRemoveMinimum() {
		double min = Double.MAX_VALUE;
		MssResult tmp = null;

		for (MssResult mssResult : listMss) {
			double current = mssResult.getValue();
			if (current < min) {
				min = current;
				tmp = mssResult;
			}
		}
		if (tmp == null) {
			return;
		}
		listMss.remove(tmp);
	}
}
