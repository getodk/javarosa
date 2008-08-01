package org.javarosa.xpath.expr;

public class XPathArithExpr extends XPathBinaryOpExpr {
	public static final int ADD = 0;
	public static final int SUBTRACT = 1;
	public static final int MULTIPLY = 2;
	public static final int DIVIDE = 3;
	public static final int MODULO = 4;

	public int op;

	public XPathArithExpr (int op, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.op = op;
	}
}
