package predictor.mss;

import interfaces.Sequence;
import interfaces.SequencePosition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author greil
 */
public class Mss {

	private List<MssResult> listMss = new ArrayList<MssResult>();
	private SequencePosition[] seqPos;

	public List<MssResult> mss(Sequence sequence, int maxMss) {
		seqPos = sequence.getSequence();
		double max = 0;
		int l = 0;
		int r = 0;

		double rmax = 0;
		int rstart = 1;

		for (int i = 0; i < seqPos.length; i++) {
			double hp = seqPos[i].getHydrophobicity();
			//HACK!
			if ((rmax + hp)> 0) {
				rmax += hp;
			} else {
				rmax = 0;
				rstart = i+1;
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
