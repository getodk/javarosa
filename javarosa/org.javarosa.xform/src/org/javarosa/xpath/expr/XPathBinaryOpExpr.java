package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;

public abstract class XPathBinaryOpExpr extends XPathOpExpr {
	public XPathExpression a, b;

	public XPathBinaryOpExpr () { } //for deserialization of children
	
	public XPathBinaryOpExpr (XPathExpression a, XPathExpression b) {
		this.a = a;
		this.b = b;
	}
	
	public String toString (String op) {
		return "{binop-expr:" + op + "," + a.toString() + "," + b.toString() + "}";
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathBinaryOpExpr) {
			XPathBinaryOpExpr x = (XPathBinaryOpExpr)o;
			return a.equals(x.a) && b.equals(x.b);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		a = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
		b = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(a));
		ExtUtil.write(out, new ExtWrapTagged(b));
	}
}
