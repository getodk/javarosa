package org.javarosa.xpath.parser.ast;

import java.util.Vector;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodePredicate extends ASTNode {
	public ASTNode expr;
	
	public Vector getChildren () {
		Vector v = new Vector();
		v.addElement(expr);
		return v;
	}
	
	public XPathExpression build() throws XPathSyntaxException {
		return expr.build();
	}
}
