package visualization;

import java.util.Comparator;

public class VertexComparator implements Comparator<Vertex> {

	@Override
	public int compare(Vertex o1, Vertex o2) {
		if(o1.aa.compareTo(o2.aa)==0)
			if(o1.sse.compareTo(o2.sse)==0)
				if(o1.hp==o2.hp)
					return 0;
				else
					return (int) (o2.hp-o1.hp);
			else
				return o1.sse.compareTo(o2.sse);
		else
			return o1.aa.compareTo(o2.aa);
	}
	
}