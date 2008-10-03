package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;
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
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
	}
}
