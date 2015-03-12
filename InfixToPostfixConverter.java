import java.util.Stack;

public class InfixToPostfixConverter {

	private InfixToPostfixConverter() {
	}

	public static String getPostfix(String expression) {
		String infix = expression + ")";
		StringBuilder postfix = new StringBuilder("");
		Stack<Character> stack = new Stack<>();

		stack.push('(');
		int i = 0;
		char c;

		while (!stack.isEmpty()) {
			c = infix.charAt(i);
			if (Character.isDigit(c)) {
				//supports numbers larger than 9
				while (Character.isDigit(c)) {
					postfix.append(c);
					c = infix.charAt(++i);
				}
				postfix.append(" ");
				i--;
			}
			else if (c == '(')
				stack.push(c);
			else if (isOperator(c)) {
				while (!stack.isEmpty()) {
					if (isOperator(stack.peek()) && precedence(stack.peek(), c))
						postfix.append(stack.pop() + " ");
					else {
						stack.push(c);
						break;
					}
				}

			} else if (c == ')') {
				while (stack.peek() != '(')
					postfix.append(stack.pop() + " ");
				stack.pop();
			} else if (c != ' ') {  //must be a variable
				postfix.append(c + " ");
			}

			i++;
		}
		return postfix.toString();
	}

	private static boolean precedence(char operator1, char operator2) {
		int op1 = -1;
		int op2 = -1;
		String [] precedenceRules = {"+ -", "* ? % /", "^"};
		for (int i = 0; i < precedenceRules.length; i++) {
			if (precedenceRules[i].indexOf(operator1) != -1)
				op1 = i;

			if (precedenceRules[i].indexOf(operator2) != -1)
				op2 = i;
		}
		if (op1 >= op2)
			return true;
		return false;

	}


	public static boolean isOperator(char c) {
		if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^')
			return true;

		return false;
	}

	public static void main(String [] args) {

		System.out.println(InfixToPostfixConverter.getPostfix("   b + 111"));
	}
}