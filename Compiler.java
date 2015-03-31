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
		table = new SymbolTable();
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
		boolean isValid = true;
		while (scanner.hasNext()) {
			if (!compileLine(scanner.nextLine()))
				isValid = false;
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
		if (!isValid) {
			System.out.println("\nCompilation Failed!");
		} else {
			writeMachineCode();
			System.out.println("Compilation Successful!");
		}
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
	//      true if no errors in input
	private boolean compileLine(String line) {
		boolean isValid = true;
		String [] tokens = getTokens(line);
		//each line must at least contain two tokens; ex: line# and command
		if (tokens.length < 2) {
			System.out.print("Syntax error: statement format error on line #");
			isValid = false;
		}

		int lineNumber = 0;
	    try {
	    	lineNumber = Integer.parseInt(tokens[0]);
	    } catch (NumberFormatException e) {
	    	System.out.println("No line number found");
	    	return false;
	    }
	    //adds line to the symbol table
		//*very important for goto statements*
		table.put(new TableEntry(lineNumber, TableEntry.LINE, instructionCounter));
		String command = tokens[1];

		if (command.equals(REMARK)) 
		{
		 //ignore the rest, its just a comment for the developer
		}
		else if (command.equals(INPUT)) 
		{
			isValid = compileInputCommand(tokens);
		}
		else if (command.equals(PRINT)) 
		{
			isValid = compilePrintCommand(tokens);
		}
		else if (command.equals(LET))
		{
			String expression = line.substring(line.indexOf("let")+3); //the substring after "let"
			isValid = compileLetCommand(expression);
	    } 
	    else if (command.equals(GOTO))
	    {
	    	compileGoToCommand(Integer.parseInt(tokens[2]), Simpletron.BRANCH);
	    }
	    else if (command.equals(IF))
	    {
	    	isValid = compileIfCommand(tokens);
	    }
	    else if (command.equals(END)) 
	    {
			machineCodeArr[instructionCounter++] = Simpletron.HALT*1000;
		} 
	    else 
	    {
			System.out.print("Command not valid on line #");
			isValid = false;
		}

		//adds line number to unfinished compiler message in helper compile methods
		if (!isValid)
			System.out.println(lineNumber);

		return isValid;
	}

	//post: input command compiled if valid
	private boolean compileInputCommand(String[] tokens) {
		//there must be a variable after the command
		if (tokens.length < 3) {
			System.out.print("Syntax error: no variable after input command on line #");
			return false;
		}
		if (tokens[2].length() != 1) {
			System.out.print("Syntax error: invalid variable format on line #");
			return false;
		}
		int symbol = (int)tokens[2].charAt(0);

		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.READ*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
		return true;
	}

	//post: print command compiled and added to machineCodeArr if valid
	private boolean compilePrintCommand(String[] tokens) {
		//there must be a variable after the command
		if (tokens.length < 3) {
			System.out.print("Syntax error: no variable after print command on line #");
			return false;
		}
		if (tokens[2].length() != 1) {
			System.out.print("Syntax error: invalid variable format on line #");
			return false;
		}

		int symbol = (int)tokens[2].charAt(0);
		
		TableEntry entry = getEntry(symbol, TableEntry.VARIABLE);
		int instruction = Simpletron.WRITE*1000 + entry.getLocation();
		machineCodeArr[instructionCounter++] = instruction;
		return true;
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
	private boolean compileIfCommand(String [] tokens) {
		if (tokens.length < 7) {
			System.out.println("input error on line #");
			return false;
		}

		String operandLeft = tokens[2];
	    String operator = tokens[3];
	    String operandRight = tokens[4];
	    if (!tokens[5].equals(GOTO)) {
	    	System.out.print("If without goto on line #");
	    	return false;
	    }
	    int location = 0;
	    try {
	    	location = Integer.parseInt(tokens[6]);
	    } catch (NumberFormatException e) {
	    	System.out.print("Line number format error on line #");
	    	return false;
	    }

		TableEntry leftEntry;
		TableEntry rightEntry;

		//loads the operand entries from the symbol table, 
		if (Character.isDigit(operandLeft.charAt(0)) || operandLeft.charAt(0) == '-') {
			leftEntry = getEntry(Integer.parseInt(operandLeft), TableEntry.CONSTANT);
		} else {
			leftEntry = getEntry((int)operandLeft.charAt(0), TableEntry.VARIABLE);
		}

		if (Character.isDigit(operandRight.charAt(0)) || operandRight.charAt(0) == '-') {
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
			tokens[3] = ">";
			compileIfCommand(tokens);
			tokens[3] = "<";
			compileIfCommand(tokens);
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
			System.out.print("Operator format error on line #");
			return false;
		}

		return true;
	}

	//pre: must be a full let statment in the format for example y = n1 + n1
	//post: let command compiled and added to machineCodeArr
	//      true if no errors in input
	private boolean compileLetCommand(String statement) {
		if (!isValidLetCommand(statement))
			return false;

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

		return true;
	}

	private boolean isValidLetCommand(String string) {
		//it must contain the assignment operator
		if (string.indexOf("=") == -1)
			return false;

		int count = 0;
		int i = 0;
		for (; string.charAt(i) != '='; i++) {
			if (string.charAt(i) != ' ')
				count++;
		}
		//there can only be one variable to the left of the assignment operator
		if (count != 1)
			return false;
		


		return true;
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
		compiler.compileProgram("example.smp");
	}
}