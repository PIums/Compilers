import java.util.*;
import java.io.*;

public class Parse {
	static final int LBRACK = 1, RBRACK = 2, SYSOUT = 3, LPAREN = 4, RPAREN = 5,
		SEMIC = 6, IF = 7, ELSE = 8, WHILE = 9, TRUE = 10, FALSE = 11, BANG = 12;

	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static ArrayList<Integer> order = new ArrayList<Integer>();
	public static void main(String[] args) throws IOException {
		String cur = new String();
		String s;
		while ((s = br.readLine()) != null) {
			//s = s.replaceAll("\\s+", "");
			int val = -1;
			int n = s.length();
			for (int i=0; i<n; i++) {
                if (cur == "" && Character.isWhitespace(s.charAt(i))) continue;
				cur += s.charAt(i);
				switch (cur) {
					case "{":
						val = LBRACK; break;
					case "}":
						val = RBRACK; break;
					case "System.out.println":
						val = SYSOUT; break;
					case "(":
						val = LPAREN; break;
					case ")":
						val = RPAREN; break;
					case ";":	
						val = SEMIC; break;
					case "if":
						val = IF; break;
					case "else":
						val = ELSE; break;
					case "while":
						val = WHILE; break;
					case "true":
						val = TRUE; break;
					case "false":
						val = FALSE; break;
					case "!":
						val = BANG; break;
				}

				if (val != -1) {
					order.add(val);
					cur = "";
					val = -1;
				}
			}
		}

		if (cur != "") {
			System.out.println("Parse error");
			return;
		}

		S();

		if (ans && order.isEmpty()) {
			System.out.println("Program parsed successfully");
		} else {
			System.out.println("Parse error");
		}

	}

	public static boolean ans = true;

	public static void eat(int tok) {
		if (order.size() == 0) {
		    System.out.println("Parse error");
			System.exit(1);
		}
		int top = order.get(0);
		if (top != tok) {
			System.out.println("Parse error");
			System.exit(1);
		}
		order.remove(0);
	}

	public static void S() {
		if (order.isEmpty()) {
			System.out.println("Parse error");
			System.exit(1);
		}

		int tok = order.get(0);
		switch (tok) {
			case LBRACK:
				eat(LBRACK); L(); eat(RBRACK); break;
			case SYSOUT:
				eat(SYSOUT); eat(LPAREN); E(); eat(RPAREN); eat(SEMIC); break;
			case IF:
				eat(IF); eat(LPAREN); E(); eat(RPAREN); S(); eat(ELSE); S(); break;
			case WHILE:
				eat(WHILE); eat(LPAREN); E(); eat(RPAREN); S(); break;
            default:
                System.out.println("Parse error");
                System.exit(1);
		}
	}

	public static void L() {
		if (order.isEmpty()) {
			return;
		}


		int tok = order.get(0);
		switch (tok) {
			case LBRACK:
			case SYSOUT:
			case IF:
			case WHILE:
				S(); L();
				return;
			default:
				return;
		}
	}

	public static void E() {
		if (order.isEmpty()) {
			System.out.println("Parse error");
			System.exit(1);
		}

		int tok = order.get(0);
		switch(tok) {
			case TRUE:
				eat(TRUE); break;
			case FALSE:
				eat(FALSE); break;
			case BANG:
				eat(BANG); E(); break;
            default:
                System.out.println("Parse error");
                System.exit(1);
		}
	}
}