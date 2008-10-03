package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathVariableReference extends XPathExpression {
    public XPathQName id;

    public XPathVariableReference (XPathQName id) {
    	this.id = id;
    }
    
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		throw new XPathUnsupportedException("variable reference");
	}

	public String toString () {
		return "{var:" + id.toString() + "}";
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		id = (XPathQName)ExtUtil.read(in, XPathQName.class);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, id);
	}
}
