import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Stack;

public class Compiler {
	//valid language commands
	private static final String REMARK = "rem";
	private static final String INPUT  = "input";
	private static final String LET    = "let";
	private static final String PRINT  = "print";
	private static final String GOTO   = "goto";
	private static final String IF     = "if";
	private static final String END    = "end";

	private SymbolTable table; //location where all identifiers of the program are stored, i.e line numbers, variables, constants
	private int [] flags; //used for goto commands
	private int [] machineCodeArr; //temporary store for machine code to be written
	private int instructionCounter; //location in machineCodeArr
	private int dataCounter;        //location in machineCodeArr
	private String fileName;	    //name on file being compiled


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
		Scanner scanner;
		try {
			scanner = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {System.out.println("Failed to open source file"); return;}

		//reads each line and compiles it
		while (scanner.hasNext()) {
			compileLine(scanner.nextLine());
		}

		//second pass, any unresolved references are set here. example: goto a place in the program that the compiler did not know 
		//existed on the first pass- it was a forward reference
		for (int i = 0; i < flags.length; i++) {
			if (flags[i] != -1) {
				//flags[i] is the line that was referenced to by the goto statement
				TableEntry entry = table.get(flags[i], TableEntry.LINE);
				machineCodeArr[i] += entry.getLocation();
			}
		}
		writeMachineCode();
	}

	//pre: symbol, and type
	//post: returns tableEntry if it is in the table, if not in table put it in table and return it. If it is a constant
	//      store the constant value in the machineCodeArr
	private TableEntry getEntry(int symbol, char type) {
		TableEntry entry = table.get(symbol, type);
		if (entry == null) { //not in the table yet
			entry = new TableEntry(symbol, type, dataCounter--);
			table.put(entry);
		}
		if (entry.getType() == TableEntry.CONSTANT)
			machineCodeArr[entry.getLocation()] = entry.getSymbol(); //stores the constant value in the machine code

		return entry;
	}

	//pre: syntactically correct program code line
	//post: machineCodeArr filled with some machine instructions
	private void compileLine(String line) {
		String [] tokens = getTokens(line);

		//adds line to the symbol table
		//*very important for goto statements*
		int lineNumber = Integer.parseInt(tokens[0]);
		table.put(new TableEntry(lineNumber, TableEntry.LINE, instructionCounter));

		String command = tokens[1];

		if (command.equals(REMARK)) 
		{
			return; //ignore the rest, its just a comment for the developer
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
			String expression = line.substring(line.indexOf("let")+3); //the substring after "let"
			compileLetCommand(expression);
	    } 
	    else if (command.equals(GOTO))
	    {
	    	compileGoToCommand(Integer.parseInt(tokens[2]), Simpletron.BRANCH);
	    }
	    else if (command.equals(IF))
	    {
	    	String operandLeft = tokens[2];
	    	String operator = tokens[3];
	    	String operandRight = tokens[4];
	    	int location = Integer.parseInt(tokens[6]);
	    	compileIfCommand(operandLeft, operator, operandRight, location);
	    }
	    else if (command.equals(END)) 
	    {
			machineCodeArr[instructionCounter++] = Simpletron.HALT*1000;
		} 
	    else 
	    {
			System.out.println("Compile error! command not found");
			System.out.println(line);
		}

	}

	//post: input command compiled
	private void compileInputCommand(int symbol) {
		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.READ*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
	}

	//post: print command compiled and added to machineCodeArr
	private void compilePrintCommand(int symbol) {
		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.WRITE*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
	}

	//pre: lineNumber to goto and the specific goto command code Simpletron.BRANCH, BRANCHNEG, or BRANCHZERO
	//post: compiled goto command added to machineCodeArr
	private void compileGoToCommand(int lineNumber, int command) {
		TableEntry entry = table.get(lineNumber, TableEntry.LINE);
		if (entry == null) { //not in the table yet, it must be a forward reference....the second pass will get it
			flags[instructionCounter] = lineNumber;
			machineCodeArr[instructionCounter++] = command*1000;
		} else {
			int instruction = command*1000 + entry.getLocation();
			machineCodeArr[instructionCounter++] = instruction;
		}
	}


