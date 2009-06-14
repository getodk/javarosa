package org.javarosa.xpath;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.Lexer;
import org.javarosa.xpath.parser.Parser;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathParseTool {
	public static String[] xpathClasses = {
			"org.javarosa.xpath.expr.XPathArithExpr",
			"org.javarosa.xpath.expr.XPathBoolExpr",
			"org.javarosa.xpath.expr.XPathCmpExpr",
			"org.javarosa.xpath.expr.XPathEqExpr",
			"org.javarosa.xpath.expr.XPathFilterExpr",
			"org.javarosa.xpath.expr.XPathFuncExpr",
			"org.javarosa.xpath.expr.XPathNumericLiteral",
			"org.javarosa.xpath.expr.XPathNumNegExpr",
			"org.javarosa.xpath.expr.XPathPathExpr",
			"org.javarosa.xpath.expr.XPathStringLiteral",
			"org.javarosa.xpath.expr.XPathUnionExpr",
			"org.javarosa.xpath.expr.XPathVariableReference"
	};

	public static XPathExpression parseXPath (String xpath) throws XPathSyntaxException {
		return Parser.parse(Lexer.lex(xpath));
	}
	
	//for running from command line
//	public static void main (String[] args) {
//		String expr = args[0];
//		Vector tokens;
//		ASTNode root;
//		
//		System.out.println(expr + " ...");
//
//		try {
//			tokens = Lexer.lex(expr);
//			root = Parser.buildParseTree(tokens);
//		} catch (XPathSyntaxException xpse) {
//			System.out.println("   ...syntax error");
//			return;
//		}
//
//		System.out.print("   [");
//		for (int i = 0; i < tokens.size(); i++) {
//			System.out.print(((Token)tokens.elementAt(i)).toString());
//			if (i < tokens.size() - 1)
//				System.out.print(" ");
//		}
//		System.out.println("]");
//		
//		root.print();
//	}

}
