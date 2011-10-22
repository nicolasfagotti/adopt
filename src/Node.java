import java.util.Vector;
import org.apache.log4j.Logger;

public class Node {

	private Node parent;
	private Agent agent;
	private int level;
	private Vector<Node> lowerNeighbors;
	private Vector<Node> ancestors;

    private static final Logger logger = Logger.getLogger(Dcop.class);

	/**
	 * Constructor de la clase.
	 */
	public Node() {
		lowerNeighbors = new Vector<Node>();
		ancestors      = new Vector<Node>();
		parent         = null;
		level          = -1;
	}


	/**
	 * Retorna true si el nodo es una hoja 
	 */
	public boolean isLeaf() {
		if (lowerNeighbors.isEmpty()) {
			return true;
		}
		return false;
	}


	/**
	 * Retorna true si el nodo es raiz 
	 */
	public boolean isRoot() {
		if (parent == null) {
			return true;
		}
		return false;
	}


	/**
	 * Obtiene el identificador del agente contenido por el nodo.
	 */
	public int getId() {
		return agent.getId();
	}

	
	/**
	 * Setea y obtiene el agente que contendrá el nodo.
	 */
	public void setAgent(Agent a){
		agent = a;
	}
	public Agent getAgent(){
		return agent;
	}


	/**
	 * Setea y obtiene el nivel que tendrá el nodo en el árbol.
	 */
	public void setLevel(int l) {
		level = l;
	}
	public int getLevel() {
		return level;
	}


	/**
	 * Setea y obtiene el padre del nodo.
	 */
	public void setParent(Node p) {
		parent = p;
	}
	public Node getParent() {
		return parent;
	}


	/**
	 * Agrega y obtiene los nodos ancestros (nodo padre relacionado con más de dos niveles de 
	 * diferencia).
	 */
	public void addAncestor(Node a) {
		ancestors.add(a);
	}
	public Vector<Node> getAncestors() {
		return ancestors;
	}


	/**
	 * Agrega un hijo con su correspondiente constraint (entre el hijo y el padre)
	 * al nodo actual.
	 */
	public void addLowerNeighbor(Node n) {
		//System.out.println("A la variable "+this.getAgent().getVariable().getName()+" se le agrega el hijo "+n.getAgent().getVariable().getName());
		logger.info("A la variable "+this.getAgent().getVariable().getName()+" se le agrega el hijo "+n.getAgent().getVariable().getName());
		lowerNeighbors.add(n);
		if (n.getParent() != null) {
			n.addAncestor(this);
		} else {
			n.setParent(this);
		}
	}

	
	/**
	 * Obtienen los nodos vecinos del nodo actual que se encuentren en un nivel
	 * más bajo (vecinos no ancestros ni padres).
	 */
	public Vector<Node> getLowerNeighbors() {
		return lowerNeighbors;
	}


	/**
	 * Obtiene los nodos vecinos que se encuentran en un nivel menor que el del 
	 * nodo actual, siempre y cuando sean hijos directos.
	 */
	public Vector<Node> getChilds() {
		Vector<Node> childs = new Vector<Node>();
		for (int i = 0; i < lowerNeighbors.size(); i++) {
			if (lowerNeighbors.get(i).getParent().getId() == this.getId())
				childs.add(lowerNeighbors.get(i));
		}
		return childs;
	}


	/**
	 * Comprueba que el Agente pasado como parámetro sea un vecino.
	 */
	public boolean isNeighbor(int idNode) {
		if ((parent != null) && (parent.getId() == idNode))
			return true;
		for (int i=0; i<lowerNeighbors.size(); i++)
			if (lowerNeighbors.get(i).getId() == idNode)
				return true;
		for (int i=0; i<ancestors.size(); i++)
			if (ancestors.get(i).getId() == idNode)
				return true;
		return false;
	}
}