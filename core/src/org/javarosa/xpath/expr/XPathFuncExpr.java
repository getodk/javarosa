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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.GeoUtils;
import org.javarosa.core.util.MathUtils;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;

/**
 * Representation of an xpath function expression.
 *
 * All of the built-in xpath functions are included here, as well as the xpath type conversion logic
 *
 * Evaluation of functions can delegate out to custom function handlers that must be registered at
 * runtime.
 *
 * @author Drew Roos
 *
 */
public class XPathFuncExpr extends XPathExpression {
	public XPathQName id;			//name of the function
	public XPathExpression[] args;	//argument list

	public XPathFuncExpr () { } //for deserialization

	public XPathFuncExpr (XPathQName id, XPathExpression[] args) {
		this.id = id;
		this.args = args;
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();

		sb.append("{func-expr:");
		sb.append(id.toString());
		sb.append(",{");
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i].toString());
			if (i < args.length - 1)
				sb.append(",");
		}
		sb.append("}}");

		return sb.toString();
	}

	public boolean equals (Object o) {
		if (o instanceof XPathFuncExpr) {
			XPathFuncExpr x = (XPathFuncExpr)o;

			//Shortcuts for very easily comprable values
			//We also only return "True" for methods we expect to return the same thing. This is not good
			//practice in Java, since o.equals(o) will return false. We should evaluate that differently.
			//Dec 8, 2011 - Added "uuid", since we should never assume one uuid equals another
			//May 6, 2013 - Added "random", since two calls asking for a random
			//Jun 4, 2013 - Added "now" and "today", since these could change during the course of a survey
			if(!id.equals(x.id) || args.length != x.args.length ||
				id.toString().equals("uuid") ||
				id.toString().equals("random") ||
				id.toString().equals("once") ||
				id.toString().equals("now") ||
				id.toString().equals("today")) {
				return false;
			}

			return ExtUtil.arrayEquals(args, x.args);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		id = (XPathQName)ExtUtil.read(in, XPathQName.class);
        List<Object> v = (List<Object>)ExtUtil.read(in, new ExtWrapListPoly(), pf);

		args = new XPathExpression[v.size()];
		for (int i = 0; i < args.length; i++)
			args[i] = (XPathExpression)v.get(i);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
      List<XPathExpression> v = Arrays.asList(args);

		ExtUtil.write(out, id);
		ExtUtil.write(out, new ExtWrapListPoly(v));
	}

	/**
	 * Evaluate the function call.
	 *
	 * First check if the function is a member of the built-in function suite. If not, then check
	 * for any custom handlers registered to handler the function. If not, throw and exception.
	 *
	 * Both function name and appropriate arguments are taken into account when finding a suitable
	 * handler. For built-in functions, the number of arguments must match; for custom functions,
	 * the supplied arguments must match one of the function prototypes defined by the handler.
	 *
	 */
	public Object eval (FormInstance model, EvaluationContext evalContext) {
		String name = id.toString();
		Object[] argVals = new Object[args.length];

		HashMap<String, IFunctionHandler> funcHandlers = evalContext.getFunctionHandlers();

		//TODO: Func handlers should be able to declare the desire for short circuiting as well
		if (name.equals("if")) {
			assertArgsCount(name, args, 3);
			return ifThenElse(model, evalContext, args, argVals);
		} else if (name.equals("coalesce")) {
			assertArgsCount(name, args, 2);
			argVals[0] = args[0].eval(model, evalContext);
			if (!isNull(argVals[0])) {
				return argVals[0];
			} else {
				// that was null, so try the other one...
				argVals[1] = args[1].eval(model, evalContext);
				return argVals[1];
			}
		} else if (name.equals("indexed-repeat")) {
			if ((args.length == 3 || args.length == 5 || args.length == 7 || args.length == 9 || args.length == 11)) {
				return indexedRepeat(model, evalContext, args, argVals);
			} else {
				throw new XPathUnhandledException("function \'" + name + "\' requires " +
						  "3, 5, 7, 9 or 11 arguments. Only " + args.length + " provided.");
			}
		}

		for (int i = 0; i < args.length; i++) {
			argVals[i] = args[i].eval(model, evalContext);
		}

		//check built-in functions
		if (name.equals("true")) {
			assertArgsCount(name, args, 0);
			return Boolean.TRUE;
		} else if (name.equals("false")) {
			assertArgsCount(name, args, 0);
			return Boolean.FALSE;
		} else if (name.equals("boolean")) {
			assertArgsCount(name, args, 1);
			return toBoolean(argVals[0]);
		} else if (name.equals("number")) {
			assertArgsCount(name, args, 1);
			return toNumeric(argVals[0]);
		} else if (name.equals("int")) { //non-standard
			assertArgsCount(name, args, 1);
			return toInt(argVals[0]);
		} else if (name.equals("round")) { // non-standard Excel-style round(value,decimal place)
			assertArgsCount(name, args, 2);
			Double aval = toNumeric(argVals[0]);
			Double bval = toInt(argVals[1]);
			return round(aval, bval);
		} else if (name.equals("string")) {
			assertArgsCount(name, args, 1);
			return toString(argVals[0]);
		} else if (name.equals("date")) { //non-standard
			assertArgsCount(name, args, 1);
			return toDate(argVals[0], false);
		} else if (name.equals("date-time")) { //non-standard -- convert double/int/string to Date object
			assertArgsCount(name, args, 1);
			return toDate(argVals[0], true);
		} else if (name.equals("decimal-date-time")) { //non-standard -- convert string/date to decimal days off 1970-01-01T00:00:00.000-000
			assertArgsCount(name, args, 1);
			return toDecimalDateTime(argVals[0], true);
		} else if (name.equals("decimal-time")) { //non-standard -- convert string/date to decimal days off 1970-01-01T00:00:00.000-000
			assertArgsCount(name, args, 1);
			return toDecimalDateTime(argVals[0], false);
		} else if (name.equals("not")) {
			assertArgsCount(name, args, 1);
			return boolNot(argVals[0]);
		} else if (name.equals("boolean-from-string")) {
			assertArgsCount(name, args, 1);
			return boolStr(argVals[0]);
		} else if (name.equals("format-date")) {
			assertArgsCount(name, args, 2);
			return dateStr(argVals[0], argVals[1], false);
		} else if (name.equals("format-date-time")) { // non-standard
			assertArgsCount(name, args, 2);
			return dateStr(argVals[0], argVals[1], true);
		} else if ((name.equals("selected") || name.equals("is-selected"))) { //non-standard
			assertArgsCount(name, args, 2);
			return multiSelected(argVals[0], argVals[1], name);
		} else if (name.equals("count-selected")) { //non-standard
			assertArgsCount(name, args, 1);
			return countSelected(argVals[0]);
		} else if (name.equals("selected-at")) { //non-standard
			assertArgsCount(name, args, 2);
			return selectedAt(argVals[0], argVals[1]);
		} else if (name.equals("position")) {
			//TODO: Technically, only the 0 length argument is valid here.
			if (args.length == 1) {
				XPathNodeset nodes = (XPathNodeset) argVals[0];
				if (nodes.size() == 0) {
					// Added to prevent an exception within ODK Validate.
					// Will likely cause an error downstream when used in an XPath.
					return new Double(1 + TreeReference.INDEX_UNBOUND);
				} else {
					// This is weird -- we are returning the position of the first
					// Nodeset element but there may be a list of elements. Unclear
					// if or how this might manifest into a bug... .
					return position(nodes.getRefAt(0));
				}
			} else if (args.length == 0) {
				if (evalContext.getContextPosition() != -1) {
					return new Double(1 + evalContext.getContextPosition());
				}
				return position(evalContext.getContextRef());
			} else {
				throw new XPathUnhandledException("function \'" + name +
						  "\' requires either exactly one argument or no arguments. Only " + args.length + " provided.");
			}
		} else if (name.equals("count")) {
			assertArgsCount(name, args, 1);
			return count(argVals[0]);
		} else if (name.equals("sum")) {
			assertArgsCount(name, args, 1);
			if (argVals[0] instanceof XPathNodeset) {
				return sum(((XPathNodeset) argVals[0]).toArgList());
			} else {
				throw new XPathTypeMismatchException("not a nodeset");
			}
		} else if (name.equals("max")) {
			if (args.length == 1 && argVals[0] instanceof XPathNodeset) {
				return max(((XPathNodeset) argVals[0]).toArgList());
			} else {
				return max(argVals);
			}
		} else if (name.equals("min")) {
			if (args.length == 1 && argVals[0] instanceof XPathNodeset) {
				return min(((XPathNodeset) argVals[0]).toArgList());
			} else {
				return min(argVals);
			}
		} else if (name.equals("today")) {
			assertArgsCount(name, args, 0);
			return DateUtils.roundDate(new Date());
		} else if (name.equals("now")) {
			assertArgsCount(name, args, 0);
			return new Date();
		} else if (name.equals("concat")) {
			if (args.length == 1 && argVals[0] instanceof XPathNodeset) {
				return join("", ((XPathNodeset) argVals[0]).toArgList());
			} else {
				return join("", argVals);
			}
		} else if (name.equals("join") && args.length >= 1) {
			if (args.length == 2 && argVals[1] instanceof XPathNodeset) {
				return join(argVals[0], ((XPathNodeset) argVals[1]).toArgList());
			} else {
				return join(argVals[0], subsetArgList(argVals, 1));
			}
		} else if (name.equals("substr") && (args.length == 2 || args.length == 3)) {
			return substring(argVals[0], argVals[1], args.length == 3 ? argVals[2] : null);
		} else if (name.equals("string-length")) {
			assertArgsCount(name, args, 1);
			return stringLength(argVals[0]);
		} else if (name.equals("checklist") && args.length >= 2) { //non-standard
			if (args.length == 3 && argVals[2] instanceof XPathNodeset) {
				return checklist(argVals[0], argVals[1], ((XPathNodeset) argVals[2]).toArgList());
			} else {
				return checklist(argVals[0], argVals[1], subsetArgList(argVals, 2));
			}
		} else if (name.equals("weighted-checklist") && args.length >= 2 && args.length % 2 == 0) { //non-standard
			if (args.length == 4 && argVals[2] instanceof XPathNodeset && argVals[3] instanceof XPathNodeset) {
				Object[] factors = ((XPathNodeset) argVals[2]).toArgList();
				Object[] weights = ((XPathNodeset) argVals[3]).toArgList();
				if (factors.length != weights.length) {
					throw new XPathTypeMismatchException("weighted-checklist: nodesets not same length");
				}
				return checklistWeighted(argVals[0], argVals[1], factors, weights);
			} else {
				return checklistWeighted(argVals[0], argVals[1], subsetArgList(argVals, 2, 2), subsetArgList(argVals, 3, 2));
			}
		} else if (name.equals("regex")) { //non-standard
			assertArgsCount(name, args, 2);
			return regex(argVals[0], argVals[1]);
		} else if (name.equals("depend") && args.length >= 1) { //non-standard
			return argVals[0];
		} else if (name.equals("random")) { //non-standard
			assertArgsCount(name, args, 0);
			//calculated expressions may be recomputed w/o warning! use with caution!!
			return new Double(MathUtils.getRand().nextDouble());
		} else if (name.equals("once")) {
			assertArgsCount(name, args, 1);
			XPathPathExpr currentFieldPathExpr = XPathPathExpr.fromRef(evalContext.getContextRef());
			Object currValue = currentFieldPathExpr.eval(model, evalContext).unpack();
			if (currValue == null || toString(currValue).length() == 0) {
				// this is the "once" case
				return argVals[0];
			} else {
				return currValue;
			}
		} else if (name.equals("uuid") && (args.length == 0 || args.length == 1)) { //non-standard
			//calculated expressions may be recomputed w/o warning! use with caution!!
			if (args.length == 0) {
				return PropertyUtils.genUUID();
			}

			int len = toInt(argVals[0]).intValue();
			return PropertyUtils.genGUID(len);
		} else if (name.equals("version")) { //non-standard
			assertArgsCount(name, args, 0);
			return model.formVersion == null ? "" : model.formVersion;
		} else if (name.equals("property")) { // non-standard
			// return a property defined by the property manager.
			// NOTE: Property should be immutable.
			// i.e., does not work with 'start' or 'end' property.
			assertArgsCount(name, args, 1);
			String s = toString(argVals[0]);
			return PropertyManager._().getSingularProperty(s);
		} else if (name.equals("pow") && (args.length == 2)) { //XPath 3.0
			double a = toDouble(argVals[0]).doubleValue();
			double b = toDouble(argVals[1]).doubleValue();
			return Math.pow(a, b);
		} else if (name.equals("enclosed-area") || name.equals("area")) {
			assertArgsCount(name, args, 1);

			Object argVal = argVals[0];
			if (!(argVal instanceof XPathNodeset)) {
				throw new XPathUnhandledException("function \'" + name + "\' requires a field as the parameter.");
			}
			Object[] argList = ((XPathNodeset) argVal).toArgList();
			int repeatSize = argList.length;

			List<GeoUtils.GPSCoordinates> gpsCoordinatesList;

			if (repeatSize == 1) {
				// Try to determine if the argument is of type GeoShapeData
				try {
					GeoShapeData geoShapeData = new GeoShapeData().cast(new UncastData(toString(argList[0])));
					if (geoShapeData.points.size() <= 2) {
						return 0d;
					} else {
						gpsCoordinatesList = new ArrayList<GeoUtils.GPSCoordinates>();
						for (GeoPointData point : geoShapeData.points) {
							gpsCoordinatesList.add(new GeoUtils.GPSCoordinates(point.getPart(0), point.getPart(1)));
						}
					}
				} catch (Exception e) {
					throw new XPathTypeMismatchException("The function \'" + name + "\' received a value that does not represent GPS coordinates: " + argList[0]);
				}
			} else {
				if (repeatSize <= 2) {
					return 0d;
				} else {
					// treat the input as a series of GeoPointData

					gpsCoordinatesList = new ArrayList<GeoUtils.GPSCoordinates>();
					for (Object arg : argList) {
						try {
							GeoPointData geoPointData = new GeoPointData().cast(new UncastData(toString(arg)));
							gpsCoordinatesList.add(new GeoUtils.GPSCoordinates(geoPointData.getPart(0), geoPointData.getPart(1)));
						} catch (Exception e) {
							throw new XPathTypeMismatchException("The function \'" + name + "\' received a value that does not represent GPS coordinates: " + arg);
						}
					}
				}
			}

			return GeoUtils.calculateAreaOfGPSPolygonOnEarthInSquareMeters(gpsCoordinatesList);
		} else {
			//check for custom handler
			IFunctionHandler handler = funcHandlers.get(name);
			if (handler != null) {
				return evalCustomFunction(handler, argVals, evalContext);
			} else {
				throw new XPathUnhandledException("function \'" + name + "\'");
			}
		}
	}

	private static void assertArgsCount(String name, Object[] args, int count) {
		if ( args.length != count ) {
			throw new XPathUnhandledException("function \'" + name + "\' requires " +
					count + " arguments. Only " + args.length + " provided.");
		}
	}

	/**
	 * Given a handler registered to handle the function, try to coerce the function arguments into
	 * one of the prototypes defined by the handler. If no suitable prototype found, throw an eval
	 * exception. Otherwise, evaluate.
	 *
	 * Note that if the handler supports 'raw args', it will receive the full, unaltered argument
	 * list if no prototype matches. (this lets functions support variable-length argument lists)
	 *
	 * @param handler
	 * @param args
	 * @return
	 */
	private static Object evalCustomFunction (IFunctionHandler handler, Object[] args, EvaluationContext ec) {
      List<Class[]> prototypes = handler.getPrototypes();
		Object[] typedArgs = null;

		int i = 0;
		while (typedArgs == null && prototypes.size() > i) {
			typedArgs = matchPrototype(args, prototypes.get(i++));
		}

		if (typedArgs != null) {
			return handler.eval(typedArgs, ec);
		} else if (handler.rawArgs()) {
			return handler.eval(args, ec);  //should we have support for expanding nodesets here?
		} else {
			throw new XPathTypeMismatchException("for function \'" + handler.getName() + "\'");
		}
	}

	/**
	 * Given a prototype defined by the function handler, attempt to coerce the function arguments
	 * to match that prototype (checking # args, type conversion, etc.). If it is coercible, return
	 * the type-converted argument list -- these will be the arguments used to evaluate the function.
	 * If not coercible, return null.
	 *
	 * @param args
	 * @param prototype
	 * @return
	 */
	private static Object[] matchPrototype (Object[] args, Class[] prototype) {
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
							typed[i] = toDate(args[i], false);
						}
					} catch (XPathTypeMismatchException xptme) { /* swallow type mismatch exception */ }
				}

				if (typed[i] == null)
					return null;
			}
		}

		return typed;
	}

	/******** HANDLERS FOR BUILT-IN FUNCTIONS ********
	 *
	 * the functions below are the handlers for the built-in xpath function suite
	 *
	 * if you add a function to the suite, it should adhere to the following pattern:
	 *
	 *   * the function takes in its arguments as objects (DO NOT cast the arguments when calling
	 *     the handler up in eval() (i.e., return stringLength((String)argVals[0])  <--- NO!)
	 *
	 *   * the function converts the generic argument(s) to the desired type using the built-in
	 *     xpath type conversion functions (toBoolean(), toNumeric(), toString(), toDate())
	 *
	 *   * the function MUST return an object of type Boolean, Double, String, or Date; it may
	 *     never return null (instead return the empty string or NaN)
	 *
	 *   * the function may throw exceptions, but should try as hard as possible not to, and if
	 *     it must, strive to make it an XPathException
	 *
	 */

	public static boolean isNull (Object o) {
		if (o == null) {
			return true; //true 'null' values aren't allowed in the xpath engine, but whatever
		}

		o = unpack(o);
		if (o instanceof String && ((String)o).length() == 0) {
			return true;
		} else if (o instanceof Double && ((Double)o).isNaN()) {
			return true;
		} else {
			return false;
		}
	}

	public static Double stringLength (Object o) {
		String s = toString(o);
		if(s == null) {
			return new Double(0.0);
		}
		return new Double(s.length());
	}

	/**
	 * convert a value to a boolean using xpath's type conversion rules
     *
	 * @param o
	 * @return
	 */
	public static Boolean toBoolean (Object o) {
		Boolean val = null;

		o = unpack(o);

		if (o instanceof Boolean) {
			val = (Boolean)o;
		} else if (o instanceof Double) {
			double d = ((Double)o).doubleValue();
			val = new Boolean(Math.abs(d) > 1.0e-12 && !Double.isNaN(d));
		} else if (o instanceof String) {
			String s = (String)o;
			val = new Boolean(s.length() > 0);
		} else if (o instanceof Date) {
			val = Boolean.TRUE;
		} else if (o instanceof IExprDataType) {
			val = ((IExprDataType)o).toBoolean();
		}

		if (val != null) {
			return val;
		} else {
			throw new XPathTypeMismatchException("converting to boolean");
		}
	}

    public static Double toDouble(Object o ){
            if(o instanceof Date) {
                    return DateUtils.fractionalDaysSinceEpoch((Date)o);
            } else {
                    return toNumeric(o);
            }

    }

	/**
	 * convert a value to a number using xpath's type conversion rules (note that xpath itself makes
	 * no distinction between integer and floating point numbers)
	 *
	 * @param o
	 * @return
	 */
	public static Double toNumeric (Object o) {
		Double val = null;

		o = unpack(o);

		if (o instanceof Boolean) {
			val = new Double(((Boolean)o).booleanValue() ? 1 : 0);
		} else if (o instanceof Double) {
			val = (Double)o;
		} else if (o instanceof String) {
			/* annoying, but the xpath spec doesn't recognize scientific notation, or +/-Infinity
			 * when converting a string to a number
			 */

			String s = (String)o;
			double d;
			try {
				s = s.trim();
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					if (c != '-' && c != '.' && (c < '0' || c > '9'))
						throw new NumberFormatException();
				}

				d = Double.parseDouble(s);
				val = new Double(d);
			} catch (NumberFormatException nfe) {
				val = new Double(Double.NaN);
			}
		} else if (o instanceof Date) {
			val = new Double(DateUtils.daysSinceEpoch((Date)o));
		} else if (o instanceof IExprDataType) {
			val = ((IExprDataType)o).toNumeric();
		}

		if (val != null) {
			return val;
		} else {
			throw new XPathTypeMismatchException("converting to numeric");
		}
	}

	/**
	 * convert a number to an integer by truncating the fractional part. if non-numeric, coerce the
	 * value to a number first. note that the resulting return value is still a Double, as required
	 * by the xpath engine
	 *
	 * @param o
	 * @return
	 */
	public static Double toInt (Object o) {
		Double val = toNumeric(o);

		if (val.isInfinite() || val.isNaN()) {
			return val;
		} else if (val.doubleValue() >= Long.MAX_VALUE || val.doubleValue() <= Long.MIN_VALUE) {
			return val;
		} else {
			long l = val.longValue();
			Double dbl = new Double(l);
			if (l == 0 && (val.doubleValue() < 0. || val.equals(new Double(-0.)))) {
				dbl = new Double(-0.);
			}
			return dbl;
		}
	}

	/**
	 * convert a value to a string using xpath's type conversion rules
	 *
	 * @param o
	 * @return
	 */
	public static String toString (Object o) {
		String val = null;

		o = unpack(o);

		if (o instanceof Boolean) {
			val = (((Boolean)o).booleanValue() ? "true" : "false");
		} else if (o instanceof Double) {
			double d = ((Double)o).doubleValue();
			if (Double.isNaN(d)) {
				val = "NaN";
			} else if (Math.abs(d) < 1.0e-12) {
				val = "0";
			} else if (Double.isInfinite(d)) {
				val = (d < 0 ? "-" : "") + "Infinity";
			} else if (Math.abs(d - (int)d) < 1.0e-12) {
				val = String.valueOf((int)d);
			} else {
				val = String.valueOf(d);
			}
		} else if (o instanceof String) {
			val = (String)o;
		} else if (o instanceof Date) {
			val = DateUtils.formatDate((Date)o, DateUtils.FORMAT_ISO8601);
		} else if (o instanceof IExprDataType) {
			val = ((IExprDataType)o).toString();
		}

		if (val != null) {
			return val;
		} else {
			throw new XPathTypeMismatchException("converting to string");
		}
	}

	/**
	 * convert a value to a date. note that xpath has no intrinsic representation of dates, so this
	 * is off-spec. dates convert to strings as 'yyyy-mm-dd', convert to numbers as # of days since
	 * the unix epoch, and convert to booleans always as 'true'
	 *
	 * string and int conversions are reversable, however:
	 *   * cannot convert bool to date
	 *   * empty string and NaN (xpath's 'null values') go unchanged, instead of being converted
	 *     into a date (which would cause an error, since Date has no null value (other than java
	 *     null, which the xpath engine can't handle))
	 *   * note, however, than non-empty strings that aren't valid dates _will_ cause an error
	 *     during conversion
	 *
	 * @param o
	 * @return
	 */
	public static Object toDate (Object o, boolean preserveTime) {
		o = unpack(o);

		if (o instanceof Double) {
			if (preserveTime) {
				Double n = (Double) o;

				if (n.isNaN()) {
					return n;
				}

				if (n.isInfinite() || n.doubleValue() > Integer.MAX_VALUE || n.doubleValue() < Integer.MIN_VALUE) {
					throw new XPathTypeMismatchException("converting out-of-range value to date");
				}

				long timeMillis = (long) (n * DateUtils.DAY_IN_MS);

				Date d = new Date(timeMillis);
				return d;
			} else {
				Double n = toInt(o);

				if (n.isNaN()) {
					return n;
				}

				if (n.isInfinite() || n.doubleValue() > Integer.MAX_VALUE || n.doubleValue() < Integer.MIN_VALUE) {
					throw new XPathTypeMismatchException("converting out-of-range value to date");
				}

				return DateUtils.dateAdd(DateUtils.getDate(1970, 1, 1), n.intValue());
			}
		} else if (o instanceof String) {
			String s = (String)o;

			if (s.length() == 0) {
				return s;
			}

			Date d = DateUtils.parseDateTime(s);
			if (d == null) {
				throw new XPathTypeMismatchException("converting to date");
			} else {
				return d;
			}
		} else if (o instanceof Date) {
			if ( preserveTime ) {
				return (Date) o;
			} else {
				return DateUtils.roundDate((Date)o);
			}
		} else {
			throw new XPathTypeMismatchException("converting to date");
		}
	}

	public static Object toDecimalDateTime (Object o, boolean keepDate) {
		o = unpack(o);

		if (o instanceof Double) {
			Double n = (Double) o;

			if (n.isNaN()) {
				return n;
			}

			if (n.isInfinite() || n.doubleValue() > Integer.MAX_VALUE || n.doubleValue() < Integer.MIN_VALUE) {
				throw new XPathTypeMismatchException("converting out-of-range value to date");
			}

			if ( keepDate ) {
				return n;
			} else {
				return n - Math.floor(n);
			}
		} else if (o instanceof String) {
			String s = (String)o;

			if (s.length() == 0) {
				return s;
			}

			Date d = DateUtils.parseDateTime(s);
			if (d == null) {
				throw new XPathTypeMismatchException("converting to date");
			} else {
				if ( keepDate ) {
					long milli = d.getTime();
					Double v = ((double) milli) / DateUtils.DAY_IN_MS;
					return v;
				} else {
					return DateUtils.decimalTimeOfLocalDay(d);
				}
			}
		} else if (o instanceof Date) {
			Date d = (Date) o;
			if ( keepDate ) {
				long milli = d.getTime();
				Double v = ((double) milli) / DateUtils.DAY_IN_MS;
				return v;
			} else {
				return DateUtils.decimalTimeOfLocalDay(d);
			}
		} else {
			throw new XPathTypeMismatchException("converting to date");
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

	public static String dateStr (Object od, Object of, boolean preserveTime) {
		od = toDate(od, preserveTime);
		if (od instanceof Date) {
			return DateUtils.format((Date)od, toString(of));
		} else {
			return "";
		}
	}

	private Double position(TreeReference refAt) {
		return new Double(1+refAt.getMultLast());
	}

	public static Object ifThenElse (FormInstance model, EvaluationContext ec, XPathExpression[] args, Object[] argVals) {
		argVals[0] = args[0].eval(model, ec);
		boolean b = toBoolean(argVals[0]).booleanValue();
		return (b ? args[1].eval(model, ec) : args[2].eval(model, ec));
	}

 	/**
	 * This provides a method of indexing fields stored in prior repeat groups.
	 *
	 * args[0] = generic XPath expression to index
	 * args[1] = generic XPath expression for group to index
	 * args[2] = index number for group
	 * args[3] = generic XPath expression for add'l group to index (if 5 or 7 parameters passed)
	 * args[4] = index number for group (if 5 or 7 parameters passed)
	 * args[5] = generic XPath expression for add'l group to index (if 7 parameters passed)
	 * args[6] = index number for group (if 7 parameters passed)
	 *
	 * @param model
	 * @param ec
	 * @param args
	 * @param argVals
	 * @return
	 */
	public static Object indexedRepeat (FormInstance model, EvaluationContext ec, XPathExpression[] args, Object[] argVals) throws XPathTypeMismatchException {
		// initialize target and context references
		if (!(args[0] instanceof XPathPathExpr)) {
			throw new XPathTypeMismatchException("indexed-repeat(): first parameter must be XPath field reference");
		}
		XPathPathExpr targetPath=(XPathPathExpr)args[0];
		TreeReference targetRef=targetPath.getReference();
		TreeReference contextRef=targetRef.clone();

		// process passed index(es)
		for (int pathargi=1, idxargi=2 ; idxargi < args.length ; pathargi+=2, idxargi+=2) {
			// confirm that we were passed an XPath
			if (!(args[pathargi] instanceof XPathPathExpr)) {
				throw new XPathTypeMismatchException("indexed-repeat(): parameter " + (pathargi+1) + " must be XPath repeat-group reference");
			}
			// confirm that the passed XPath is a parent of our overall target path
			TreeReference groupRef=((XPathPathExpr)args[pathargi]).getReference();
			if (!groupRef.isParentOf(targetRef,true)) {
				throw new XPathTypeMismatchException("indexed-repeat(): parameter " + (pathargi+1) + " must be a parent of the field in parameter 1");
			}

			// process index (if valid)
			int groupIdx = toInt(args[idxargi].eval(model, ec)).intValue();
			if (groupIdx <= 0) {
				// ignore invalid indexes (primarily happens during validation)
			}
			else {
				// otherwise, add the index to the context reference
				contextRef.setMultiplicity(groupRef.size()-1, groupIdx-1);
			}
		}

		// evaluate and return the XPath expression, in context
		EvaluationContext revisedec = new EvaluationContext(ec, contextRef);
		return (targetPath.eval(model, revisedec));
	}

	/**
	 * return whether a particular choice of a multi-select is selected
	 *
	 * @param o1 XML-serialized answer to multi-select question (i.e, space-delimited choice values)
	 * @param o2 choice to look for
	 * @return
	 */
	public static Boolean multiSelected (Object o1, Object o2, String functionName) {
        Object indexObject = unpack(o2);
        if (!(indexObject instanceof String)) {
            throw new XPathTypeMismatchException("The second parameter to the " + functionName + "() function must be in quotes (like '1').");
        }
        String s1 = (String)unpack(o1);
        String s2 = ((String) indexObject).trim();

		return new Boolean((" " + s1 + " ").indexOf(" " + s2 + " ") != -1);
	}

	/**
	 * return the number of choices in a multi-select answer
	 *
	 * @param o XML-serialized answer to multi-select question (i.e, space-delimited choice values)
	 * @return
	 */
	public static Double countSelected (Object o) {
		String s = (String)unpack(o);

		return new Double(DateUtils.split(s, " ", true).size());
	}

    /**
     * Get the Nth item in a selected list
     *
     * @param o1 XML-serialized answer to multi-select question (i.e, space-delimited choice values)
     * @param o2 the integer index into the list to return
     * @return
     */
    public static String selectedAt (Object o1, Object o2) {
        String selection = (String)unpack(o1);
        int index = toInt(o2).intValue();
       List<String> stringVector = DateUtils.split(selection, " ", true);
        if (stringVector.size() > index && index >= 0) {
            return stringVector.get(index);
        } else {
        	return ""; // empty string if outside of array
        }
    }

	/**
	 * count the number of nodes in a nodeset
	 *
	 * @param o
	 * @return
	 */
	public static Double count (Object o) {
		if (o instanceof XPathNodeset) {
			return new Double(((XPathNodeset)o).size());
		} else {
			throw new XPathTypeMismatchException("not a nodeset");
		}
	}

	/**
	 * sum the values in a nodeset; each element is coerced to a numeric value
	 *
	 * @param model
	 * @param o
	 * @return
	 */
	public static Double sum (Object argVals[]) {
		double sum = 0.0;
		for (int i = 0; i < argVals.length; i++) {
			Double dargVal=toNumeric(argVals[i]);
			if (!dargVal.isNaN()) {
				sum += dargVal.doubleValue();
			}
		}
		return new Double(sum);
	}

	/**
	 * round function like in Excel.
	 *
	 * @param num
	 * @param dblDecim
	 * @return
	 */
	private static Double round(double num, double numDecim) {
		long p=0;

		// rounding doesn't affect special values...
		if ( num == Double.NaN ||
			 num == Double.NEGATIVE_INFINITY ||
			 num == Double.POSITIVE_INFINITY ) {
			return new Double(num);
		}

		// absurdly large rounding requests yield NaN...
		if ( numDecim > 30.0 || numDecim < -30.0 ) {
			return new Double(Double.NaN);
		}

		// ignore fractional numDecim rounding values.
		// any numDecim that is more than 0.5 is considered "1"

		if ( numDecim < 0.0 ) {
			// we want to retain 100's or higher value
			// round( 33.33, 1) = 30.0
			// divide everything by 10's.
			while ( numDecim <= -0.5 ) {
				num /= 10.0;
				++p;
				++numDecim;
			}
		} else {
			// we want to retain a fractional value
			// round( 33.33, -1) = 3.3
			// multiply everything by 10's.
			while ( numDecim >= 0.5 ) {
				num *= 10.0;
				--p;
				--numDecim;
			}
		}
		// truncate with a 0.5 offset to round to the nearest long...
		num = (double)((long) (num+0.5));

		// and now restore the number to the appropriate scale.
		if ( p < 0 ) {
			while ( p < 0 ) {
				num /= 10.0;
				++p;
			}
		} else {
			while ( p > 0 ) {
				num *= 10.0;
				--p;
			}
		}
		return new Double(num);
	}

    /**
     * Identify the largest value from the list of provided values.
     *
     * @param argVals
     * @return
     */
    private static Object max(Object[] argVals) {
        double max = Double.MIN_VALUE;
        boolean returnNaN = true;
        for (int i = 0; i < argVals.length; i++) {
            Double dargVal = toNumeric(argVals[i]);
            if (!dargVal.isNaN()) {
                max = Math.max(max, dargVal.doubleValue());
                returnNaN = false;
            }
        }
        return new Double(returnNaN ? Double.NaN : max);
    }

    private static Object min(Object[] argVals) {
        double min = Double.MAX_VALUE;
        boolean returnNaN = true;
        for (int i = 0; i < argVals.length; i++) {
            Double dargVal = toNumeric(argVals[i]);
            if (!dargVal.isNaN()) {
                min = Math.min(min, dargVal.doubleValue());
                returnNaN = false;
            }
        }
        return new Double(returnNaN ? Double.NaN : min);
    }

	/**
	 * concatenate an abritrary-length argument list of string values together
	 *
	 * @param argVals
	 * @return
	 */
	public static String join (Object oSep, Object[] argVals) {
		String sep = toString(oSep);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < argVals.length; i++) {
			sb.append(toString(argVals[i]));
			if (i < argVals.length - 1)
				sb.append(sep);
		}

		return sb.toString();
	}

	public static String substring (Object o1, Object o2, Object o3) {
		String s = toString(o1);
		int start = toInt(o2).intValue();

		int len = s.length();

		int end = (o3 != null ? toInt(o3).intValue() : len);
		if (start < 0) {
			start = len + start;
		}
		if (end < 0) {
			end = len + end;
		}
		end = Math.min(Math.max(0, end), len);
		start = Math.min(Math.max(0, start), len);

		return (start <= end ? s.substring(start, end) : "");
	}

	/**
	 * perform a 'checklist' computation, enabling expressions like 'if there are at least 3 risk
	 * factors active'
	 *
	 * @param argVals
	 *   the first argument is a numeric value expressing the minimum number of factors required.
	 *     if -1, no minimum is applicable
	 *   the second argument is a numeric value expressing the maximum number of allowed factors.
	 *     if -1, no maximum is applicalbe
	 *   arguments 3 through the end are the individual factors, each coerced to a boolean value
	 * @return true if the count of 'true' factors is between the applicable minimum and maximum,
	 *   inclusive
	 */
	public static Boolean checklist (Object oMin, Object oMax, Object[] factors) {
		int min = toNumeric(oMin).intValue();
		int max = toNumeric(oMax).intValue();

		int count = 0;
		for (int i = 0; i < factors.length; i++) {
			if (toBoolean(factors[i]).booleanValue())
				count++;
		}

		return new Boolean((min < 0 || count >= min) && (max < 0 || count <= max));
	}

	/**
	 * very similar to checklist, only each factor is assigned a real-number 'weight'.
	 *
	 * the first and second args are again the minimum and maximum, but -1 no longer means
	 * 'not applicable'.
	 *
	 * subsequent arguments come in pairs: first the boolean value, then the floating-point
	 * weight for that value
	 *
	 * the weights of all the 'true' factors are summed, and the function returns whether
	 * this sum is between the min and max
	 *
	 * @param argVals
	 * @return
	 */
	public static Boolean checklistWeighted (Object oMin, Object oMax, Object[] flags, Object[] weights) {
		double min = toNumeric(oMin).doubleValue();
		double max = toNumeric(oMax).doubleValue();

		double sum = 0.;
		for (int i = 0; i < flags.length; i++) {
			boolean flag = toBoolean(flags[i]).booleanValue();
			double weight = toNumeric(weights[i]).doubleValue();

			if (flag)
				sum += weight;
		}

		return new Boolean(sum >= min && sum <= max);
	}

	/**
	 * determine if a string matches a regular expression.
	 *
	 * @param o1 string being matched
	 * @param o2 regular expression
	 * @return
	 */
	public static Boolean regex (Object o1, Object o2) {
		String str = toString(o1);
		String re = toString(o2);

		boolean result = Pattern.matches(re, str);
		return new Boolean(result);
	}

	private static Object[] subsetArgList (Object[] args, int start) {
		return subsetArgList(args, start, 1);
	}

	/**
	 * return a subset of an argument list as a new arguments list
	 *
	 * @param args
	 * @param start index to start at
	 * @param skip sub-list will contain every nth argument, where n == skip (default: 1)
	 * @return
	 */
	private static Object[] subsetArgList (Object[] args, int start, int skip) {
		if (start > args.length || skip < 1) {
			throw new RuntimeException("error in subsetting arglist");
		}

		Object[] subargs = new Object[(int)MathUtils.divLongNotSuck(args.length - start - 1, skip) + 1];
		for (int i = start, j = 0; i < args.length; i += skip, j++) {
			subargs[j] = args[i];
		}

		return subargs;
	}

	public static Object unpack (Object o) {
		if (o instanceof XPathNodeset) {
			return ((XPathNodeset)o).unpack();
		} else {
			return o;
		}
	}

	/**
	 *
	 */
	public Object pivot (FormInstance model, EvaluationContext evalContext, List<Object> pivots, Object sentinal) throws UnpivotableExpressionException {
		String name = id.toString();

		//for now we'll assume that all that functions do is return the composition of their components
		Object[] argVals = new Object[args.length];


		//Identify whether this function is an identity: IE: can reflect back the pivot sentinal with no modification
		String[] identities = new String[] {"string-length"};
		boolean id = false;
		for(String identity : identities) {
			if(identity.equals(name)) {
				id = true;
			}
		}

		//get each argument's pivot
		for (int i = 0; i < args.length; i++) {
			argVals[i] = args[i].pivot(model, evalContext, pivots, sentinal);
		}

		boolean pivoted = false;
		//evaluate the pivots
		for(int i = 0 ; i < argVals.length; ++i) {
			if(argVals[i] == null) {
				//one of our arguments contained pivots,
				pivoted = true;
			} else if(sentinal.equals(argVals[i])) {
				//one of our arguments is the sentinal, return the sentinal if possible
				if(id) {
					return sentinal;
				} else {
					//This function modifies the sentinal in a way that makes it impossible to capture
					//the pivot.
					throw new UnpivotableExpressionException();
				}
			}
		}

		if(pivoted) {
			if(id) {
				return null;
			} else {
				//This function modifies the sentinal in a way that makes it impossible to capture
				//the pivot.
				throw new UnpivotableExpressionException();
			}
		}

		//TODO: Inner eval here with eval'd args to improve speed
		return eval(model, evalContext);

	}

}
