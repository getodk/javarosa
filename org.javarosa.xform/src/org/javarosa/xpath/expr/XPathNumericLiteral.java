package org.javarosa.xpath.expr;

public class XPathNumericLiteral extends XPathExpression {
	public double d;

	public XPathNumericLiteral (Double d) {
		this.d = d.doubleValue();
	}
}
