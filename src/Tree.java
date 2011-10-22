import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observer;
import java.util.Observable;
import java.util.Vector;


public class Tree implements Observer {

	private Node root;
	private Hashtable<Integer, Integer> solution;
	private int numberOfagents;
	private Hashtable<Integer, String> mapeo;
	private long tiempoInicio;
	private Vector <String> addedNodes;


	/**
	 * Constructor de la clase. A partir de las estructuras iniciales, genera el árbol
	 * que servirá como entrada de datos de la ejecución del algoritmo.
	 */
	public Tree(Parser parser, Heuristic heuristic) {
		Hashtable<String, Vector<Integer>> domains = parser.getDomains();
		Hashtable<String,String> va = parser.getVariablesAgent();
		Hashtable<String,String> vd = parser.getVariablesDomain();
		Hashtable<String,Hashtable<String,Container>> c = parser.getConstraints();
		String varRoot = parser.getRoot();
		Agent adoptAgent;

		solution   = new Hashtable<Integer, Integer>();
		mapeo      = new Hashtable<Integer, String>();
		addedNodes = new Vector <String>();

		// Variables internas al método.
		Hashtable<Integer,Node> nodes       = new Hashtable<Integer,Node>();
		Hashtable<String,Variable> mapeoVar = new Hashtable<String,Variable>();
		int identificador = 0;

		// Genero las variables y asigno las mismas a los agentes correspondientes.
		for (Enumeration<String> i = va.keys(); i.hasMoreElements();) {
			String varName = i.nextElement();
			
			// Se define el nodo y el agente relacionados a la variable.
			Node node         = new Node();
			Variable variable = new Variable(varName, domains.get(vd.get(varName)));

			if (heuristic != null) {
				adoptAgent = new HeuristicAgent(node, va.get(varName), variable, heuristic);
			} else {
				adoptAgent = new AdoptAgent(node, va.get(varName), variable);
			}
			
			// Se setea el identificador de la variable, de utilización interna al algoritmo.
			variable.setId(identificador);
			node.setAgent(adoptAgent);

			// Agrego el nodo al árbol y lo agrego como observable del árbol para poder así
			// identificar cuando se termina la ejecución de los nodos.
			nodes.put(identificador, node);
			adoptAgent.addObserver(this);

			mapeoVar.put(varName, variable);
			identificador++;
		}

		// Se definen el nodo raíz y el número de agentes totales que tendrá el árbol.
		root = nodes.get(mapeoVar.get(varRoot).getId());
		numberOfagents = nodes.size();

		// Se genera el árbol de modo recursivo a partir de la raíz definida.
		generateTree(c, mapeoVar, nodes, varRoot, 0);
		
		// Si se trata de un MCA, genero el nodo requerido.
		if (parser.getGlobalConstraint() != null) {
			generateVirtualNode(parser, identificador, mapeoVar, nodes);
		}
	}


	/**
	 * Genera el nodo virtual necesario para el procesamiento del MCA.
	 */
	private void generateVirtualNode(Parser parser, int identificador, Hashtable<String,Variable> mapeoVar, Hashtable<Integer,Node> nodes) {
		ConstraintVar mcaConstraint = parser.getGlobalConstraint();
		Vector<Integer> mcaDomain = new Vector<Integer>();
		int gBudget = parser.getGBudget();

		// Se generan el dominio, la variable, el agente y el nodo necesarios. 
		mcaDomain.add(0);
		mcaDomain.add(Integer.MAX_VALUE);
		Variable var = new Variable("MCA", mcaDomain);
		var.setId(identificador);
		var.addMcaConstraint(mcaConstraint);
		Node mcaNode = new Node();
		MCAAgent mcaAgent = new MCAAgent(mcaNode, "MCA", var, gBudget);
		mcaNode.setAgent(mcaAgent);

		// Se obtiene el nodo de mayor nivel (de los involucrados en el MCA)
		// para poder conocer al padre.
		Vector<String> mcaVars = parser.getMCAVars();
		Hashtable<Integer, String> mv = new Hashtable<Integer, String>();
		String maxLevelName = "";
		int maxLevel = 0, maxLevelIndex = 0;
		for (int i = 0; i < mcaVars.size(); i++) {
			Variable mcaVar = mapeoVar.get(mcaVars.get(i));
			if (nodes.get(mcaVar.getId()).getLevel() > maxLevel) {
				maxLevelName = mcaVars.get(i);
				maxLevelIndex = i;
				maxLevel = nodes.get(mcaVar.getId()).getLevel();
			}
		}

		// Se agrega el nodo virtual al correspondiente padre.
		Variable mcaVar = mapeoVar.get(maxLevelName);
		nodes.get(mcaVar.getId()).addLowerNeighbor(mcaNode);
		mv.put(mcaVar.getId(), mcaVar.getName());
		mcaVars.remove(maxLevelIndex);

		// Se agregan el resto de los nodos involucrados en el MCA como ancestros.
		for (int i = 0; i < mcaVars.size(); i++) {
			mcaVar = mapeoVar.get(mcaVars.get(i));
			nodes.get(mcaVar.getId()).addLowerNeighbor(mcaNode);
			mv.put(mcaVar.getId(), mcaVar.getName());
		}
		mcaAgent.setMapeoVar(mv);
	}


