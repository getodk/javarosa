package org.javarosa.xpath.parser.ast;

import java.util.Vector;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathNumNegExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodeUnaryOp extends ASTNode {
	public ASTNode expr;
	public int op;
		
	public Vector getChildren () {
		Vector v = new Vector();
		v.addElement(expr);
		return v;
	}

	public XPathExpression build() throws XPathSyntaxException {
		XPathUnaryOpExpr x;
		if (op == Token.UMINUS) {
			x = new XPathNumNegExpr(expr.build());
		} else {
			throw new XPathSyntaxException();
		}
		return x;
	}
}
