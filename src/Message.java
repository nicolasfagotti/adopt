
public class Message {
	private int idEmisor;
	
	public Message(int e) {
		idEmisor = e;
	}
	
	public int getIdEmisor() {
		return idEmisor;
	}
	
	public void send(Agent a) {
		a.pushMessage(this);
	}
}
