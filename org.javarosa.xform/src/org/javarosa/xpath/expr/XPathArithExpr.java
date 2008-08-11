package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.EvaluationContext;

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
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		double aval = XPathFuncExpr.toNumeric(a.eval(model, evalContext)).doubleValue();
		double bval = XPathFuncExpr.toNumeric(b.eval(model, evalContext)).doubleValue();
		
		double result = 0;
		switch (op) {
		case ADD: result = aval + bval; break;
		case SUBTRACT: result = aval - bval; break;
		case MULTIPLY: result = aval * bval; break;
		case DIVIDE: result = aval / bval; break;
		case MODULO: result = aval % bval; break;
		}
		return new Double(result);
	}
}
