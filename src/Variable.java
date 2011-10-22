import java.util.Hashtable;
import java.util.Vector;

public class Variable {
	private int id;
	private String name;
	private int value;
	private Vector<Integer> domain;
	protected Hashtable<Integer,Container> constraints;
	private ConstraintVar mcaConstraint;


	public Variable(String n, Vector<Integer> d) {
		id     = 0;
		name   = n;
		domain = d;
		value  = d.firstElement();
		constraints    = new Hashtable<Integer,Container>();
		mcaConstraint = new ConstraintVar();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public Vector<Integer> getDomain() {
		return domain;
	}

	public void setId(int i) {
		id = i;
	}

	public void setValue(int v) {
		value = v;
	}

	public Container getConstraint(int v) {
		return constraints.get(v);
	}

	public int getConstraint(int neighbor, int value1, int value2) {
		return constraints.get(neighbor).getValue(value1, value2);
	}

	public void addConstraint(int v, Container c) {
		constraints.put(v, c);
	}
	
	
	/**
	 * Recibe como parametro un hash con los datos para obtener el costo del constraint.
	 * Devuelve el costo asociado al constraint.
	 */
	public int getMcaConstraint(Hashtable<String, Integer> vars) {
		return mcaConstraint.getCost(vars);
	}

	public void addMcaConstraint(ConstraintVar CV) {
		mcaConstraint = CV;
	}
}
