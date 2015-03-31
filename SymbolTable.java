import java.util.TreeMap;

public class SymbolTable {
	private TreeMap<TableEntry, TableEntry> table;
	

	public SymbolTable() {
		table = new TreeMap<>();
	}

	public void put(TableEntry entry) {
		table.put(entry, entry);
	}

	public TableEntry get(int symbol, char type) {
		return get(new TableEntry(symbol, type, 0));
	}

	public TableEntry get(TableEntry entry) {
		return table.get(entry);
	}

	public boolean find(int symbol, char type) {
		return find(new TableEntry(symbol, type, 0));
	}

	public boolean find(TableEntry entry) {
		return (table.get(entry) != null);
	}

}