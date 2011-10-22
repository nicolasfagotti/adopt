import java.util.Enumeration;
import java.util.Hashtable;

public class ConstraintVar extends Constraint {

	private Hashtable<Integer, Constraint> vars;


	/**
	 * 
	 */
	public ConstraintVar() {
		vars = new Hashtable<Integer, Constraint>();
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
		v.remove(name);

		if (!vars.containsKey(value)) {
			if (v.size() == 1) {
				vars.put(value, new ConstraintVarLeaf());
			} else {
				vars.put(value, new ConstraintVar());
			}
		}
		vars.get(value).setCost(v, c);
	}


	/**
	 * @Override
	 */
	protected int getCost(Hashtable<String, Integer> c) {
		if (!c.containsKey(name)) {
			return Integer.MAX_VALUE;
		}

		int value = c.get(name);
		return vars.get(value).getCost(c);
	}
}