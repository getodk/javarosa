package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;

public abstract class XPathUnaryOpExpr extends XPathOpExpr {
	public XPathExpression a;

	public XPathUnaryOpExpr () { } //for deserialization of children

	public XPathUnaryOpExpr (XPathExpression a) {
		this.a = a;
	}

	public boolean equals (Object o) {
		if (o instanceof XPathUnaryOpExpr) {
			XPathUnaryOpExpr x = (XPathUnaryOpExpr)o;
			return a.equals(x.a);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		a = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(a));
	}
}
