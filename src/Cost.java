import java.util.Hashtable;


public class Cost extends Message {

	private Hashtable<Integer,Integer> currentContext;
	private int lowerBound, upperBound;

	@SuppressWarnings("unchecked")
	public Cost(int e, Hashtable<Integer,Integer> cc, int lb, int ub) {
		super(e);
		currentContext = (Hashtable<Integer,Integer>)cc.clone();
		lowerBound = lb;
		upperBound = ub;
	}

	public Hashtable<Integer, Integer> getCurrentContext() {
		return currentContext;
	}

	public int getLb() {
		return lowerBound;
	}

	public int getUb() {
		return upperBound;
	}
}
