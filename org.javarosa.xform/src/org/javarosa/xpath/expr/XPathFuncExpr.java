package org.javarosa.xpath.expr;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xpath.IFunctionHandler;

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
		
		Hashtable funcHandlers = null; //how does this get here?
		
		for (int i = 0; i < args.length; i++) {
			argVals[i] = args[i].eval(model);
		}
		
		if (name.equals("true") && args.length == 0) {
			return Boolean.TRUE;
		} else if (name.equals("false") && args.length == 0) {
			return Boolean.FALSE;
		} else if (name.equals("boolean") && args.length == 1) {
			return toBoolean(argVals[0]);
		} else if (name.equals("number") && args.length == 1) {
			return toNumeric(argVals[0]);
		} else if (name.equals("string") && args.length == 1) {
			return toString(argVals[0]);			
		} else if (name.equals("date") && args.length == 1) { //non-standard
			return toDate(argVals[0]);				
		} else if (name.equals("not") && args.length == 1) {
			return boolNot(argVals[0]);
		} else if (name.equals("boolean-from-string") && args.length == 1) {
			return boolStr(argVals[0]);
		} else if (name.equals("selected") && args.length == 2) { //non-standard
			return multiSelected(argVals[0], argVals[1]);
		} else {
			IFunctionHandler handler = (IFunctionHandler)funcHandlers.get(name);
			if (handler != null) {
				return evalCustomFunction(handler, argVals);
			} else {
				throw new RuntimeException("XPath evaluation: no handler for function [" + name + "]");
			}
		}
	}

	private Object evalCustomFunction (IFunctionHandler handler, Object[] args) {
		Vector prototypes = handler.getPrototypes();
		Enumeration e = prototypes.elements();
		Object[] typedArgs = null;

		while (typedArgs == null && e.hasMoreElements()) {
			typedArgs = matchPrototype(args, (Class[])e.nextElement());
		}

		if (typedArgs != null) {
			return handler.eval(typedArgs);
		} else if (handler.rawArgs()) {
			return handler.eval(args);
		} else {
			throw new RuntimeException("XPath evaluation: type mismatch for function [" + handler.getName() + "]");
		}
	}
	
	Object[] matchPrototype (Object[] args, Class[] prototype) {
		Object[] typed = null;

		if (prototype.length == args.length) {
			typed = new Object[args.length];

			for (int i = 0; i < prototype.length; i++) {
				typed[i] = null;

				//how to handle type conversions of custom types?
				if (prototype[i].isAssignableFrom(args[i].getClass())) {
					typed[i] = args[i];
				} else {
					try {
						if (prototype[i] == Boolean.class) {
							typed[i] = toBoolean(args[i]);
						} else if (prototype[i] == Double.class) {
							typed[i] = toNumeric(args[i]);
						} else if (prototype[i] == String.class) {
							typed[i] = toString(args[i]);
						} else if (prototype[i] == Date.class) {
							typed[i] = toDate(args[i]);
						}
					} catch (RuntimeException re) { /* swallow type mismatch exception */ }
				}

				if (typed[i] == null)
					return null;
			}
		}

		return typed;
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

	public static Boolean boolNot (Object o) {
		boolean b = toBoolean(o).booleanValue();
		return new Boolean(!b);
	}
	
	public static Boolean boolStr (Object o) {
		String s = toString(o);
		if (s.equalsIgnoreCase("true") || s.equals("1"))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}

	//return whether a particular choice of a multi-select is selected
	//arg1: XML-serialized answer to multi-select question (space-delimited choice values)
	//arg2: choice to look for
	public static Boolean multiSelected (Object o1, Object o2) {
		String s1 = (String)o1;
		String s2 = (String)o2;
		
		return new Boolean((" " + s1 + " ").indexOf(" " + s2 + " ") != -1);
	}
}
