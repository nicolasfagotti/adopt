import java.util.Enumeration;
import java.util.Hashtable;


public class Container {

	private Hashtable<Integer,Hashtable<Integer,Integer>> matrix;
	private int defaultValue;


	/**
	 * Constructor.
	 */
	public Container(int dV) {
		matrix = new Hashtable<Integer,Hashtable<Integer,Integer>>();
		defaultValue = dV;
	}


	/**
	 * Agrega cierto valor en la ubicación definida por los
	 * claves pasadas por parámetro.
	 */
	public void addValue(int r, int c, int v) {
		if (!matrix.containsKey(r))
			matrix.put(r,new Hashtable<Integer,Integer>());
		matrix.get(r).put(c,v);
	}


	/**
	 * Obtiene el valor que definen las claves pasadas por
	 * parámetro.
	 */
	public int getValue(int r, int c) {
		if (matrix.containsKey(r) && matrix.get(r).containsKey(c))
			return matrix.get(r).get(c);
	    return defaultValue;
	}


	/**
	 * Pasadas las dos claves por parámetro, elimina el valor
	 * definido en ellas.
	 */
	public void resetValue(int r, int c) {
		if (matrix.containsKey(r)) {
			matrix.get(r).remove(c);
		}	    
	}


	/**
	 * Obtiene la fila pasada por parámetro.
	 */
	public Hashtable<Integer,Integer> getRow(int r) {
		return (Hashtable<Integer,Integer>)matrix.get(r).clone();
	}	


	/**
	 * Agrega una fila en la posición especificada.
	 */
	public void addRow(int position, Hashtable<Integer,Integer> row) {
		matrix.put(position, row);
	}


	/**
	 * Agrega una columna en la posición especificada.
	 */
	public void addColumn(int position, Hashtable<Integer,Integer> col) {
		int idRow;
		for (Enumeration<Integer> i=col.keys(); i.hasMoreElements();) {
			idRow = i.nextElement();
			if (!matrix.containsKey(idRow)) {
				matrix.put(idRow,new Hashtable<Integer,Integer>());
			}
			matrix.get(idRow).put(position, col.get(idRow));
		}
	}


	/**
	 * Elimina una fila determinada.
	 */
	public void deleteRow(int row) {
		matrix.remove(row);
	}


	/**
	 * Retorna una enumeración de claves para poder iterar sobre ellas.
	 */
	public Enumeration<Integer> keys() {
		return matrix.keys();
	}


	/**
	 * Retorna si la clave pasada por parámetro existe en la estructura
	 * principal.  
	 */
	public boolean containsKey(int key) {
		return matrix.containsKey(key);
	}
	
	
	/**
	 * Imprime la matriz principal.
	 */
	public void print() {
		Hashtable<Integer,Integer> row = new Hashtable<Integer,Integer>();
		int idRow, idCol;
		for (Enumeration<Integer> i=this.keys(); i.hasMoreElements();) {
			idRow = i.nextElement();
			row = this.getRow(idRow);
			for (Enumeration<Integer> j=row.keys(); j.hasMoreElements();) {
				idCol = j.nextElement();
				System.out.println("Key1: "+idRow+", Key2: "+idCol+", Value: "+this.getValue(idRow, idCol));
			}
		}
	}
}