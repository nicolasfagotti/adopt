import java.util.Vector;


public class Queue {

	protected Vector<Message> queue;


	public Queue() {
		queue = new Vector<Message>();
	}


	/**
	 * Obtiene un mensaje en la cola FIFO del agente.
	 */
	public Message popMessage() {
		if (queue.isEmpty()) {
			return null;
		}
		Message m = queue.firstElement();
		queue.remove(0);
		return m;
	}


	/**
	 * Obtiene un mensaje en la cola FIFO del agente.
	 */
	public Message firstMessage() {
		if (queue.isEmpty()) {
			return null;
		}
		Message m = queue.firstElement();
		return m;
	}


	/**
	 * Inserta un mensaje de la cola FIFO del agente.
	 */
	public void pushMessage(Message m) {
		if (m instanceof Terminate) { 
			queue.add(0, m);
		} else {
			queue.add(m);
		}
	}

	public void pushMessage(Message m, int position) {
		queue.add(position, m);
	}

	public int size() {
		return queue.size();
	}

}
