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
import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.pivot.CmpPivot;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathNodeset;

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
	

	public Object pivot (FormInstance model, EvaluationContext evalContext, List<Object> pivots, Object sentinal) throws UnpivotableExpressionException {
		Object aval = a.pivot(model, evalContext, pivots, sentinal);
		Object bval = b.pivot(model, evalContext, pivots, sentinal);
		if(bval instanceof XPathNodeset) {
			bval = ((XPathNodeset)bval).unpack();
		}
		
		if(handled(aval, bval, sentinal, pivots) || handled(bval, aval, sentinal, pivots)) { return null; }
		
		return this.eval(model, evalContext);
	}
	
	private boolean handled(Object a, Object b, Object sentinal, List<Object> pivots) throws UnpivotableExpressionException {
		if(sentinal == a) {
			if(b == null) {
				//Can't pivot on an expression which is derived from pivoted expressions
				throw new UnpivotableExpressionException();
			} else if(sentinal == b) {
				//WTF?
				throw new UnpivotableExpressionException();
			} else {
				Double val = null;
				//either of
				if(b instanceof Double) {
					val = (Double)b;
				} else {
					//These are probably the 
					if(b instanceof Integer) {
						val = new Double(((Integer) b).doubleValue());
					} else if(b instanceof Long) {
						val = new Double(((Long) b).doubleValue());
					} else if(b instanceof Float) {
						val = new Double(((Float) b).doubleValue());
					} else if(b instanceof Short) {
						val = new Double(((Short) b).shortValue());
					} else if(b instanceof Byte) {
						val = new Double(((Byte) b).byteValue());
					} else {
						if(b instanceof String) {
							try {
								//TODO: Too expensive?
								val = (Double)new DecimalData().cast(new UncastData((String)b)).getValue();
							} catch(Exception e) {
								throw new UnpivotableExpressionException("Unrecognized numeric data in cmp expression: " + b);
							}
						} else {
							throw new UnpivotableExpressionException("Unrecognized numeric data in cmp expression: " + b);
						}
					}
				}
				
				
				pivots.add(new CmpPivot(val.doubleValue(), op));
				return true;
			}
		} 
		return false;
	}
}
