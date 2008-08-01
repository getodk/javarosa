package org.javarosa.xpath.expr;

public class XPathCmpExpr extends XPathBinaryOpExpr {
	public static final int LT = 0;
	public static final int GT = 1;
	public static final int LTE = 2;
	public static final int GTE = 3;

	public int op;

	public XPathCmpExpr (int op, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.op = op;
	}
}
