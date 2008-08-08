package org.javarosa.xpath.expr;

import java.util.Date;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.utils.DateUtils;

public class XPathFuncExpr extends XPathExpression {
	public XPathQName id;
	public XPathExpression[] args;

	public XPathFuncExpr (XPathQName id, XPathExpression[] args) {
		this.id = id;
		this.args = args;
	}
	
	public Object eval (IFormDataModel model) {
		String name = id.toString();
		Object[] argVals = new Object[args.length];
		
		for (int i = 0; i < args.length; i++) {
			argVals[i] = args[i].eval(model);
		}
		
		if (name.equals("true") && args.length == 0) {
			return Boolean.TRUE;
		} else if (name.equals("false") && args.length == 0) {
			return Boolean.FALSE;
		} else if (name.equals("boolean") && args.length == 1) {
			return XPathFuncExpr.toBoolean(argVals[0]);
		} else if (name.equals("number") && args.length == 1) {
			return XPathFuncExpr.toNumeric(argVals[0]);
		} else if (name.equals("string") && args.length == 1) {
			return XPathFuncExpr.toString(argVals[0]);			
		} else if (name.equals("date") && args.length == 1) { //non-standard
			return XPathFuncExpr.toDate(argVals[0]);				
			
		} else {
			throw new RuntimeException("XPath evaluation: unsupported construct [function call]");
		}
	}

	public static Boolean toBoolean (Object o) {
		if (o instanceof Boolean) {
			return (Boolean)o;
		} else if (o instanceof Double) {
			double d = ((Double)o).doubleValue();
			return new Boolean(Math.abs(d) > 1.0e-12 && !Double.isNaN(d));
		} else if (o instanceof String) {
			String s = (String)o;
			return new Boolean(s.length() > 0);
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch [cannot convert to boolean]");
		}
	}
	
	public static Double toNumeric (Object o) {
		if (o instanceof Boolean) {
			return new Double(((Boolean)o).booleanValue() ? 1 : 0);
		} else if (o instanceof Double) {
			return (Double)o;
		} else if (o instanceof String) {
			String s = (String)o;
			double d;
			try {
				d = Double.parseDouble(s.trim());
			} catch (NumberFormatException nfe) {
				return new Double(Double.NaN);
			}
			return new Double(d);
		} else if (o instanceof Date) {
			return new Double((((Date)o).getTime() - DateUtils.getDateFromString("1970-01-01").getTime()) / 86400000l);
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch [cannot convert to numeric]");
		}
	}

	public static String toString (Object o) {
		if (o instanceof Boolean) {
			return (((Boolean)o).booleanValue() ? "true" : "false");
		} else if (o instanceof Double) {
			double d = ((Double)o).doubleValue();
			if (Double.isNaN(d)) {
				return "NaN";
			} else if (Math.abs(d) < 1.0e-12) {
				return "0";
			} else if (Double.isInfinite(d)) {
				return (d < 0 ? "-" : "") + "Infinity";
			} else if (Math.abs(d - (int)d) < 1.0e-12) {
				return String.valueOf((int)d);
			} else {
				return String.valueOf(d);
			}
		} else if (o instanceof String) {
			return (String)o;
		} else if (o instanceof Date) {
			return DateUtils.getXMLStringValue((Date)o);
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch [cannot convert to string]");
		}
	}

	public static Date toDate (Object o) {
		if (o instanceof Double) {
			Date d = DateUtils.getDateFromString("1970-01-01");
			d.setTime(d.getTime() + (long)((Double)o).doubleValue() * 86400000l);
			return d;
		} else if (o instanceof String) {
			Date d = DateUtils.getDateFromString((String)o);
			if (d == null) {
				throw new RuntimeException("XPath evaluation: type mismatch [cannot convert to date]");
			} else {
				return d;
			}
		} else if (o instanceof Date) {
			return (Date)o;
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch [cannot convert to date]");
		}
	}

	
}
