public class SymbolTable {
	private TableEntry [] table;
	private int size;

	public SymbolTable(int initSize) {
		table = new TableEntry[initSize];
		size = 0;
	}

	public void put(TableEntry entry) {
		if (size < table.length)
			table[size++] = entry;
	}

	public int find(int symbol, char type) {
		for (int i = 0; i < size; i++) {
			if (symbol == table[i].getSymbol() && type == table[i].getType())
				return i;
		}
		return -1;
	}

	public TableEntry get(int symbol, char type) {
		for (int i = 0; i < size; i++) {
			if (symbol == table[i].getSymbol() && type == table[i].getType())
				return table[i];
		}
		return null;
	}

	public TableEntry get(TableEntry entry) {
		return get(entry.getSymbol(), entry.getType());
	}

	public int find(TableEntry entry) {
		return find(entry.getSymbol(), entry.getType());
	}

}