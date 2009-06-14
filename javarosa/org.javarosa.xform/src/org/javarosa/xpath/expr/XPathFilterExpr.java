package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.xpath.XPathUnsupportedException;

public class XPathFilterExpr extends XPathExpression {
	public XPathExpression x;
	public XPathExpression[] predicates;
	
	public XPathFilterExpr () { } //for deserialization
	
	public XPathFilterExpr (XPathExpression x, XPathExpression[] predicates) {
		this.x = x;
		this.predicates = predicates;
	}
	
	public Object eval(IFormDataModel model, EvaluationContext evalContext) {
		throw new XPathUnsupportedException("filter expression");
	}
		
	public String toString () {
		StringBuffer sb = new StringBuffer();
		
		sb.append("{filt-expr:");	
		sb.append(x.toString());
		sb.append(",{");
		for (int i = 0; i < predicates.length; i++) {
			sb.append(predicates[i].toString());
			if (i < predicates.length - 1)
				sb.append(",");
		}
		sb.append("}}");
		
		return sb.toString();
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathFilterExpr) {
			XPathFilterExpr fe = (XPathFilterExpr)o;

			Vector a = new Vector();
			for (int i = 0; i < predicates.length; i++)
				a.addElement(predicates[i]);
			Vector b = new Vector();
			for (int i = 0; i < fe.predicates.length; i++)
				b.addElement(fe.predicates[i]);
			
			return x.equals(fe.x) && ExtUtil.vectorEquals(a, b);
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		x = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
		Vector v = (Vector)ExtUtil.read(in, new ExtWrapListPoly(), pf);
		
		predicates = new XPathExpression[v.size()];
		for (int i = 0; i < predicates.length; i++)
			predicates[i] = (XPathExpression)v.elementAt(i);		
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		Vector v = new Vector();
		for (int i = 0; i < predicates.length; i++)
			v.addElement(predicates[i]);

		ExtUtil.write(out, new ExtWrapTagged(x));
		ExtUtil.write(out, new ExtWrapListPoly(v));
	}
}
