import java.util.Stack;

public class InfixToPostfixConverter {
	String expression;

	public InfixToPostfixConverter(String expression) {
		this.expression = expression;
	}

	public String getPostfix() {
		String infix = expression + ")";
		StringBuilder postfix = new StringBuilder("");
		Stack<Character> stack = new Stack<>();

		stack.push('(');
		int i = 0;
		char c;

		while (!stack.isEmpty()) {
			c = infix.charAt(i);
			if (Character.isDigit(c)) {
				//loop supports numbers larger than 9
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
					if (isOperator(stack.peek()) && precedence(c, stack.peek()))
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
			}

			i++;
		}
		return postfix.toString();
	}

	private boolean precedence(char operator1, char operator2) {
		int op1 = -1;
		int op2 = -1;
		String [] precedenceRules = {"^", "*?%", "+-"};
		for (int i = 0; i < precedenceRules.length; i++) {
			if (precedenceRules[i].indexOf(operator1) != -1)
				op1 = precedenceRules[i].indexOf(operator1);

			if (precedenceRules[i].indexOf(operator2) != -1)
				op2 = precedenceRules[i].indexOf(operator2);
		}

		if (op1 > op2)
			return true;
		return false;

	}


	private boolean isOperator(char c) {
		if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^')
			return true;

		return false;
	}

	public static void main(String [] args) {
		InfixToPostfixConverter test = new InfixToPostfixConverter("(625+20)* 5 -8/ 4");
		System.out.println(test.getPostfix());
	}
}