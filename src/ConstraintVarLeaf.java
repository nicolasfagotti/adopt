import java.util.Enumeration;
import java.util.Hashtable;

public class ConstraintVarLeaf extends Constraint {

	private Hashtable<Integer, Integer> vars;

	
	/**
	 * 
	 */
	public ConstraintVarLeaf() {
		vars = new Hashtable<Integer, Integer>();
		name = "";
	}


	/**
	 * 
	 */
	public void setCost(Hashtable<String, Integer> v, int c) {
		if (name.isEmpty()) {
			Enumeration<String> e = v.keys();
			name = e.nextElement();
		}

		int value = v.get(name);
		vars.put(value, c);
	}


	/**
	 * @Override
	 */
	protected int getCost(Hashtable<String, Integer> c) {
		if (!c.containsKey(name)) {
			return Integer.MAX_VALUE;
		}

		int value = c.get(name);
		if (vars.containsKey(value)) {
			return vars.get(value);
		} else {
			return Integer.MAX_VALUE;
		}
	}
}