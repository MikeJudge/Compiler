/**************************************************************************
* This class runs machine level programs. The machine level programs      *
* are written as a list of five digit numbers with an operation code     *
* corresponding to the first two digits of a five digit decimal number,   * 
* and the last three digits corresponding to an operand.                    *
**************************************************************************/
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.io.PrintWriter;

public class Simpletron {
	private static final int MEMORY_SIZE   = 1000;
	private static final int MAX_WORD_SIZE = 99999;
	private static final int MIN_WORD_SIZE = -99999;

	//operation code constants
	public static final int READ       = 10;
	public static final int WRITE      = 11;
	public static final int NEWLINE    = 12;

	public static final int LOAD  = 20;
	public static final int STORE = 21;

	public static final int ADD       = 30;
	public static final int SUBTRACT  = 31;
	public static final int DIVIDE    = 32;
	public static final int MULTIPLY  = 33;
	public static final int REMAINDER = 34;
	public static final int POWER     = 35;

	public static final int BRANCH     = 40;
	public static final int BRANCHNEG  = 41;
	public static final int BRANCHZERO = 42;
	public static final int HALT       = 43;


	private int[] memory;			 //program is stored here
	private int accumulator;
	private int instructionCounter;  //location in memory whose instruction is being performed now

	private int operationCode;       //operation being currently performed, 1st two numbers of instructionRegister
	private int operand;		     //memory location where operation is being operated on last three of instructionRegister
	private int instructionRegister; //full instruction word

	private String fileName;
	private PrintWriter writer;


	public Simpletron(String fileName) {
		memory = new int[MEMORY_SIZE];
		accumulator 	    = 0;
		instructionCounter  = 0;
		operationCode       = 0;
		operand             = 0;
		instructionRegister = 0;
		this.fileName = fileName;
	}

	public void loadProgram () {
		clearMemory();
		try {
			Scanner scanner = new Scanner(new File(fileName));
			int i = 0;
			while (scanner.hasNext()) {
				storeWord(i++, scanner.nextInt());
			}
			scanner.close();
		} catch (FileNotFoundException e) {fatalError("*** error loading program ***");}
	}

	//post: sets all member variables to 0
	private void clearMemory() {
		Arrays.fill(memory, 0);
		accumulator 	    = 0;
		instructionCounter  = 0;
		operationCode       = 0;
		operand             = 0;
		instructionRegister = 0;
	}

	//pre:  index, and word are in range
	//post: word is stored in memory
	private void storeWord(int index, int word) {
		if (word > MAX_WORD_SIZE || word < MIN_WORD_SIZE) {
			fatalError("*** overflow occured ***");
		}
		if (index > (MEMORY_SIZE-1) || index < 0) {
			fatalError("*** index out of bounds ***");
		}

		memory[index] = word;
	}

	//returns false if the accumulator has overflowed the max or min WORD_SIZE
	private boolean isAccumulatorValid() {
		if (accumulator > MAX_WORD_SIZE || accumulator < MIN_WORD_SIZE)
			return false;
		return true;
	}

	private void startPrintWriter() {
		try {
			writer = new PrintWriter(fileName.substring(0,fileName.length()-3) + "out");
		} catch (FileNotFoundException io) {fatalError("*** error opening output stream ***");}
	}

