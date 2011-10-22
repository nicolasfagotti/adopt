import java.util.Hashtable;
import java.util.Vector;


public class DP2 extends Heuristic {
	
	public DP2() {
		super();
	}

	public Hashtable<Integer, Integer> calculateHeuristicValue(Node node, Hashtable<Integer,Integer> hvIn) {
		Vector<Node> ancestors = node.getAncestors();
		Vector<Integer> myDomain = node.getAgent().getVariable().getDomain();
		Vector<Integer> parentDomain = node.getParent().getAgent().getVariable().getDomain();
		Hashtable<Integer,Integer> hvOut = new Hashtable<Integer,Integer>();
		Vector<Integer> ancDomain;
		Hashtable<Integer,Integer> ancConstraints = new Hashtable<Integer,Integer>();

		int minConst,constraint,totalConstAnc;
		int ancestorsConst,parentConst,minHv,hvConst = 0;
		Node ancestor;

		// Calcula los constraints del nodo "myNode", de cada uno de los valores del dominio,
		// con todos los posibles valores de cada uno de los ancestros que tenga.
		for (int i = 0; i < myDomain.size(); i++) {
			totalConstAnc = 0;
			for (int j = 0; j < ancestors.size(); j++) {
				minConst  = Integer.MAX_VALUE;
				ancestor  = ancestors.get(j);
				ancDomain = ancestor.getAgent().getVariable().getDomain();
				for (int k = 0; k < ancDomain.size(); k++) {
					constraint = node.getAgent().getVariable().getConstraint(ancestor.getId(), ancDomain.get(k), myDomain.get(i));
					if (constraint < minConst) { minConst = constraint; }
				}
				totalConstAnc += minConst;
			}
			ancConstraints.put(myDomain.get(i), totalConstAnc);
		}
		
		// Calcula los constraints con el nodo padre y los suma con el hv que ya le venia de
		// los hijos y también con los constraints guardados en ancConstraints (ancestorsConstraints).
		for (int i = 0; i < parentDomain.size(); i++) {
			minHv = Integer.MAX_VALUE;
			for (int j = 0; j < myDomain.size(); j++) {
				hvConst = 0;
				ancestorsConst = 0;
				if (hvIn.containsKey(myDomain.get(j))) {
					hvConst = hvIn.get(myDomain.get(j));
				}
				parentConst = node.getAgent().getVariable().getConstraint(node.getParent().getId(), parentDomain.get(i), myDomain.get(j));
				if (ancConstraints.containsKey(myDomain.get(j))) {
					ancestorsConst = ancConstraints.get(myDomain.get(j));
				}
				if ((parentConst + hvConst + ancestorsConst) < minHv) {
					minHv = parentConst + hvConst + ancestorsConst;
				}
			}
			hvOut.put(parentDomain.get(i), minHv);
		}
		return hvOut;
	}
}