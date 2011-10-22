import java.util.Vector;


public class HeuristicAgent extends AdoptAgent {
	private Heuristic heuristic;

	/**
	 * Constructor de la clase.
	 */
	public HeuristicAgent(Node n, String na, Variable v, Heuristic h) {
		super(n, na, v);
		heuristic = h;
	}

	/**
	 * Extrae el mensaje HeuristicValue de la cola de mensajes.
	 */
	private boolean receiveHeuristicMessage() {
		Message message = queue.firstMessage();
		if (message instanceof HeuristicValue) {
			return true;
		}
		return false;
	}

	/**
	 * Aplica la heurísitca definida al agente.
	 */
	public void applicateHeuristic() {
		Message message;
		int heuristicChild = 0; 
		HeuristicValue heuristicMessage = new HeuristicValue(node.getId(), heuristic);
		Vector<Node> childs = node.getChilds();

		// Si el nodo es una hoja, se calcula el "heuristic value" y se envía el resultado
		// al nodo padre.
		if (node.isLeaf()) {
			
			logger.info("Mensaje HEURISTIC VALUE enviado a "+node.getParent().getAgent().getName());

			heuristicMessage.calculateHV(node);
			heuristicMessage.send(node.getParent().getAgent());

		// Se espera a que los hijos reporten el cálculo del "heuristic value" para poder
		// realizar el cálculo que corresponde al nodo y enviar el resultado al padre.
		} else {
			while (heuristicChild < childs.size()) {
				message = queue.firstMessage();
				if (message != null && this.receiveHeuristicMessage()) {
					message = queue.popMessage();
					int idEmisor = message.getIdEmisor();
					
					logger.info("Procesando mensaje HEURISTIC VALUE de "+idEmisor);

					// Setea el lowerbound del AdoptAgent y completa el "heuristic value" propio
					// con el "heuristic value" repotado por el nodo hijo. 
					this.setLbRow(idEmisor, ((HeuristicValue)message).getHv());
					heuristicMessage.setHv(idEmisor, ((HeuristicValue)message).getHv());
					heuristicChild++;
				}
			}
			if (!node.isRoot()) {
				
				logger.info("Mensaje HEURISTIC VALUE enviado a "+node.getParent().getAgent().getName());
				heuristicMessage.calculateHV(node);
				heuristicMessage.send(node.getParent().getAgent());
			}
		}		
	}


	/**
	 * Ejecuta el algoritmo. Se encarga de aplicar la heurística y llamar al método de ejecución
	 * del AdoptAgent padre.
	 */
	public void startAgent() {
		this.applicateHeuristic();
		super.startAgent();
	}
}