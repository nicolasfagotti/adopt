import java.util.Hashtable;

public abstract class Heuristic {
	
	public Heuristic() {}
	
	public abstract Hashtable<Integer, Integer> calculateHeuristicValue(Node node, Hashtable<Integer,Integer> hv);

}