/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class XPathArithExpr extends XPathBinaryOpExpr {
	public static final int ADD = 0;
	public static final int SUBTRACT = 1;
	public static final int MULTIPLY = 2;
	public static final int DIVIDE = 3;
	public static final int MODULO = 4;

	public int op;

	public XPathArithExpr () { } //for deserialization
	
	public XPathArithExpr (int op, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.op = op;
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		double aval = XPathFuncExpr.toNumeric(a.eval(model, evalContext)).doubleValue();
		double bval = XPathFuncExpr.toNumeric(b.eval(model, evalContext)).doubleValue();
		
		double result = 0;
		switch (op) {
		case ADD: result = aval + bval; break;
		case SUBTRACT: result = aval - bval; break;
		case MULTIPLY: result = aval * bval; break;
		case DIVIDE: result = aval / bval; break;
		case MODULO: result = aval % bval; break;
		}
		return new Double(result);
	}
	
	public String toString () {
		String sOp = null;
		
		switch (op) {
		case ADD: sOp = "+"; break;
		case SUBTRACT: sOp = "-"; break;
		case MULTIPLY: sOp = "*"; break;
		case DIVIDE: sOp = "/"; break;
		case MODULO: sOp = "%"; break;
		}
		
		return super.toString(sOp);
	}

	public boolean equals (Object o) {
		if (o instanceof XPathArithExpr) {
			XPathArithExpr x = (XPathArithExpr)o;
			return super.equals(o) && op == x.op;
		} else {
			return false;
		}
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
