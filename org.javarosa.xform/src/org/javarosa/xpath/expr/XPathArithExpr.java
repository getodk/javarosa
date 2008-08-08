package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

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
	
	public Object eval (IFormDataModel model) {
		Object aval = a.eval(model);
		Object bval = b.eval(model);
		
		if (!(aval instanceof Double && bval instanceof Double)) {
			throw new RuntimeException("XPath evaluation: type mismatch");
		}
		
		double fa = ((Double)aval).doubleValue();
		double fb = ((Double)bval).doubleValue();
		
		double fc = 0;
		switch (op) {
		case ADD: fc = fa + fb; break;
		case SUBTRACT: fc = fa - fb; break;
		case MULTIPLY: fc = fa * fb; break;
		case DIVIDE: fc = fa / fb; break;
		case MODULO: fc = fa % fb; break;
		}
		return new Double(fc);
	}
}
