package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathNumNegExpr extends XPathUnaryOpExpr {
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
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
	throws IOException, InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
	}
}
