public class TableEntry implements Comparable<TableEntry> {
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

	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}

	public void setType(char type) {
		this.type = type;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	@Override
	public int compareTo(TableEntry entry) {
		if (this.getType() != entry.getType())
			return this.getType() - entry.getType();

		return this.getSymbol() - entry.getSymbol();	
	}

	@Override
	public boolean equals(Object entry) {
		if (entry instanceof TableEntry)
			return (0 == this.compareTo((TableEntry)entry));
		return false;
	}

	

}