	public void executeProgram() {
		Scanner input = new Scanner(System.in);
		startPrintWriter();

		while (true) {
			//case when branch jumps the program out of bounds
			if (instructionCounter >= MEMORY_SIZE || instructionCounter < 0)
				fatalError("*** program execution failed ***");

			instructionRegister = memory[instructionCounter];
			operationCode = instructionRegister / 1000;
			operand = instructionRegister % 1000;

			if (!isAccumulatorValid())
				fatalError("*** Overflow occured ***");

			if (operand >= MEMORY_SIZE || operand < 0)
				fatalError("*** operand index out of bounds ***");

			switch (operationCode) {
				//condense code branch, and branchneg are the only ops that don't instructioncounter++
				case READ:        printString("Enter an integer: ");
							      int n = input.nextInt();
							      storeWord(operand, n);
							      writer.println(""+ n);
							      instructionCounter++;
							      break;
				case WRITE:       printString(memory[operand] + "");
							      instructionCounter++;
							      break;
			    case NEWLINE:     printString("\n");
			                      instructionCounter++;
			                      break;
				case LOAD:        accumulator = memory[operand];
							      instructionCounter++;
							      break;
				case STORE:       storeWord(operand, accumulator);
							      instructionCounter++;
							      accumulator = 0;
							      break;
				case ADD:         accumulator += memory[operand];
								  instructionCounter++;
								  break;
				case SUBTRACT:    accumulator -= memory[operand];
								  instructionCounter++;
								  break;
				case DIVIDE:      if (memory[operand] == 0) //can't divide by zero
        					      	 fatalError("*** attempt to divide by zero ***");
								  accumulator /= memory[operand];     
						          instructionCounter++;
						          break;
				case MULTIPLY:    accumulator *= memory[operand];
								  instructionCounter++;
								  break;
				case REMAINDER:   if (memory[operand] == 0) //can't divide by zero
									 fatalError("*** attempt to divide by zero ***");
								  accumulator %= memory[operand];
								  instructionCounter++;
								  break;
				case POWER:       accumulator = (int)Math.pow(accumulator,memory[operand]);
								  instructionCounter++;
								  break;
				case BRANCH:      instructionCounter = operand;
							      break;
				case BRANCHNEG:   if (accumulator < 0) 
									  instructionCounter = operand;
							      else
							      	  instructionCounter++;
				                  break;
				case BRANCHZERO:  if (accumulator == 0) 
									  instructionCounter = operand;
								  else
								  	  instructionCounter++;
								  break;
				case HALT:        printString("*** Simpletron execution terminated ***");
				                  writer.close();
							      return;
				//invalid operation code
				default:		  fatalError("*** Invalid operation code ***");

			}
		}
	}

	private void printString(String string) {
		System.out.print(string);
		writer.print(string);
	}


	//post: all of the variables are printed to the screen
	private void dumpMemory() {
		printString("REGISTERS:\n");
		printString("accumulator" + "          " + formatWord(accumulator) + "\n");
		printString("instructionCounter" + "   " + "    " + formatCode(instructionCounter) + "\n");
		printString("instructionRegister" + "  " + formatWord(instructionRegister) + "\n");
		printString("operationCode" + "        " + "    " + formatCode(operationCode) + "\n");
		printString("operand" + "              " + "    " + formatCode(operand) + "\n");
		printString("\n" + "MEMORY:" + "\n");
		printString("   ");

		final int DIMEN = 10;
		for (int i = 0; i < DIMEN; i++) {
			printString("      " + i);
		}
		printString("\n");

		for (int i = 0; i < DIMEN*DIMEN; i++) {
			if (i == 0) {
				printString("  0");
			} else if (i < 10) {
				printString(" " + i + "0");
			} else {
				printString(i + "0");
			}
			for (int n = 0; n < DIMEN; n++) {
				printString(" " + formatWord(memory[i*DIMEN + n]));
			}
			printString("\n");
		}
		writer.close();
	}

	//post: returns word in +wxyz or -wxyz format
	private String formatWord(int word) {
		String s = Math.abs(word) + "";
		while (s.length() < ((MAX_WORD_SIZE+"").length()))
			s = "0" + s;
		if (word >= 0)
			s = "+" + s;
		else
			s = "-" + s;

		return s;
	}

    //post: returns a two digit string with preceding 0's if necessary
	private String formatCode(int code) {
		String s = code + "";
		while (s.length() < 2)
			s = "0" + s;
		return s;
	}

	private void fatalError(String errorMessage) {
		System.out.println(errorMessage);
		System.out.println("*** Simpletron execution abnormally terminated ***");
		if (writer != null) {
			writer.println(errorMessage);
			writer.println("*** Simpletron execution abnormally terminated ***");
			dumpMemory();
		}
		System.exit(-1);
	}


	public static void main(String [] args) {
		Simpletron test = new Simpletron("test.txt");
		test.loadProgram();
		test.executeProgram();
		
		
		
	}


}