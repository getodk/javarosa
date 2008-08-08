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

		if (aval == null || bval == null) {
			if (aval instanceof String || bval instanceof String) {
				if (aval == null)
					aval = "";
				if (bval == null)
					bval = "";				
			} else {
				return Boolean.FALSE; //non-string comparison always false if one arg is null
			}
		}
		
		if (aval instanceof Double && bval instanceof Double) {
			double fa = ((Double)aval).doubleValue();
			double fb = ((Double)bval).doubleValue();
			eq = (fa == fb);
		} else if (aval instanceof String && bval instanceof String) {
			String sa = (String)aval;
			String sb = (String)bval;
			eq = (sa.equals(sb));
		} else if (aval instanceof Boolean && bval instanceof Boolean) {
			boolean ba = ((Boolean)aval).booleanValue();
			boolean bb = ((Boolean)bval).booleanValue();
			eq = (ba == bb);
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch");
		}
		
		return new Boolean(equal ? eq : !eq);
	}

}
