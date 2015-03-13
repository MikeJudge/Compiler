import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Stack;

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

	//pre: file exists and contains valid syntax for a simple program
	//post: file created with machine level instructions contained in it
	public void compileProgram(String fileName) {
		this.fileName = fileName;

		Scanner scanner = openFile(fileName);

		while (scanner.hasNext()) {
			compileLine(scanner.nextLine());
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

	//pre: symbol, and type
	//post: returns tableEntry if it is in the table, if not in table put it in table and return it
	private TableEntry getEntry(int symbol, char type) {
		TableEntry entry = table.get(symbol, type);
		if (entry == null) { //not in the table yet
			entry = new TableEntry(symbol, type, dataCounter--);
			table.put(entry);
		}
		return entry;
	}

	private void compileLine(String line) {
		String [] tokens = getTokens(line);

		int lineNumber = Integer.parseInt(tokens[0]);
		table.put(new TableEntry(lineNumber, TableEntry.LINE, instructionCounter));

		String command = tokens[1];

		if (command.equals(REMARK)) 
		{
			return;
		}
		else if (command.equals(INPUT)) 
		{
			int symbol = (int)tokens[2].charAt(0);
			compileInputCommand(symbol);
		}
		else if (command.equals(PRINT)) 
		{
			int symbol = (int)tokens[2].charAt(0);
			compilePrintCommand(symbol);
		}
		else if (command.equals(LET))
		{
			String expression = line.substring(line.indexOf("let")+3);
			compileLetCommand(expression);
	    } 
	    else if (command.equals(END)) 
	    {
			machineCodeArr[instructionCounter++] = Simpletron.HALT*1000;
		} 
	    else 
	    {
			System.out.println("Compile error! command not found");
		}

	}

	private void compileInputCommand(int symbol) {
		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.READ*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
	}

	private void compilePrintCommand(int symbol) {
		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.WRITE*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
	}

	//pre: must be a full let statment i.e y = n1 + n1
	private void compileLetCommand(String statement) {
		String assignmentExpression = InfixToPostfixConverter.getPostfix(statement.substring(statement.indexOf("=")+1));
		String [] expressionArr = getTokens(assignmentExpression);

		Stack<TableEntry> stack = new Stack<>();
		String string;
		TableEntry x, y;

		for (int i = 0; i < expressionArr.length; i++) {
			string = expressionArr[i];
			//if it is an operator
			if (string.length() == 1 && InfixToPostfixConverter.isOperator(string.charAt(0))) {
				//operator: take the top two items evaluate them using the operand, and push the result to the stack
				char operator = string.charAt(0);

				if (stack.empty()) {
					x = new TableEntry(0, TableEntry.CONSTANT, dataCounter--);
					table.put(x);
				}
				else 
					x = stack.pop();

				if (stack.empty()) {
					y = new TableEntry(0, TableEntry.CONSTANT, dataCounter--);
					table.put(y);
				}
				else 
					y = stack.pop();

				switch (operator) {
					case '*': compileLetSubCommand(x, y, Simpletron.MULTIPLY); break;
					case '/': compileLetSubCommand(x, y, Simpletron.DIVIDE); break;
					case '+': compileLetSubCommand(x, y, Simpletron.ADD); break;
					case '-': compileLetSubCommand(x, y, Simpletron.SUBTRACT); break;
					case '%': compileLetSubCommand(x, y, Simpletron.REMAINDER); break;
				}

				TableEntry result = new TableEntry(0, TableEntry.CONSTANT, dataCounter--);
				machineCodeArr[instructionCounter++] = Simpletron.STORE*1000 + result.getLocation();
				stack.push(result); 

			} else {
				//it must be a constant, or a variable
				int symbol;
				char type;
				if (Character.isDigit(string.charAt(0))) {
					symbol = Integer.parseInt(string);
					type = TableEntry.CONSTANT;
				} 
				else {
					symbol = (int)string.charAt(0);
					type = TableEntry.VARIABLE;
				}

				TableEntry entry = getEntry(symbol, type);
				if (entry.getType() == TableEntry.CONSTANT)
					machineCodeArr[entry.getLocation()] = symbol;

				stack.push(entry);
			}
		}
		TableEntry solution = stack.pop();
		int variable = (int)(getTokens(statement)[0].charAt(0));
		TableEntry assigneeVariable = getEntry(variable, TableEntry.VARIABLE);

		machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + solution.getLocation();
		machineCodeArr[instructionCounter++] = Simpletron.STORE*1000 + assigneeVariable.getLocation();
	}


	//returns an array of strings split from whitespace in the input string
	private String[] getTokens(String string) {
		String [] tokens;
		String temp = string.trim();

		tokens = temp.split("\\s{1,}");
		return tokens;
	}

	private void compileLetSubCommand(TableEntry x, TableEntry y, int operation) {
		machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000  + y.getLocation();
		machineCodeArr[instructionCounter++] = operation*1000   + x.getLocation();
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