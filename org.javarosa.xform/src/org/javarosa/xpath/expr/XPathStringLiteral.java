package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathStringLiteral extends XPathExpression {
	public String s;

	public XPathStringLiteral (String s) {
		this.s = s;
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		return s;
	}

	public String toString () {
		return "{str:\'" + s + "\'}"; //TODO: s needs to be escaped (' -> \'; \ -> \\)
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
	throws IOException, InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, s);
	}
}