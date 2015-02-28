import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class Compiler {
	private static final String REMARK = "rem";
	private static final String INPUT  = "input";
	private static final String LET    = "let";
	private static final String PRINT  = "print";
	private static final String GOTO   = "goto";
///////if/goto

	private static final String END    = "end";

	private SymbolTable table;
	private int [] flags;
	private int [] machineCodeArr;
	private int instructionCounter;
	private int dataCounter;
	private String fileName;



	public Compiler() {
		table = new SymbolTable(1000);
		flags = new int[1000];
		machineCodeArr = new int[1000];

		Arrays.fill(flags, -1);
		instructionCounter = 0;
		dataCounter = 999;
	}

	public void compileProgram(String fileName) {
		this.fileName = fileName;

		Scanner scanner = openFile(fileName);
		String [] tokens;

		while (scanner.hasNext()) {
			analyzeLine(scanner.nextLine());
		}
		writeMachineCode();
	}

	private static Scanner openFile(String fileName) {
		Scanner scanner = new Scanner("");
		try {
			scanner = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {System.out.println("Failed to read file");}

		return scanner;
	}


	private void analyzeLine(String line) {
		String [] tokens = line.split(" ");

		int lineNumber = Integer.parseInt(tokens[0]);
		table.put(new TableEntry(lineNumber, TableEntry.LINE, instructionCounter));

		String command = tokens[1];

		if (command.equals(REMARK))
			return;

		if (command.equals(INPUT)) 
		{
			int symbol = (int)tokens[2].charAt(0);
			TableEntry entry = table.get(symbol, TableEntry.VARIABLE);
			if (entry == null) {
				entry = new TableEntry(symbol, TableEntry.VARIABLE, dataCounter--);
				table.put(entry);
			}

			int instruction = Simpletron.READ*1000 + entry.getLocation();
			machineCodeArr[instructionCounter++] = instruction;
		}
		else if (command.equals(PRINT)) 
		{
			int symbol = (int)tokens[2].charAt(0);
			TableEntry entry = table.get(symbol, TableEntry.VARIABLE);

			int instruction = Simpletron.WRITE*1000 + entry.getLocation();
			machineCodeArr[instructionCounter++] = instruction;
		}
		else if (command.equals(END)) {
			machineCodeArr[instructionCounter++] = Simpletron.HALT*1000;
		} else {
			System.out.println("Compile error! command not found");
		}

	}

	private void writeMachineCode() {
		try {
			PrintWriter writer = new PrintWriter(fileName.substring(0,fileName.length()-3) + "txt");
			for (int i = 0; i < machineCodeArr.length; i++) {
				writer.println(machineCodeArr[i]);
			}
			writer.close();
		} catch (FileNotFoundException io) {System.out.println("failed to write file");}

	}

	
	public static void main(String [] args) {
		Compiler compiler = new Compiler();
		compiler.compileProgram("test.smp");
	}
}