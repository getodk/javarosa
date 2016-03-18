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

public class XPathEqExpr extends XPathBinaryOpExpr {
	public boolean equal;

	public XPathEqExpr () { } //for deserialization
	
	public XPathEqExpr (boolean equal, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.equal = equal;
	}
	
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		Object aval = XPathFuncExpr.unpack(a.eval(model, evalContext));
		Object bval = XPathFuncExpr.unpack(b.eval(model, evalContext));
		boolean eq = false;

		if (aval instanceof Boolean || bval instanceof Boolean) {
			if (!(aval instanceof Boolean)) {
				aval = XPathFuncExpr.toBoolean(aval);
			} else if (!(bval instanceof Boolean)) {
				bval = XPathFuncExpr.toBoolean(bval);
			}

			boolean ba = ((Boolean)aval).booleanValue();
			boolean bb = ((Boolean)bval).booleanValue();
			eq = (ba == bb);
		} else if (aval instanceof Double || bval instanceof Double) {
			if (!(aval instanceof Double)) {
				aval = XPathFuncExpr.toNumeric(aval);
			} else if (!(bval instanceof Double)) {
				bval = XPathFuncExpr.toNumeric(bval); 
			} 

			double fa = ((Double)aval).doubleValue();
			double fb = ((Double)bval).doubleValue();
			eq = Math.abs(fa - fb) < 1.0e-12;
		} else {
			aval = XPathFuncExpr.toString(aval);
			bval = XPathFuncExpr.toString(bval);
			eq = (aval.equals(bval));
		}
		
		return new Boolean(equal ? eq : !eq);
	}

	public String toString () {
		return super.toString(equal ? "==" : "!=");
	}
	
	public boolean equals (Object o) {
		if (o instanceof XPathEqExpr) {
			XPathEqExpr x = (XPathEqExpr)o;
			return super.equals(o) && equal == x.equal;
		} else {
			return false;
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		equal = ExtUtil.readBool(in);
		super.readExternal(in, pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeBool(out, equal);
		super.writeExternal(out);
	}
}
