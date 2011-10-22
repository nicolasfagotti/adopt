import java.util.Hashtable;

public abstract class Constraint {

	protected String name;
	
	protected abstract int getCost(Hashtable<String, Integer> c);
	protected abstract void setCost(Hashtable<String, Integer> v, int c);
}