import java.util.Enumeration;
import java.util.Hashtable;


public class MCAAgent extends Agent {

	private Hashtable<Integer, Integer> currentContext;
	private Hashtable<Integer, String> mapeoVar;
	private int gBudget;


	/**
	 * Constructor de la clase.
	 */
	public MCAAgent(Node n, String na, Variable v, int gb) {
		super(n, na, v);
		currentContext = new Hashtable<Integer, Integer>();
		gBudget = gb;
	}


	/**
	 * @Override
	 */
	protected void receiveMessage(Message message) {

		// Proceso el mensaje si el tipo del mismo es de COST.
		if (message instanceof Cost) {

		// Proceso el mensaje si el tipo del mismo es de THRESHOLD.
		} else if (message instanceof Threshold) {

		// Proceso el mensaje si el tipo del mismo es de VALUE.
		} else if (message instanceof Value) {
			int idEmisor = ((Value)message).getIdEmisor();
			
			logger.info("Procesando mensaje VALUE de "+idEmisor);

			currentContext.put(idEmisor, ((Value)message).getValue());
			backTrack();

   		// Proceso el mensaje si el tipo del mismo es de TERMINATE.
    	} else if (message instanceof Terminate) {
			finishThread = true;
    		int idEmisor = ((Terminate)message).getIdEmisor();
			
			logger.info("Procesando mensaje TERMINATE de "+idEmisor);
//    		currentContext = ((Terminate)message).getCurrentContext();
    	}
	}


	/**
	 * 
	 */
	private void backTrack() {
		Hashtable<String, Integer> varValues = new Hashtable<String, Integer>();
		int idVar, lb, ub;
		for (Enumeration<Integer> i = currentContext.keys(); i.hasMoreElements(); ) {
			idVar = i.nextElement();
			varValues.put(mapeoVar.get(idVar), currentContext.get(idVar));
		}
		if (getLocalCost(varValues)) {
			lb = Integer.MAX_VALUE;
			ub = Integer.MAX_VALUE;
		} else {
			lb = 0;
			ub = 0;
		}
		Message c = new Cost(node.getId(), currentContext, lb, ub);
		Node parent = node.getParent();
		if ((parent != null) && (!currentContext.isEmpty())) {
			this.sendMessage(c, parent.getAgent());
		}
	}


	/**
	 * 
	 */
	private boolean getLocalCost(Hashtable<String, Integer> varValues) {
		int constraintCost = var.getMcaConstraint(varValues);
		if (constraintCost > gBudget) {
			return true;
		}
		return false;
	}

	
	/**
	 * @Override
	 */
	protected void sendMessage(Message m, Agent a) {
		if (a != null) {
			
			logger.info("Mensaje COST enviado a "+a.getVariable().getName());
			a.pushMessage(m);
		}
	}


	/**
	 * Es el metodo encargado de disparar la ejecución del thread.
	 * @Override
	 */
	public void startAgent() {
		Message m;
		while (finishThread == false) {
			m = this.popMessage();
			if (m != null) {
				
				logger.info("Queue size: "+queue.size());
				this.receiveMessage(m);
				m = null;
			}
		}
		logger.info("=> "+var.getValue()+"; ");
		logger.info("Stoping thread");
		
		

		// Métodos para informar a quien lo requiera que se terminó con la ejecución
		// del hilo.
		setChanged();
	    notifyObservers();
	}

	public void setMapeoVar(Hashtable<Integer, String> mv) {
		mapeoVar = mv;
	}
}