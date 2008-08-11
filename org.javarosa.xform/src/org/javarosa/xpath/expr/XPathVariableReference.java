package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathVariableReference extends XPathExpression {
    public XPathQName id;

    public XPathVariableReference (XPathQName id) {
    	this.id = id;
    }
    
	public Object eval (IFormDataModel model) {
		throw new XPathUnsupportedException("variable reference");
	}

}
