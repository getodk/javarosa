package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathUnionExpr extends XPathBinaryOpExpr {
	public XPathUnionExpr (XPathExpression a, XPathExpression b) {
		super(a, b);
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		throw new XPathUnsupportedException("nodeset union operation");
	}

	public String toString () {
		return super.toString("union");
	}
}
