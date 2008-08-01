package org.javarosa.xpath.expr;

public class XPathVariableReference extends XPathExpression {
    public XPathQName id;

    public XPathVariableReference (XPathQName id) {
	this.id = id;
    }
}
