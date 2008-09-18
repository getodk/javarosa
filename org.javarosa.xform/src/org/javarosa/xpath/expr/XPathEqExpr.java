package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;

public class XPathEqExpr extends XPathBinaryOpExpr {
	public boolean equal;

	public XPathEqExpr (boolean equal, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.equal = equal;
	}
	
	public Object eval (IFormDataModel model, EvaluationContext evalContext) {
		Object aval = a.eval(model, evalContext);
		Object bval = b.eval(model, evalContext);
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
}
