import java.util.Stack;

public class PostfixEvaluator {
	private String expression;

	public PostfixEvaluator(String postFixExpression) {
		expression = postFixExpression;
	}

	public int evaluate() {
		Stack<Integer> stack = new Stack<>();
		char c;

		String [] expressionArr = expression.split(" ");
		String string;
		int x, y;

		for (int i = 0; i < expressionArr.length; i++) {
			string = expressionArr[i];
			//could be a one digit number, a variable, or an operator
			if (string.length() == 1)
				//operator: take the top two items evaluate them using the operand, and push the result to the stack
				if (isOperator(string.charAt(0))) {
					char operator = string.charAt(0);
					int result;

					if (stack.empty())
						x = 0;
					else 
						x = stack.pop();

					if (stack.empty())
						y = 0;
					else 
						y = stack.pop();

					switch (operator) {
						case '*': stack.push(y * x); break;
						case '/': stack.push(y / x); break;
						case '+': stack.push(y + x); break;
						case '-': stack.push(y - x); break;
						case '%': stack.push(y % x); break;
					}


				} else { //must be a number
					stack.push(Integer.parseInt(string));
				}
			else //must be a number
				stack.push(Integer.parseInt(string))	;
		}
		return stack.pop();
	}


	private static boolean isOperator(char c) {
		if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^')
			return true;

		return false;
	}

	public static void main(String [] args) {
		PostfixEvaluator test = new PostfixEvaluator("625 - 20 + 5 * 8 4 / - ");
		System.out.println(test.evaluate());
	}



}