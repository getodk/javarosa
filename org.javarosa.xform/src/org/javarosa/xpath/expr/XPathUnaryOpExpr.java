package org.javarosa.xpath.expr;

public abstract class XPathUnaryOpExpr extends XPathOpExpr {
	public XPathExpression a;

	public XPathUnaryOpExpr (XPathExpression a) {
		this.a = a;
	}
}
