package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathEqExpr extends XPathBinaryOpExpr {
	public boolean equal;

	public XPathEqExpr (boolean equal, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.equal = equal;
	}
	
	public Object eval (IFormDataModel model) {
		Object aval = a.eval(model);
		Object bval = b.eval(model);
		boolean eq = false;

		if (aval instanceof Boolean && !(bval instanceof Boolean)) {
			bval = XPathFuncExpr.toBoolean(bval);
		} else if (bval instanceof Boolean && !(aval instanceof Boolean)) {
			aval = XPathFuncExpr.toBoolean(aval);
		} else if (aval instanceof Double && !(bval instanceof Double)) {
			bval = XPathFuncExpr.toNumeric(bval);
		} else if (bval instanceof Double && !(aval instanceof Double)) {
			aval = XPathFuncExpr.toNumeric(aval); 
		} else {
			aval = XPathFuncExpr.toString(aval);
			bval = XPathFuncExpr.toString(bval);			
		}
				
		if (aval instanceof Boolean && bval instanceof Boolean) {
			boolean ba = ((Boolean)aval).booleanValue();
			boolean bb = ((Boolean)bval).booleanValue();
			eq = (ba == bb);
		} else if (aval instanceof Double && bval instanceof Double) {
			double fa = ((Double)aval).doubleValue();
			double fb = ((Double)bval).doubleValue();
			eq = (fa == fb);
		} else if (aval instanceof String && bval instanceof String) {
			String sa = (String)aval;
			String sb = (String)bval;
			eq = (sa.equals(sb));
		}
		
		return new Boolean(equal ? eq : !eq);
	}

}
