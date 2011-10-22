import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class AdoptAgent extends Agent {

	private Hashtable<Integer,Integer> currentContext;
	private Container lowerBounds;
	private Container upperBounds;
	private Container thresholds;
	private Hashtable<Integer,Context> context;
	private int threshold;
	private boolean terminate;

	/**
	 * Constructor de la clase
	 */
	public AdoptAgent(Node n, String na, Variable v) {
		super(n, na, v);
		
		threshold      = 0;
		currentContext = new Hashtable<Integer,Integer>();
		context        = new Hashtable<Integer,Context>();
		lowerBounds    = new Container(LB_DEFAULT);
		upperBounds    = new Container(UB_DEFAULT);
		thresholds     = new Container(TH_DEFAULT);
		terminate      = false;

		var.setValue(getMinimalLBValue());
	}


	/**
	 * Devuelve el localCost para el agente en base a los constraints
	 * del agente con su padre y ancestros.
	 */
	private int getLocalCost(int v) {
		int localCost = 0, idAncestor;
		Vector<Node> ancestors = node.getAncestors();
		for (int i = 0; i < ancestors.size(); i++) {
			idAncestor = ancestors.get(i).getId();
			if (!currentContext.containsKey(idAncestor))
				return NOT_DEFINED;
			localCost += var.getConstraint(idAncestor, currentContext.get(idAncestor), v);
		}
		if (node.getParent() != null) {
			int idParent = node.getParent().getId();
			if ((currentContext == null) || (!currentContext.containsKey(idParent)))
				return NOT_DEFINED;
			localCost += var.getConstraint(idParent, currentContext.get(idParent), v);
		}
		return localCost;
	}


	/**
	 * Obtiene el UpperBound del agente para un valor del dominio pasado como parametro.
	 */
	private int getUB(int v) {
		int ub = this.getLocalCost(v);
		if (ub == NOT_DEFINED) {
			return UB_DEFAULT;
		}
		Vector<Node> childs = node.getChilds();

		for (int i=0; i<childs.size(); i++) {
			if (upperBounds.getValue(v, childs.get(i).getId()) == UB_DEFAULT) {
				return UB_DEFAULT;
			} else {
				ub += upperBounds.getValue(v, childs.get(i).getId());
			}
		}
		if (ub < 0) {
			return UB_DEFAULT;
		} else {
			return ub;
		}
	}


	/**
	 * Obtiene el LowerBound del agente para un valor del dominio pasado como parametro.
	 */
	private int getLB(int v) {
		int lb = this.getLocalCost(v);
		if (lb == NOT_DEFINED) {
			lb = LB_DEFAULT;	
		}
		Vector<Node> childs = node.getChilds();
		for (int i=0; i<childs.size(); i++) {
			lb += lowerBounds.getValue(v, childs.get(i).getId());
		}
		if (lb < 0) {
			return LB_DEFAULT;
		} else {
			return lb;
		}
	}


	/**
	 * 
	 */
	protected void setLbRow(int d, Hashtable<Integer,Integer> hv){
		lowerBounds.addColumn(d, hv);
	}


	/**
	 * Setea el valor del currentContext para el agente 'a' con el valor 'v'
	 */		
	private void changeCurrentContext(int a, int v) {
		currentContext.put(a,v);
	}
	
	
	/**
	 * Devuelve el valor de dominio que minimiza el lowerBound.
	 */
	private int getMinimalLBValue() {
		Vector<Integer> domain = var.getDomain();
		int lb, d = Integer.MAX_VALUE, result = domain.firstElement();
		for (int i=0; i<domain.size(); i++ ) {
			lb = getLB(domain.elementAt(i));
			if (d > lb)	{
				d = lb;
				result = domain.elementAt(i);
			}
		}
		return result;
	}

	
	/**
	 * Devuelve el valor de dominio que minimiza el upperBound.
	 */
	private int getMinimalUBValue() {
		Vector<Integer> domain = var.getDomain();
		int ub, d = Integer.MAX_VALUE, result = domain.firstElement();
		for (int i = 0; i < domain.size(); i++) {
			ub = getUB(domain.elementAt(i));
			if (d > ub)	{
				d = ub;
				result = domain.elementAt(i);
			}
		}
		return result;
	}


	/**
	 * Envía el mensaje 'm' al agente 'a'
	 */
	public void sendMessage(Message m, Agent a) {
		if (a != null) {
			if (m instanceof Cost) {
				logger.info("Mensaje COST enviado a "+a.getVariable().getName());
			} else if (m instanceof Terminate) {
				logger.info("Mensaje TERMINATE enviado a "+a.getVariable().getName());
			} else if (m instanceof Threshold) {
				logger.info("Mensaje THRESHOLD enviado a "+a.getVariable().getName());
			} else if (m instanceof Value) {
				logger.info("Mensaje VALUE enviado a "+a.getVariable().getName()+" ["+var.getValue()+"]");
			}
			a.pushMessage(m);
		}
	}


	/**
	 * Obtiene los datos necesarios y envía un mensaje 'cost' a su padre
	 */
	private void sendCostMessage() {
		int minUBDomain = this.getMinimalUBValue();
		int minLBDomain = this.getMinimalLBValue();
		Node parent = node.getParent();
		if ((parent != null) && (!currentContext.isEmpty())) {
			Message c = new Cost(node.getId(), currentContext, this.getLB(minLBDomain), this.getUB(minUBDomain));
			this.sendMessage(c, parent.getAgent());
		}
	}

	
	/**
	 * Obtiene los datos necesarios y envía un mensaje 'value' a todos sus vecinos de 
	 * nivel inferior
	 */
	private void sendValueMessage() {
		Message v = new Value(node.getId(), var.getValue());
		Vector<Node> ln = node.getLowerNeighbors();
		for (int i = 0; i < ln.size(); i++) {
			this.sendMessage(v, ln.get(i).getAgent());
		}
	}

	
	/**
	 * Obtiene los datos necesarios y envía un mensaje 'terminate' a sus hijos 
	 */
	private void sendTerminateMessage() {
		Message t = new Terminate(node.getId(), currentContext, var.getValue());
		Vector<Node> childs = node.getChilds();
		for (int i = 0; i < childs.size(); i++) {
			this.sendMessage(t, childs.get(i).getAgent());
		}
	}


	/**
	 * Obtiene los datos necesarios y envía un mensaje 'threshold' a sus hijos
	 */
	private void sendThresholdMessage() {
		int t;
		Message th;
		Vector<Node> childs = node.getChilds();
		for (int i = 0; i < childs.size(); i++) {
			t = thresholds.getValue(var.getValue(), childs.get(i).getId());
			th = new Threshold(this.getId(), t, currentContext);
			this.sendMessage(th, childs.get(i).getAgent());
		}
	}


	/**
	 * Ajusta el valor del threshold para que cumpla con las condiciones necesarias (LB >= threshold >= UB) 
	 */
	private void maintainThresholdInvariant() {
		int minLBDomain = this.getMinimalLBValue();
		int minUBDomain = this.getMinimalUBValue();
		int lb = this.getLB(minLBDomain);
		int ub = this.getUB(minUBDomain);
		
		if (threshold < lb) {
			threshold = lb;
		}
		if (threshold > ub) {
			threshold = ub;
		}
	}

	
	/**
	 * En base al threshold, calcula los bounds de los hijos y les envía un mensaje de threshold
	 * para que los mismos se ajusten a estos valores.
	 */
	private void maintainAllocationInvariant() {
		int i, childThreshold, childBound;
		boolean wasUpdated;
		Vector<Node> childs = node.getChilds();
		while (threshold > (this.getLocalCost(var.getValue())+this.sumChildThreshold(var.getValue()))) {
			wasUpdated = false;
			i = 0;
			while ((i<childs.size()) && !wasUpdated) {
				childThreshold = thresholds.getValue(var.getValue(), childs.get(i).getId());
				childBound     = upperBounds.getValue(var.getValue(), childs.get(i).getId());
				if (childBound > childThreshold) {
					wasUpdated = true;
					thresholds.addValue(var.getValue(), childs.get(i).getId(), childThreshold+1);
				}
				i++;
			}
			if (!wasUpdated) break;
		}
		while (threshold < (this.getLocalCost(var.getValue())+this.sumChildThreshold(var.getValue()))) {
			wasUpdated = false;
			i = 0;
			
			while ((i<childs.size()) && !wasUpdated) {
				childThreshold = thresholds.getValue(var.getValue(), childs.get(i).getId());
				childBound     = lowerBounds.getValue(var.getValue(), childs.get(i).getId());
				if (childBound < childThreshold) {
					wasUpdated = true;
					thresholds.addValue(var.getValue(), childs.get(i).getId(), childThreshold-1);
				}
				i++;
			}
			if (!wasUpdated) break;
		}
		this.sendThresholdMessage();
	}


	/**
	 * Ajusta el threshold de los hijos para que el mismo se mantenga dentro de los limites (LB >= threshold >= UB)
	 * y almacena estos valores dentro del hash de thresholds.
	 */
	private void maintainChildThresholdInvariant() {
		int domainValue, childThreshold, childBound;
		Vector<Node> childs = node.getChilds();
		Vector<Integer> domain = var.getDomain();

		for (int i = 0; i < domain.size(); i++) {
			domainValue = domain.get(i);
			
			for (int j = 0; j < childs.size(); j++) {
				childThreshold = thresholds.getValue(domainValue, childs.get(j).getId());
				childBound     = lowerBounds.getValue(domainValue, childs.get(j).getId());
				while (childBound > childThreshold) {
					childThreshold++;
				}
				thresholds.addValue(domainValue, childs.get(j).getId(), childThreshold);
			}
		}
		for (int i = 0; i < domain.size(); i++) {
			domainValue = domain.get(i);
			for (int j = 0; j < childs.size(); j++) {
				childThreshold = thresholds.getValue(domainValue, childs.get(j).getId());
				childBound     = upperBounds.getValue(domainValue, childs.get(j).getId());
				while (childBound < childThreshold) {
					childThreshold--;
				}
				thresholds.addValue(domainValue, childs.get(j).getId(), childThreshold);
			}
		}
	}

	
	/**
	 * Devuelve la suma de los threshods de los hijos para un dominio dado.
	 */
	private int sumChildThreshold(int v) {
		int t = 0;
		Vector<Node> childs = node.getChilds();

		for (int i=0; i<childs.size(); i++) {
			t += thresholds.getValue(v, childs.get(i).getId());
		}
		return t;
	}

	
	/**
	 * Retorna true si 2 currentContext pasados como parametro son compatibles. False en caso contrario. 
	 */
	private boolean isCompatible(Hashtable<Integer, Integer> cc1, Hashtable<Integer, Integer> cc2){
		int idAgent;
		for (Enumeration<Integer> i=cc1.keys(); i.hasMoreElements();) {
			idAgent = i.nextElement();
			if (cc2.containsKey(idAgent) && cc1.get(idAgent) != cc2.get(idAgent)) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Verifica que el contexto se encuentre compatible con el currentContext y en caso contrario
	 * elimina toda la información incompatible del context.
	 */
	private void checkCompatibility() {
		for (Enumeration<Integer> i=context.keys(); i.hasMoreElements();) {
			int domainValue = i.nextElement();
			Vector<Node> childs = node.getChilds();
			for (int j=0; j<childs.size(); j++) {
				int idChild = ((Node)childs.get(j)).getId();
				if (!context.get(domainValue).isCompatible(idChild, currentContext)) {
					lowerBounds.resetValue(domainValue,idChild);
					upperBounds.resetValue(domainValue,idChild);
					thresholds.resetValue(domainValue,idChild);
					context.get(domainValue).deleteRow(idChild);
				}
			}
		}
	}

	
	/**
	 * Dado un mensaje, realiza la acción correspondiente para el tipo de mensaje que se trate. 
	 */
	public void receiveMessage(Message message) {
		
		// Proceso el mensaje si el tipo del mismo es de COST.
		if (message instanceof Cost) {
			Hashtable<Integer, Integer> senderCC = new Hashtable<Integer, Integer>();
			senderCC = ((Cost)message).getCurrentContext();
			int idAgent, idEmisor, myContextValue, lb, ub;
			if (senderCC.containsKey(node.getId())) { // Se agregó esta condición para evitar tratamientos de mensajes de costo cuando todavía el hijo no procesó mi mensaje de valor.
				myContextValue = senderCC.get(node.getId());
				idEmisor = ((Cost)message).getIdEmisor();
				lb = ((Cost)message).getLb();
				ub = ((Cost)message).getUb();
				logger.info("Procesando mensaje COST de "+idEmisor);
				senderCC.remove(node.getId());
				if (terminate == false) {
					for (Enumeration<Integer> i=currentContext.keys(); i.hasMoreElements();) {
						idAgent = i.nextElement();
						if (!node.isNeighbor(idAgent)) {
							this.changeCurrentContext(idAgent, currentContext.get(idAgent));
						}
					}
					this.checkCompatibility();
				}
	    		if (this.isCompatible(currentContext, senderCC)) {
	    			lowerBounds.addValue(myContextValue, idEmisor, lb);
	    			upperBounds.addValue(myContextValue, idEmisor, ub);
	    			if (!context.containsKey(myContextValue)) {
	    				context.put(myContextValue, new Context(NOT_DEFINED));
	    			}
	    			context.get(myContextValue).addRow(idEmisor, senderCC);
	    			this.maintainChildThresholdInvariant();
	    			this.maintainThresholdInvariant();
	    		}
	    		this.backTrack();
			}

		// Proceso el mensaje si el tipo del mismo es de THRESHOLD.
		} else if (message instanceof Threshold) {
			Hashtable<Integer, Integer> senderCC = ((Threshold)message).getContext();
    		int senderThreshold = ((Threshold)message).getThreshold();
    		int idEmisor = ((Threshold)message).getIdEmisor();
			logger.info("Procesando mensaje THRESHOLD de "+idEmisor);
    		if (this.isCompatible(currentContext,senderCC)) {
    			threshold = senderThreshold;
    			this.maintainThresholdInvariant();
    			this.backTrack();
    		}

   		// Proceso el mensaje si el tipo del mismo es de VALUE.
		} else if (message instanceof Value) {
			int idEmisor = ((Value)message).getIdEmisor();
			logger.info("Procesando mensaje VALUE de "+idEmisor);
    		if (terminate == false) {
    			this.changeCurrentContext(((Value)message).getIdEmisor(),((Value)message).getValue());
    			this.checkCompatibility();
    			this.maintainThresholdInvariant();
    			this.backTrack();
    		}

   		// Proceso el mensaje si el tipo del mismo es de TERMINATE.
    	} else if (message instanceof Terminate) {
    		int idEmisor = ((Terminate)message).getIdEmisor();
			logger.info("Procesando mensaje TERMINATE de "+idEmisor);
    		currentContext = ((Terminate)message).getCurrentContext();
    		terminate = true;
    		this.backTrack();
    	}
	}


	/**
	 * El backtrack se encarga de establcer un valor para el agente, y envíar los mensajes correspondientes
	 * a sus agentes vecinos.
	 */
	private void backTrack() {
		int minUBDomain = this.getMinimalUBValue();
		int minLBDomain = this.getMinimalLBValue();
		if (threshold == this.getUB(minUBDomain)) {
			var.setValue(minUBDomain);
			this.sendValueMessage();
			this.maintainAllocationInvariant();
		} else if (threshold < this.getLB(var.getValue())) {
			var.setValue(minLBDomain);
			this.sendValueMessage();
			this.maintainAllocationInvariant();
//		} else if (node.isRoot()) {
//			finishThread = true;
//			this.sendTerminateMessage();
		}
		
//		this.sendValueMessage();
//		this.maintainAllocationInvariant();
		
		//logger.info("threshold: "+threshold+" getUB: "+this.getUB(minUBDomain));
		
		if ((threshold == this.getUB(minUBDomain)) && (terminate || node.isRoot())) {
			finishThread = true;
			this.sendTerminateMessage();
		} else {
			this.sendCostMessage();
		}
	}


	/**
	 * Es el metodo encargado de disparar la ejecución del thread.
	 */
	public void startAgent() {

		this.sendValueMessage();

		// Mientras no se haya indicado que el hilo debe finalizar, se procesan los mensajes
		// que existen en la cola.
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
}