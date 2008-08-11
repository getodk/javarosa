package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.EvaluationContext;

public class XPathStringLiteral extends XPathExpression {
	public String s;

	public XPathStringLiteral (String s) {
		this.s = s;
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		return s;
	}

}
