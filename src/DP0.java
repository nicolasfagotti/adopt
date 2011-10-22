import java.util.Hashtable;
import java.util.Vector;

public class DP0 extends Heuristic {

	public DP0() {
		super();
	}

	public Hashtable<Integer, Integer> calculateHeuristicValue(Node node, Hashtable<Integer,Integer> hv) {
		Vector<Node> ancestors = node.getAncestors();
		Vector<Integer> myDomain = node.getAgent().getVariable().getDomain();
		Vector<Integer> parentDomain = node.getParent().getAgent().getVariable().getDomain();
		Vector<Integer> ancDomain;
		
		int minConst,constraint,totalConstAnc;
		int parentConst,minHv;
		Node ancestor;

		// Calcula los constraints del nodo "myNode",de cada uno de los valores del dominio, con todos los posibles valores de
		// cada uno de los ancestros que tenga. Obtiene el minimo con cada uno y calcula la suma total.
		totalConstAnc = 0;
		for (int j = 0; j < ancestors.size(); j++) {
			ancestor  = ancestors.get(j);
			ancDomain = ancestor.getAgent().getVariable().getDomain();
			minConst  = Integer.MAX_VALUE;
			for (int i = 0; i < myDomain.size(); i++) {
				for (int k = 0; k < ancDomain.size(); k++) {
					constraint = node.getAgent().getVariable().getConstraint(ancestor.getId(), ancDomain.get(k), myDomain.get(i));
					if (constraint < minConst) {
						minConst = constraint;
					}
				}
			}
			totalConstAnc += minConst;
		}
		
		// Calcula los constraints del nodo "myNode",de cada uno de los valores del dominio, con todos los posibles valores 
		// del dominio del padre y se queda con el minimo.
		minHv = Integer.MAX_VALUE;
		for (int i = 0; i < parentDomain.size(); i++) {
			for (int j = 0; j < myDomain.size(); j++) {
				parentConst = node.getAgent().getVariable().getConstraint(node.getParent().getId(), parentDomain.get(i), myDomain.get(j));
				if (parentConst < minHv) { minHv = parentConst; }
			}
		}

		for (int i = 0; i < parentDomain.size(); i++) {
			hv.put(parentDomain.get(i), minHv + totalConstAnc);
		}

		return hv;
	}
}