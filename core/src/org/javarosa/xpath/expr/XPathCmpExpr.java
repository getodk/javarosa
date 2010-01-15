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

public class XPathCmpExpr extends XPathBinaryOpExpr {
	public static final int LT = 0;
	public static final int GT = 1;
	public static final int LTE = 2;
	public static final int GTE = 3;

	public int op;

	public XPathCmpExpr () { } //for deserialization

	public XPathCmpExpr (int op, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.op = op;
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		Object aval = a.eval(model, evalContext);
		Object bval = b.eval(model, evalContext);
		boolean result = false;

		//xpath spec says comparisons only defined for numbers (not defined for strings)
		aval = XPathFuncExpr.toNumeric(aval);
		bval = XPathFuncExpr.toNumeric(bval);
					
		double fa = ((Double)aval).doubleValue();
		double fb = ((Double)bval).doubleValue();

		switch (op) {
		case LT: result = fa < fb; break;
		case GT: result = fa > fb; break;
		case LTE: result = fa <= fb; break;
		case GTE: result = fa >= fb; break;
		}
		
		return new Boolean(result);		
		
//		String sa = (String)aval;
//		String sb = (String)bval;
//		int cmp = sa.compareTo(sb);
//		
//		switch (op) {
//		case LT: result = (cmp < 0); break;
//		case GT: result = (cmp > 0); break;
//		case LTE: result = (cmp <= 0); break;
//		case GTE: result = (cmp >= 0); break;
//		}
	}

	public String toString () {
		String sOp = null;
		
		switch (op) {
		case LT: sOp = "<"; break;
		case GT: sOp = ">"; break;
		case LTE: sOp = "<="; break;
		case GTE: sOp = ">="; break;
		}
		
		return super.toString(sOp);
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathCmpExpr) {
			XPathCmpExpr x = (XPathCmpExpr)o;
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
