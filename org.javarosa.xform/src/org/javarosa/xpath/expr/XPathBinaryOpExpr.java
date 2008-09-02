package org.javarosa.xpath.expr;

public abstract class XPathBinaryOpExpr extends XPathOpExpr {
	public XPathExpression a, b;

	public XPathBinaryOpExpr (XPathExpression a, XPathExpression b) {
		this.a = a;
		this.b = b;
	}
	
	public String toString (String op) {
		return "{binop-expr:" + op + "," + a.toString() + "," + b.toString() + "}";
	}
}
