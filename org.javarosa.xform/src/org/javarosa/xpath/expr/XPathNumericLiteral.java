package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathNumericLiteral extends XPathExpression {
	public double d;

	public XPathNumericLiteral (Double d) {
		this.d = d.doubleValue();
	}
	
	public Object eval (IFormDataModel model) {
		return new Double(d);
	}

}
