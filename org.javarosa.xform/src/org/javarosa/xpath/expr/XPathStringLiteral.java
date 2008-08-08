package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathStringLiteral extends XPathExpression {
	public String s;

	public XPathStringLiteral (String s) {
		this.s = s;
	}
	
	public Object eval (IFormDataModel model) {
		return s;
	}

}
