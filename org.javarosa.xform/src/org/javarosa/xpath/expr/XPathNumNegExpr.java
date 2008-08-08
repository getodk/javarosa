package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathNumNegExpr extends XPathUnaryOpExpr {
	public XPathNumNegExpr (XPathExpression a) {
		super(a);
	}
	
	public Object eval (IFormDataModel model) {
		double aval = XPathFuncExpr.toNumeric(a.eval(model)).doubleValue();
		return new Double(-aval);
	}

}
