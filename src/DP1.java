import java.util.Hashtable;
import java.util.Vector;

public class DP1 extends Heuristic {

	public DP1() {
		super();
	}

	public Hashtable<Integer, Integer> calculateHeuristicValue(Node node, Hashtable<Integer,Integer> hvIn) {
		Vector<Integer> myDomain     = node.getAgent().getVariable().getDomain();
		Vector<Integer> parentDomain = node.getParent().getAgent().getVariable().getDomain();
		Hashtable<Integer,Integer> hvOut = new Hashtable<Integer,Integer>();
		int parentConst, minHv, hvConst = 0;

		// Calcula los constraints con el nodo padre y los suma con el hv que ya le venia de los hijos y también con 
		// los constraints guardados en ancConstraints (ancestorsConstraints).
		for (int i = 0; i < parentDomain.size(); i++) {
			minHv = Integer.MAX_VALUE;
			for (int j = 0; j < myDomain.size(); j++) {
				hvConst = 0;
				if (hvIn.containsKey(myDomain.get(j))) {
					hvConst = hvIn.get(myDomain.get(j));
				}
				parentConst = node.getAgent().getVariable().getConstraint(node.getParent().getId(), parentDomain.get(i), myDomain.get(j));
				if ((parentConst + hvConst) < minHv) {
					minHv = parentConst + hvConst;
				}
			}
			hvOut.put(parentDomain.get(i), minHv);
		}
		return hvOut;
	}
}
