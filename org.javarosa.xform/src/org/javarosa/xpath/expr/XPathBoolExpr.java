package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathBoolExpr extends XPathBinaryOpExpr {
	public static final int AND = 0;
	public static final int OR = 1;

	public int op;

	public XPathBoolExpr (int op, XPathExpression a, XPathExpression b) {
		super (a, b);
		this.op = op;
	}
	
	public Object eval (IFormDataModel model) {
		Object aval = a.eval(model);
		Object bval = b.eval(model);
		
		//no short-circuiting support right now
		
		if (!(aval instanceof Boolean && bval instanceof Boolean)) {
			throw new RuntimeException("XPath evaluation: type mismatch");
		}
		
		boolean ba = ((Boolean)aval).booleanValue();
		boolean bb = ((Boolean)bval).booleanValue();
		
		boolean bc = false;
		switch (op) {
		case AND: bc = ba && bb; break;
		case OR: bc = ba || bb; break;
		}
		return new Boolean(bc);
	}

}