	//pre: operandLeft, operandRight == variable or constant, operator == "<=, >=, !=, ==, >, <", and location for control to be transferred if true
	//post: compiled if command added to machineCodeArr
	private void compileIfCommand(String operandLeft, String operator, String operandRight, int location) {
		TableEntry leftEntry;
		TableEntry rightEntry;

		//loads the operand entries from the symbol table, 
		if (Character.isDigit(operandLeft.charAt(0))) {
			leftEntry = getEntry(Integer.parseInt(operandLeft), TableEntry.CONSTANT);
		} else {
			leftEntry = getEntry((int)operandLeft.charAt(0), TableEntry.VARIABLE);
		}

		if (Character.isDigit(operandRight.charAt(0))) {
			rightEntry = getEntry(Integer.parseInt(operandRight), TableEntry.CONSTANT);
		} else {
			rightEntry = getEntry((int)operandRight.charAt(0), TableEntry.VARIABLE);
		}

		/*the relational operators are simulated using a combination of the BRANCHNEG and BRANCHZERO operations. */
		if (operator.equals("==")) 
		{
			machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + leftEntry.getLocation();
			machineCodeArr[instructionCounter++] = Simpletron.SUBTRACT*1000 + rightEntry.getLocation();
			compileGoToCommand(location, Simpletron.BRANCHZERO);
		} 
		else if (operator.equals("!=")) 
		{
			compileIfCommand(operandLeft, ">", operandRight, location);
			compileIfCommand(operandLeft, "<", operandRight, location);
		} 
		else if (operator.equals(">=")) 
		{
			machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + rightEntry.getLocation();
			machineCodeArr[instructionCounter++] = Simpletron.SUBTRACT*1000 + leftEntry.getLocation();
			compileGoToCommand(location, Simpletron.BRANCHZERO);
			compileGoToCommand(location, Simpletron.BRANCHNEG);
		} 
		else if (operator.equals("<=")) 
		{
			machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + leftEntry.getLocation();
			machineCodeArr[instructionCounter++] = Simpletron.SUBTRACT*1000 + rightEntry.getLocation();
			compileGoToCommand(location, Simpletron.BRANCHZERO);
			compileGoToCommand(location, Simpletron.BRANCHNEG);
		} 
		else if (operator.equals(">")) 
		{
			machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + rightEntry.getLocation();
			machineCodeArr[instructionCounter++] = Simpletron.SUBTRACT*1000 + leftEntry.getLocation();
			compileGoToCommand(location, Simpletron.BRANCHNEG);
		} 
		else if (operator.equals("<")) 
		{
			machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000 + leftEntry.getLocation();
			machineCodeArr[instructionCounter++] = Simpletron.SUBTRACT*1000 + rightEntry.getLocation();
			compileGoToCommand(location, Simpletron.BRANCHNEG);
		} 
		else 
		{
			System.out.println("Syntax error");
		}
	}

	//pre: must be a full let statment in the format for example y = n1 + n1
	//post: let command compiled and added to machineCodeArr
	private void compileLetCommand(String statement) {
		//take the substring to the right of the = operator, convert it to postfix notation, and break it up into tokens
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
				stack.push(result); //add the result to the stack

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
				stack.push(entry); //add the operand to the stack
			}
		}
		//add the final assignment instruction to the machineCodeArr
		TableEntry solution = stack.pop(); //the solution is on the top of the stack
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

	//pre: entry x, and entry y are in the table, operation is valid
	//post: command is compiled into the machineCodeArr
	private void compileLetSubCommand(TableEntry x, TableEntry y, int operation) {
		machineCodeArr[instructionCounter++] = Simpletron.LOAD*1000  + y.getLocation();
		machineCodeArr[instructionCounter++] = operation*1000   + x.getLocation();
	}


	//post: file created with contents identical to the machineCodeArr
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