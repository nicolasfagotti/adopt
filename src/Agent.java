import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;


public abstract class Agent extends Observable implements Runnable {
	
	// Constantes utilizadas en la clase.
	final int NOT_DEFINED = -1;
	final int TH_DEFAULT  = 0;
	final int LB_DEFAULT  = 0;
	final int UB_DEFAULT  = Integer.MAX_VALUE;

	// Referencia al nodo que contiene al agente.
	protected Node node;

	// Variables propias del Agente.
	private String name;
	protected Variable var;
	protected Queue queue;
	
	// Variables para controlar hilos y concurrencia.
	private Thread thread;
	public Semaphore available;
	public Semaphore queueSem;
	protected boolean finishThread;

	// Variables varias;
    protected static final Logger logger = Logger.getLogger(Dcop.class);

	/**
	 * Constructor de la clase.
	 */
	public Agent(Node n, String na, Variable v) {
		name         = na;
		node         = n;
		var          = v;
		thread       = new Thread(this);
		available    = new Semaphore(1, true);
		queue        = new Queue();
		finishThread = false;
		queueSem     = new Semaphore(1, true);

	}


	/**
	 * Obtiene el identificador del agente.
	 */
	public int getId() {
		return var.getId();
	}


	/**
	 * Obtiene el valor del agente.
	 */
	public int getValue() {
		return var.getValue();
	}
	

	/**
	 * Obtiene el nombre del agente.
	 */
	public String getName() {
		return name;
	}

	
	/**
	 * Obtiene la variable que contiene el agente.
	 */
	public Variable getVariable() {
		return var;
	}


	/**
	 * Agrega y obtiene un constraint del agente.
	 */
	public Container getConstraint(int a) {
		return var.getConstraint(a);
	}


	/**
	 * Método definido en la interfaz Runnable. Se encarga de propagar el comienzo
	 * de los hilos a los agentes que se encuentran en nodos inferiores y de ejecutar
	 * el algoritmo principal del agente actual.
	 */
	public void run() {
		boolean startThread = false;

		// Si se trata del nodo raíz, se comienza a propagar el mensaje de START a
		// los agentes que se encuentran en nodos inferiores.
		if (node.isRoot()) {
			sendStartMessage();
			startThread = true;
		}

		// Si se trata de un nodo que no es raíz, el agente se queda observando la
		// cola de mensajes hasta que el mensaje de START le llega.
		while (!startThread) {
			if (this.receiveStartMessage()) {
				startThread = true;
			}
		}

		// Se ejecuta el algoritmo (Método definido en la implementación de la clase
		// que hereda).
		this.startAgent();
	}


	/**
	 * Ejecuta el hilo donde el agente correrá. Se utlilizan semáforos para controlar
	 * la concurrencia en la ejecución del los hilos.
	 */
	public void exec() {
		try {
			available.acquire();
			if (!(thread.isAlive())) {
				
				// El método start de la clase Thread ejecuta el método run(), el cual
				// viene definido en la implementación de la interfaz Runnable.
				thread.setName(this.getVariable().getName());
				thread.start();
			}
			available.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Comienzo de la ejecución del hilo de los hijos del nodo.
    	logger.info("Starting thread ("+this.getVariable().getName()+")");

		Vector<Node> childs = node.getChilds();
		for (int i=0; i<childs.size(); i++) {
			((Node)childs.elementAt(i)).getAgent().exec();
        }
	}

	
	/**
	 * Loguea el comando pasado por parámetro siempre y cuando el nivel de logueo
	 * definido así lo determine.
	 */
//	public void log(String text, int level) {
//		if (debugLevel >= level)
//			System.out.println(var.getName()+": "+text);
//	}


	/**
	 * Envia el mensaje de Start a los hijos.
	 */
	private void sendStartMessage() {
		Message m;
		Vector<Node> childs = node.getChilds();
		for (int i = 0; i < childs.size(); i++) {
			m = new Start(this.getId());
			
			Agent a = childs.get(i).getAgent();
			a.pushPriorityMessage(m);
			logger.info("Mensaje START enviado a "+childs.get(i).getAgent().getVariable().getName());
		}
	}


	/**
	 * Extrae el mensaje de Start de la cola de mensajes.
	 */
	private boolean receiveStartMessage() {
		Message message = queue.firstMessage();
		if (message instanceof Start) {
			message = this.popMessage();
			int idEmisor = ((Start)message).getIdEmisor();
			logger.info("Procesando mensaje START de "+idEmisor);
			sendStartMessage();
			return true;
		} else {
			return false;
		}
	}


	/**
	 * Extienden la funcionalidad de la cola y la vuelven de ámbito público.
	 */
	public Message popMessage() {
		return queue.popMessage();
	}
	public void pushMessage(Message m) {
		try {
			queueSem.acquire();
			queue.pushMessage(m);
			queueSem.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void pushPriorityMessage(Message m) {
		try {
			queueSem.acquire();
			queue.pushMessage(m, 0);
			queueSem.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Métodos abstractos de la clase, encargados del envío de los mensajes entre
	 * los agentes y de la ejecución de los algoritmos que afectaran a los mismos.
	 */
	protected abstract void sendMessage(Message m, Agent a);
	protected abstract void receiveMessage(Message m);
	public abstract void startAgent();
}