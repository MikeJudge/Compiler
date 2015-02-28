public class TableEntry {
	public static final char CONSTANT = 'C';
	public static final char LINE     = 'L';
	public static final char VARIABLE = 'V';

	private int symbol;
	private char type;
	private int location;

	public TableEntry(int symbol, char type, int location) {
		this.symbol = symbol;
		this.type = type;
		this.location = location;
	}

	public int getSymbol() {
		return symbol;
	}

	public char getType() {
		return type;
	}

	public int getLocation() {
		return location;
	}

}