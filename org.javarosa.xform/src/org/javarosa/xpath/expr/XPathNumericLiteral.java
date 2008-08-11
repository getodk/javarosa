package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.EvaluationContext;

public class XPathNumericLiteral extends XPathExpression {
	public double d;

	public XPathNumericLiteral (Double d) {
		this.d = d.doubleValue();
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		return new Double(d);
	}

}
