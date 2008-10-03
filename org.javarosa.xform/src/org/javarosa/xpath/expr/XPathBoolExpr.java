package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.core.util.externalizable.DeserializationException;

public class XPathBoolExpr extends XPathBinaryOpExpr {
	public static final int AND = 0;
	public static final int OR = 1;

	public int op;

	public XPathBoolExpr (int op, XPathExpression a, XPathExpression b) {
		super (a, b);
		this.op = op;
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		boolean aval = XPathFuncExpr.toBoolean(a.eval(model, evalContext)).booleanValue();
		
		//short-circuiting
		if ((!aval && op == AND) || (aval && op == OR)) {
			return new Boolean(aval);
		}

		boolean bval = XPathFuncExpr.toBoolean(b.eval(model, evalContext)).booleanValue();
		
		boolean result = false;
		switch (op) {
		case AND: result = aval && bval; break;
		case OR: result = aval || bval; break;
		}
		return new Boolean(result);
	}

	public String toString () {
		String sOp = null;
		
		switch (op) {
		case AND: sOp = "and"; break;
		case OR: sOp = "or"; break;
		}
		
		return super.toString(sOp);
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		op = ExtUtil.readInt(in);
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, op);
		super.writeExternal(out);
	}
}
