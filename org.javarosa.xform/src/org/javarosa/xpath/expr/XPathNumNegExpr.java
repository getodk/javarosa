package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathNumNegExpr extends XPathUnaryOpExpr {
	public XPathNumNegExpr (XPathExpression a) {
		super(a);
	}
	
	public Object eval (IFormDataModel model) {
		Object aval = a.eval(model);
		
		if (aval instanceof Double) {
			return new Double(-((Double)aval).doubleValue());
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch");
		}
	}

}
