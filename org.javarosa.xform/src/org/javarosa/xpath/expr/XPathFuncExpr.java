package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathFuncExpr extends XPathExpression {
	public XPathQName id;
	public XPathExpression[] args;

	public XPathFuncExpr (XPathQName id, XPathExpression[] args) {
		this.id = id;
		this.args = args;
	}
	
	public Object eval (IFormDataModel model) {
		if (id.toString().equals("true") && args.length == 0) {
			return Boolean.TRUE;
		} else if (id.toString().equals("false") && args.length == 0) {
			return Boolean.FALSE;
		} else {
			throw new RuntimeException("XPath evaluation: unsupported construct");
		}
	}

	
	
}
