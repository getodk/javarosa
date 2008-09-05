package org.javarosa.xpath.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import java.util.Date;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xpath.EvaluationContext;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnsupportedException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathEvalTest extends TestCase {
	public XPathEvalTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public XPathEvalTest(String name) {
		super(name);
	}
	
	public XPathEvalTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		//for (int i = 0; i < parseTestCases.length; i++) {
		//	final String expr = parseTestCases[i][0];
		//	final String expected = parseTestCases[i][1];
			
			aSuite.addTest(new XPathEvalTest("XPath Evaluation Test", new TestMethod() {
				public void run (TestCase tc) {
					((XPathEvalTest)tc).doTests();
				}
			}));
		//}
		
		return aSuite;
	}
		
	private void testEval (String expr, IFormDataModel model, Object expected) {
		//System.out.println("[" + expr + "]");
		
		XPathExpression xpe = null;
		boolean exceptionExpected = (expected instanceof XPathException);
		
		try {
			xpe = XPathParseTool.parseXPath(expr);
		} catch (XPathSyntaxException xpse) { }
		
		if (xpe == null) {
			fail("Null expression or syntax error");
		}
		
		try {
			Object result = xpe.eval(model, new EvaluationContext());
			//System.out.println("out: " + result);
			
			if (exceptionExpected) {
				fail("Expected exception");
			} else if ((result instanceof Double && expected instanceof Double)) {
				if (Math.abs(((Double)result).doubleValue() - ((Double)expected).doubleValue()) > 1.0e-12) {
					fail("Doubles outside of tolerance");
				}
			} else if (!expected.equals(result)) {
				fail("Did not get expected result");
			}
		} catch (XPathException xpex) {
			if (!exceptionExpected) {
				fail("Did not expect exception");
			} else if (xpex.getClass() != expected.getClass()) {
				fail("Did not get expected exception type");
			}
		}
	}

	public void doTests () {
		/* unsupporteds */
		testEval("$var", null, new XPathUnsupportedException());
		testEval("/union | /expr", null, new XPathUnsupportedException());
		testEval("relative/path", null, new XPathUnsupportedException());
		testEval("/descendant::blah", null, new XPathUnsupportedException());
		testEval("/@blah", null, new XPathUnsupportedException());
		testEval("/.", null, new XPathUnsupportedException());
		testEval("/..", null, new XPathUnsupportedException());
		testEval("/text()", null, new XPathUnsupportedException());
		testEval("/*", null, new XPathUnsupportedException());
		testEval("/namespace:*", null, new XPathUnsupportedException());
		testEval("/blah[5]", null, new XPathUnsupportedException());
		testEval("(filter-expr)[5]", null, new XPathUnsupportedException());
		testEval("(filter-expr)/path", null, new XPathUnsupportedException());
		/* numeric literals */
		testEval("5", null, new Double(5.0));
		testEval("555555.555", null, new Double(555555.555));
		testEval(".000555", null, new Double(0.000555));
		testEval("0", null, new Double(0.0));
		testEval("-5", null, new Double(-5.0));
		testEval("-0", null, new Double(-0.0));
		testEval("1230000000000000000000", null, new Double(1.23e21));
		testEval("0.00000000000000000123", null, new Double(1.23e-18));
		/* string literals */
		testEval("''", null, "");
		testEval("'\"'", null, "\"");
		testEval("\"test string\"", null, "test string");
		testEval("'   '", null, "   ");
		/* base type conversion functions */
		testEval("true()", null, Boolean.TRUE);
		testEval("false()", null, Boolean.FALSE);
		testEval("boolean(true())", null, Boolean.TRUE);
		testEval("boolean(false())", null, Boolean.FALSE);
		testEval("boolean(1)", null, Boolean.TRUE);
		testEval("boolean(-1)", null, Boolean.TRUE);
		testEval("boolean(0.0001)", null, Boolean.TRUE);
		testEval("boolean(0)", null, Boolean.FALSE);
		testEval("boolean(-0)", null, Boolean.FALSE);
		testEval("boolean(number('NaN'))", null, Boolean.FALSE);
		testEval("boolean(1 div 0)", null, Boolean.TRUE);
		testEval("boolean(-1 div 0)", null, Boolean.TRUE);
		testEval("boolean('')", null, Boolean.FALSE);
		testEval("boolean('asdf')", null, Boolean.TRUE);
		testEval("boolean('  ')", null, Boolean.TRUE);
		testEval("boolean('false')", null, Boolean.TRUE);
		testEval("boolean(date('2000-01-01'))", null, new XPathTypeMismatchException());
		testEval("number(true())", null, new Double(1.0));
		testEval("number(false())", null, new Double(0.0));
		testEval("number('100')", null, new Double(100.0));
		testEval("number('100.001')", null, new Double(100.001));
		testEval("number('.1001')", null, new Double(0.1001));
		testEval("number('1230000000000000000000')", null, new Double(1.23e21));
		testEval("number('0.00000000000000000123')", null, new Double(1.23e-18));
		testEval("number('0')", null, new Double(0.0));
		testEval("number('-0')", null, new Double(-0.0));
		testEval("number(' -12345.6789  ')", null, new Double(-12345.6789));
		testEval("number('NaN')", null, new Double(Double.NaN));
		testEval("number('not a number')", null, new Double(Double.NaN));
		testEval("number('- 17')", null, new Double(Double.NaN));
		testEval("number('  ')", null, new Double(Double.NaN));
		testEval("number('')", null, new Double(Double.NaN));
		testEval("number('Infinity')", null, new Double(Double.NaN));
		testEval("number('1.1e6')", null, new Double(Double.NaN));
		testEval("number('34.56.7')", null, new Double(Double.NaN));
		testEval("number(10)", null, new Double(10.0));
		testEval("number(0)", null, new Double(0.0));
		testEval("number(-0)", null, new Double(-0.0));
		testEval("number(-123.5)", null, new Double(-123.5));
		testEval("number(number('NaN'))", null, new Double(Double.NaN));
		testEval("number(1 div 0)", null, new Double(Double.POSITIVE_INFINITY));
		testEval("number(-1 div 0)", null, new Double(Double.NEGATIVE_INFINITY));
		testEval("number(date('1970-01-01'))", null, new Double(0.0));
		testEval("number(date('1970-01-02'))", null, new Double(1.0));
		testEval("number(date('1969-12-31'))", null, new Double(-1.0));
		testEval("number(date('2008-09-05'))", null, new Double(14127.0));
		testEval("number(date('1941-12-07'))", null, new Double(-10252.0));
		testEval("string(true())", null, "true");
		testEval("string(false())", null, "false");
		testEval("string(number('NaN'))", null, "NaN");
		testEval("string(1 div 0)", null, "Infinity");
		testEval("string(-1 div 0)", null, "-Infinity");
		testEval("string(0)", null, "0");
		testEval("string(-0)", null, "0");
		testEval("string(123456.0000)", null, "123456");
		testEval("string(-123456)", null, "-123456");
		testEval("string(1)", null, "1");
		testEval("string(-1)", null, "-1");
		testEval("string(.557586)", null, "0.557586");
		//broken: testEval("string(1230000000000000000000)", null, "1230000000000000000000");
		//broken: testEval("string(0.00000000000000000123)", null, "0.00000000000000000123");
		testEval("string('')", null, "");
		testEval("string('  ')", null, "  ");
		testEval("string('a string')", null, "a string");
		testEval("string(date('1989-11-09'))", null, "1989-11-09");
		testEval("date('2000-01-01')", null, DateUtils.getDate(2000, 1, 1));
		testEval("date('1945-04-26')", null, DateUtils.getDate(1945, 4, 26));
		testEval("date('1996-02-29')", null, DateUtils.getDate(1996, 2, 29));
		testEval("date('1983-09-31')", null, new XPathTypeMismatchException());
		testEval("date('not a date')", null, new XPathTypeMismatchException());
		testEval("date(0)", null, DateUtils.getDate(1970, 1, 1));
		testEval("date(6.5)", null, new XPathTypeMismatchException());
		testEval("date(1)", null, DateUtils.getDate(1970, 1, 2));
		testEval("date(-1)", null, DateUtils.getDate(1969, 12, 31));
		testEval("date(14127)", null, DateUtils.getDate(2008, 9, 5));
		testEval("date(-10252)", null, DateUtils.getDate(1941, 12, 7));
		testEval("date(date('1989-11-09'))", null, DateUtils.getDate(1989, 11, 9));
		testEval("date(true())", null, new XPathTypeMismatchException());
		/* other built-in functions */
		testEval("not(true())", null, Boolean.FALSE);
		testEval("not(false())", null, Boolean.TRUE);
		testEval("not('')", null, Boolean.TRUE);
		testEval("boolean-from-string('true')", null, Boolean.TRUE);
		testEval("boolean-from-string('false')", null, Boolean.FALSE);
		testEval("boolean-from-string('whatever')", null, Boolean.FALSE);
		testEval("boolean-from-string('1')", null, Boolean.TRUE);
		testEval("boolean-from-string('0')", null, Boolean.FALSE);
		testEval("boolean-from-string(1)", null, Boolean.TRUE);
		testEval("boolean-from-string(1.0)", null, Boolean.TRUE);
		testEval("boolean-from-string(1.0001)", null, Boolean.FALSE);
		testEval("boolean-from-string(true())", null, Boolean.TRUE);
		testEval("selected('apple baby crimson', 'apple')", null, Boolean.TRUE);
		testEval("selected('apple baby crimson', 'baby')", null, Boolean.TRUE);
		testEval("selected('apple baby crimson', 'crimson')", null, Boolean.TRUE);
		testEval("selected('apple baby crimson', '  baby  ')", null, Boolean.TRUE);
		testEval("selected('apple baby crimson', 'babby')", null, Boolean.FALSE);
		testEval("selected('apple baby crimson', 'bab')", null, Boolean.FALSE);
		testEval("selected('apple', 'apple')", null, Boolean.TRUE);
		testEval("selected('apple', 'ovoid')", null, Boolean.FALSE);
		testEval("selected('', 'apple')", null, Boolean.FALSE);
		/* operators */
		testEval("5.5 + 5.5" , null, new Double(11.0));
		testEval("0 + 0" , null, new Double(0.0));
		testEval("6.1 - 7.8" , null, new Double(-1.7));
		testEval("-3 + 4" , null, new Double(1.0));
		testEval("3 + -4" , null, new Double(-1.0));
		testEval("1 - 2 - 3" , null, new Double(-4.0));
		testEval("1 - (2 - 3)" , null, new Double(2.0));
		testEval("1.1 * -1.1" , null, new Double(-1.21));
		testEval("-10 div -4" , null, new Double(2.5));
		testEval("2 * 3 div 8 * 2" , null, new Double(1.5));
		testEval("3 + 3 * 3" , null, new Double(12.0));
		testEval("1 div 0" , null, new Double(Double.POSITIVE_INFINITY));
		testEval("-1 div 0" , null, new Double(Double.NEGATIVE_INFINITY));
		testEval("0 div 0" , null, new Double(Double.NaN));
		testEval("3.1 mod 3.1" , null, new Double(0.0));
		testEval("5 mod 3.1" , null, new Double(1.9));
		testEval("2 mod 3.1" , null, new Double(2.0));
		testEval("0 mod 3.1" , null, new Double(0.0));
		testEval("5 mod -3" , null, new Double(2.0));
		testEval("-5 mod 3" , null, new Double(-2.0));
		testEval("-5 mod -3" , null, new Double(-2.0));
		testEval("5 mod 0" , null, new Double(Double.NaN));
		testEval("5 * (6 + 7)" , null, new Double(65.0));
		testEval("'123' * '456'" , null, new Double(56088.0));
		testEval("true() + 8" , null, new Double(9.0));
		testEval("5 + 5" , null, new Double(10.0));
		testEval("true() and true()" , null, Boolean.TRUE);
		testEval("true() and false()" , null, Boolean.FALSE);
		testEval("false() and false()" , null, Boolean.FALSE);		
		testEval("true() or true()" , null, Boolean.TRUE);
		testEval("true() or false()" , null, Boolean.TRUE);
		testEval("false() or false()" , null, Boolean.FALSE);
		testEval("true() or true() and false()" , null, Boolean.TRUE);
		testEval("(true() or true()) and false()" , null, Boolean.FALSE);
		testEval("true() or date('')" , null, Boolean.TRUE); //short-circuiting
		testEval("false() and date('')" , null, Boolean.FALSE); //short-circuiting
		testEval("'' or 17" , null, Boolean.TRUE);
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));

		
		testEval("6.1 - 7.8 = -1.7" , null, Boolean.TRUE); //handle floating point rounding
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
//		testEval("5 + 5" , null, new Double(10.0));
		
		
		//testEval("", null, );
		
	}
}


