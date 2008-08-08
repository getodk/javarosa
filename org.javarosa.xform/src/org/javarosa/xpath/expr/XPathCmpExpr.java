package org.javarosa.xpath.expr;

import org.javarosa.core.model.IFormDataModel;

public class XPathCmpExpr extends XPathBinaryOpExpr {
	public static final int LT = 0;
	public static final int GT = 1;
	public static final int LTE = 2;
	public static final int GTE = 3;

	public int op;

	public XPathCmpExpr (int op, XPathExpression a, XPathExpression b) {
		super(a, b);
		this.op = op;
	}
	
	public Object eval (IFormDataModel model) {
		Object aval = a.eval(model);
		Object bval = b.eval(model);
		boolean result = false;

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
			
			switch (op) {
			case LT: result = fa < fb; break;
			case GT: result = fa > fb; break;
			case LTE: result = fa <= fb; break;
			case GTE: result = fa >= fb; break;
			}
		} else if (aval instanceof String && bval instanceof String) {
			String sa = (String)aval;
			String sb = (String)bval;
			int cmp = sa.compareTo(sb);
			
			switch (op) {
			case LT: result = (cmp < 0); break;
			case GT: result = (cmp > 0); break;
			case LTE: result = (cmp <= 0); break;
			case GTE: result = (cmp >= 0); break;
			}
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch");
		}
		
		return new Boolean(result);
	}

}
