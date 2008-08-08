package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathUnionExpr extends XPathBinaryOpExpr {
	public XPathUnionExpr (XPathExpression a, XPathExpression b) {
		super(a, b);
	}
	
	public Object eval (IFormDataModel model) {
		throw new RuntimeException("XPath evaluation: unsupported construct");
	}

}
