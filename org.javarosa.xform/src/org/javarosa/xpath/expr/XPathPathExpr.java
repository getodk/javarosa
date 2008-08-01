package org.javarosa.xpath.expr;

public class XPathPathExpr extends XPathExpression {
	public static final int INIT_CONTEXT_ROOT = 0;
	public static final int INIT_CONTEXT_RELATIVE = 1;
	public static final int INIT_CONTEXT_EXPR = 2;

	public int init_context;
	public XPathStep[] steps;

	//for INIT_CONTEXT_EXPR only
	public XPathExpression expr;
	public XPathExpression[] predicates;

	public XPathPathExpr (int init_context, XPathStep[] steps) {
		this.init_context = init_context;
		this.steps = steps;
	}

	public XPathPathExpr (XPathExpression expr, XPathExpression[] predicates, XPathStep[] steps) {
		this(INIT_CONTEXT_EXPR, steps);
		this.expr = expr;
		this.predicates = predicates;
	}
}