	/**
	 * Este método se encarga de generar la estructura del árbol donde se representan los
	 * distintos agentes (con sus respectivas variables) y los lazos que unen a estos.
	 */
	private void generateTree(Hashtable<String,Hashtable<String,Container>> c, Hashtable<String,Variable> mapeoVar, Hashtable<Integer,Node> nodes, String varRootName, int level)  {
		Variable varChild;
		Variable varRoot = mapeoVar.get(varRootName);
		String varChildName;
		addedNodes.add(varRootName);

		// Se setea el nivel del nodo siempre y cuando no se haya seteado antes.
		if (nodes.get(varRoot.getId()).getLevel() == -1) {
			nodes.get(varRoot.getId()).setLevel(level);
		}

		// Recorro y genero los constraints, los cuales a su vez definen los lazos del árbol.
		for (Enumeration<String> j = c.get(varRootName).keys(); j.hasMoreElements();) {
			varChildName = j.nextElement();
			if (!addedNodes.contains(varChildName)) {
				varChild = mapeoVar.get(varChildName);

				// Agrego el constraint y genero el lazo del árbol que relaciona los nodos
				// involucrados en dicho constraint.
				varChild.addConstraint(varRoot.getId(), c.get(varRootName).get(varChildName));
				nodes.get(varRoot.getId()).addLowerNeighbor(nodes.get(varChild.getId()));

				c.get(varRootName).remove(varChildName);
				c.get(varChildName).remove(varRootName);

				generateTree(c, mapeoVar, nodes, varChildName, level+1);
			}
		}
		addedNodes.remove(varRootName);
	}


	/**
	 * Imprime el costo final de la ejecución del algoritmo.
	 */
	private int calculateCost(Node n) {
		Vector<Node> ln = n.getLowerNeighbors();
		int total = 0;
		for (int i = 0; i < ln.size(); i++) {
			if (ln.get(i).getAgent() instanceof AdoptAgent) {
				Container pp = ln.get(i).getAgent().getConstraint(n.getId());
				if (ln.get(i).getParent().getId() == n.getId()) {
					total = total + pp.getValue(solution.get(n.getId()), solution.get(ln.get(i).getId())) + calculateCost(ln.get(i));
				} else {
					total = total + pp.getValue(solution.get(n.getId()), solution.get(ln.get(i).getId()));
				}
			}
		}
		return total;
	}

	
	/**
	 * Método principal de la clase, se encarga de ejecutar el algoritmo.
	 */
	public void run() {
		tiempoInicio = System.currentTimeMillis();
		root.getAgent().exec();
	}


	/**
	 * Notifica al árbol la finalización de la ejecución de cada uno de los agentes.
	 */
	public void update(Observable a, Object o) {
		if (a instanceof AdoptAgent) {
			int agentId      = ((Agent)a).getId();
			int agentValue   = ((Agent)a).getValue();
			String agentName = ((Agent)a).getVariable().getName();
	
			solution.put(agentId, agentValue);
			mapeo.put(agentId, agentName);
	
			if (solution.size() == numberOfagents) {
				long totalTiempo = System.currentTimeMillis() - tiempoInicio;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("\n(*) Solución: "); 
				for (Enumeration<Integer> i = solution.keys(); i.hasMoreElements();) {
					agentId = i.nextElement();
					System.out.print(mapeo.get(agentId)+" => "+solution.get(agentId)+" | ");
				}
				System.out.println("\n(*) Costo Optimo Total: " + this.calculateCost(root));
				System.out.println("(*) Tiempo de ejecución: "+(float)totalTiempo/1000+" segundos.\n");
			}
		}
	}
}