package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.EvaluationContext;

public class XPathNumNegExpr extends XPathUnaryOpExpr {
	public XPathNumNegExpr (XPathExpression a) {
		super(a);
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		double aval = XPathFuncExpr.toNumeric(a.eval(model, evalContext)).doubleValue();
		return new Double(-aval);
	}

	public String toString () {
		return "{unop-expr:num-neg," + a.toString() + "}";
	}
}
