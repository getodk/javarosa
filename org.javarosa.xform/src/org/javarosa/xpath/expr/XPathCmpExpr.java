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

}
