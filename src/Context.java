import java.util.Enumeration;
import java.util.Hashtable;


public class Context extends Container {


	/**
	 * Constructor.
	 */
	public Context(int defaultValue) {
		super(defaultValue);
	}


	/**
	 * Devuelve true si el currentContext pasado como parametro
	 * es compatible con este contexto.
	 */
	public boolean isCompatible(Hashtable<Integer,Integer> currentContext) {
		int rowKey;
		for (Enumeration<Integer> i=this.keys(); i.hasMoreElements();) {
			rowKey = i.nextElement();
			if (!this.isCompatible(rowKey, currentContext)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Devuelve true si el Current Context es compatible con el contexto
	 * definido en la actual clase para el hijo pasado por parámetro.
	 */
	public boolean isCompatible(int idChild, Hashtable<Integer,Integer> currentContext) {
		Hashtable<Integer,Integer> ccChild = new Hashtable<Integer,Integer>();
		if (this.containsKey(idChild)) {
			ccChild = this.getRow(idChild);
			int idAgent;
			for (Enumeration<Integer> i=ccChild.keys(); i.hasMoreElements();) {
				idAgent = i.nextElement();
				if (currentContext.containsKey(idAgent) && ccChild.get(idAgent) != currentContext.get(idAgent)) {
					return false;
				}
			}
		}
		return true;
	}
}
