package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathNumNegExpr extends XPathUnaryOpExpr {
	public XPathNumNegExpr () { } //for deserialization

	public XPathNumNegExpr (XPathExpression a) {
		super(a);
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		double aval = XPathFuncExpr.toNumeric(a.eval(model, evalContext)).doubleValue();
		return new Double(-aval);
	}

	public String toString () {
		return "{unop-expr:num-neg," + a.toString() + "}";
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathNumNegExpr) {
			return super.equals(o);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
	}
}
