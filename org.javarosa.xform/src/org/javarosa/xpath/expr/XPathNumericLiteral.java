package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathNumericLiteral extends XPathExpression {
	public double d;

	public XPathNumericLiteral (Double d) {
		this.d = d.doubleValue();
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		return new Double(d);
	}

	public String toString () {
		return "{num:" + Double.toString(d) + "}";
	}
	
	public void readExternal(DataInputStream in)
	throws IOException, InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		if (in.readByte() == 0x00) {
			d = ExtUtil.readNumeric(in);
		} else {
			d = ExtUtil.readDecimal(in);
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
	throws IOException, InstantiationException, IllegalAccessException,
	UnavailableExternalizerException {
		readExternal(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		if (Math.abs(d - (int)d) < 1.0e-12) {
			out.writeByte(0x00);
			ExtUtil.writeNumeric(out, (int)d);
		} else {
			out.writeByte(0x01);
			ExtUtil.writeDecimal(out, d);
		}
	}
}
