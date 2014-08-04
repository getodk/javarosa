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

public class XPathBoolExpr extends XPathBinaryOpExpr {
	public static final int AND = 0;
	public static final int OR = 1;

	public int op;

	public XPathBoolExpr () { } //for deserialization

	public XPathBoolExpr (int op, XPathExpression a, XPathExpression b) {
		super (a, b);
		this.op = op;
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
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
	
	public boolean equals (Object o) {
		if (o instanceof XPathBoolExpr) {
			XPathBoolExpr x = (XPathBoolExpr)o;
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
