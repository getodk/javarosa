package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
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
		
	public String toString () {
		StringBuffer sb = new StringBuffer();
		
		sb.append("{filt-expr:");	
		sb.append(x.toString());
		sb.append(",{");
		for (int i = 0; i < predicates.length; i++) {
			sb.append(predicates[i].toString());
			if (i < predicates.length - 1)
				sb.append(",");
		}
		sb.append("}}");
		
		return sb.toString();
	}
}
