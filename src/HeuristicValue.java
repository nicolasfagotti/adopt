import java.util.Enumeration;
import java.util.Hashtable;

public class HeuristicValue extends Message {

	private Heuristic heuristic;
	private Hashtable<Integer, Integer> heuristicValue;
	private Hashtable<Integer, Integer> hvSum;
	
	public HeuristicValue(int e, Heuristic h) {
		super(e);
		heuristic = h;
		heuristicValue  = new Hashtable<Integer, Integer>();
		hvSum = new Hashtable<Integer, Integer>();
	}
	
	public Hashtable<Integer,Integer> getHv() {
		return heuristicValue;
	}
	
	public void setHv(int position, Hashtable<Integer, Integer> value) {

		for (Enumeration<Integer> i = value.keys(); i.hasMoreElements();) {
			int key = i.nextElement();
			if (hvSum.containsKey(key))
				hvSum.put(key, hvSum.get(key)+value.get(key));
			else
				hvSum.put(key, value.get(key));
		}
	}
	
	public void calculateHV(Node node) {
		heuristicValue = heuristic.calculateHeuristicValue(node, hvSum);
	}
}