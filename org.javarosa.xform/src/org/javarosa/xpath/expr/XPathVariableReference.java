package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathVariableReference extends XPathExpression {
    public XPathQName id;

    public XPathVariableReference (XPathQName id) {
    	this.id = id;
    }
    
	public Object eval (IFormDataModel model) {
		throw new RuntimeException("XPath evaluation: unsupported construct [variable reference]");
	}

}
