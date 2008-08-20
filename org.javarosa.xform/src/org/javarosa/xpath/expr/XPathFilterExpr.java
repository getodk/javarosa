package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.EvaluationContext;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathFilterExpr extends XPathExpression {
	public XPathExpression x;
	public XPathExpression[] predicates;
	
	public XPathFilterExpr (XPathExpression x, XPathExpression[] predicates) {
		this.x = x;
		this.predicates = predicates;
	}
	
	public Object eval(IFormDataModel model, EvaluationContext evalContext) {
		throw new XPathUnsupportedException("filter expression");
	}
}
