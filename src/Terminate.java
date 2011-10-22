import java.util.Hashtable;


public class Terminate extends Message {

	private Hashtable<Integer,Integer> currentContext;

	@SuppressWarnings("unchecked")
	public Terminate(int e, Hashtable<Integer,Integer> cc, int v) {
		super(e);
		currentContext = (Hashtable<Integer,Integer>)cc.clone();
		currentContext.put(e,v);		
	}


	public Hashtable<Integer, Integer> getCurrentContext() {
		return currentContext;
	}
}