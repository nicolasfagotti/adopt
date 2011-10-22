import java.util.Hashtable;

public class Threshold extends Message {

	private int threshold;
	private Hashtable<Integer,Integer> context;

	@SuppressWarnings("unchecked")
	public Threshold(int e, int t, Hashtable<Integer,Integer> c) {
		super(e);
		threshold = t;
		context = (Hashtable<Integer,Integer>)c.clone();
	}
	
	public Hashtable<Integer, Integer> getContext() {
		return context;
	}
	
	public int getThreshold() {
		return threshold;
	}

}
