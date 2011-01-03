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
import java.util.Vector;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.instance.FormInstance;
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
	
	public Object pivot (FormInstance model, EvaluationContext evalContext, Vector<Object> pivots, Object sentinal) throws UnpivotableExpressionException {
		Object aval = a.pivot(model, evalContext, pivots, sentinal);
		Object bval = b.pivot(model, evalContext, pivots, sentinal);
		
		if(aval == sentinal || bval == sentinal) {
			throw new UnpivotableExpressionException();
		}
		
		if(aval == null || bval == null) {
			return null;
		}
		
		return this.eval(model, evalContext);
	}
}
