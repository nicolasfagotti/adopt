
public class Value extends Message{

	private int value;
		
	public Value(int e, int v) {
		super(e);
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}
