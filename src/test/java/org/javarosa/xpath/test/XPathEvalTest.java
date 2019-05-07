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

package org.javarosa.xpath.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.XPathException;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;
import org.javarosa.xpath.XPathUnsupportedException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

public class XPathEvalTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(XPathEvalTest.class);

    public XPathEvalTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite aSuite = new TestSuite();

        aSuite.addTest(new XPathEvalTest("doTests"));
        aSuite.addTest(new XPathEvalTest("doTestsInPolishLocale"));

        return aSuite;
    }

    private void testEval (String expr, Object expected) {
        testEval(expr, null, null, expected);
    }

    private void testEval (String expr, FormInstance model, EvaluationContext ec, Object expected) {
        XPathExpression xpe = null;
        boolean exceptionExpected = (expected instanceof XPathException);
        if (ec == null) {
            ec = new EvaluationContext(model);
        }

        try {
            xpe = XPathParseTool.parseXPath(expr);
        } catch (XPathSyntaxException xpse) {
            logger.info("Parsing {} failed with {}", expr, xpse.toString());
        }

        if (xpe == null) {
            fail("Null expression or syntax error");
        }

        try {
            Object result = xpe.eval(model, ec);

            if (exceptionExpected) {
                fail("Expected exception, expression : " + expr);
            } else if ((result instanceof Double && expected instanceof Double)) {
                if (! expected.equals(result)) {
                    logger.info(String.format("%s result %s differed from expected %s",
                            expr, result, expected));
                }
                assertEquals((Double) expected, (Double) result, 1e-12);
            } else if (result instanceof XPathNodeset && expected instanceof XPathNodeset) {
                XPathNodeset expectedAsXPathNodeset = (XPathNodeset) expected;
                XPathNodeset resultAsXPathNodeset = (XPathNodeset) result;
                assertEquals(expectedAsXPathNodeset.size(), resultAsXPathNodeset.size());
                assertEquals(expectedAsXPathNodeset.getRefAt(0), resultAsXPathNodeset.getRefAt(0));
                assertEquals(expectedAsXPathNodeset.unpack(), resultAsXPathNodeset.unpack());
                assertEquals(expectedAsXPathNodeset.toArgList().length, resultAsXPathNodeset.toArgList().length);
                for (int i = 0; i < expectedAsXPathNodeset.toArgList().length; i++) {
                    assertEquals(expectedAsXPathNodeset.toArgList()[i], resultAsXPathNodeset.toArgList()[i]);
                }
            } else {
                assertEquals(expected, result);
            }
        } catch (XPathException xpex) {
            if (!exceptionExpected) {
                fail("Unexpected exception: " + xpex);
            } else if (xpex.getClass() != expected.getClass()) {
                fail("Did not get expected exception type");
            }
        }
    }

    private void doTestsInSpecificLocale(Locale targetLocale) {
        Locale previous = Locale.getDefault();
        Locale.setDefault(targetLocale);
        doTests();
        Locale.setDefault(previous);
    }

    public void doTestsInPolishLocale() {
        doTestsInSpecificLocale(Locale.forLanguageTag("pl"));
    }

    public void doTests () {
        EvaluationContext ec = getFunctionHandlers();

        FormInstance instance = createTestInstance();

        FormInstance countNonEmptyInstance = createCountNonEmptyTestInstance();

        logTestCategory("counting");
        testEval("count(/data/path)", countNonEmptyInstance, null, 5.0);
        testEval("count-non-empty(/data/path)", countNonEmptyInstance, null, 3);

        logTestCategory("unsupporteds");
        testEval("/union | /expr", new XPathUnsupportedException());
        testEval("/descendant::blah", new XPathUnsupportedException());
        testEval("/cant//support", new XPathUnsupportedException());
        testEval("/text()", new XPathUnsupportedException());
        testEval("/namespace:*", new XPathUnsupportedException());
        testEval("(filter-expr)[5]", instance, null, new XPathUnsupportedException());
        testEval("(filter-expr)/data", instance, null, new XPathUnsupportedException());

        logTestCategory("numeric literals");
        testEval("5", 5.0);
        testEval("555555.555", 555555.555);
        testEval(".000555", 0.000555);
        testEval("0", 0.0);
        testEval("-5", -5.0);
        testEval("-0", -0.0);
        testEval("1230000000000000000000", 1.23e21);
        testEval("0.00000000000000000123", 1.23e-18);

        logTestCategory("string literals");
        testEval("''", "");
        testEval("'\"'", "\"");
        testEval("\"test string\"", "test string");
        testEval("'   '", "   ");

        logTestCategory("type conversions");
        testEval("true()", TRUE);
        testEval("false()", FALSE);
        testEval("boolean(true())", TRUE);
        testEval("boolean(false())", FALSE);
        testEval("boolean(1)", TRUE);
        testEval("boolean(-1)", TRUE);
        testEval("boolean(0.0001)", TRUE);
        testEval("boolean(0)", FALSE);
        testEval("boolean(-0)", FALSE);
        testEval("boolean(number('NaN'))", FALSE);
        testEval("boolean(1 div 0)", TRUE);
        testEval("boolean(-1 div 0)", TRUE);
        testEval("boolean('')", FALSE);
        testEval("boolean('asdf')", TRUE);
        testEval("boolean('  ')", TRUE);
        testEval("boolean('false')", TRUE);
        testEval("boolean(date('2000-01-01'))", TRUE);
        testEval("boolean(convertible())", null, ec, TRUE);
        testEval("boolean(inconvertible())", null, ec, new XPathTypeMismatchException());
        testEval("number(true())", 1.0);
        testEval("number(false())", 0.0);
        testEval("number('100')", 100.0);
        testEval("number('100.001')", 100.001);
        testEval("number('.1001')", 0.1001);
        testEval("number('1230000000000000000000')", 1.23e21);
        testEval("number('0.00000000000000000123')", 1.23e-18);
        testEval("number('0')", 0.0);
        testEval("number('-0')", -0.0);
        testEval("number(' -12345.6789  ')", -12345.6789);
        testEval("number('NaN')", NaN);
        testEval("number('not a number')", NaN);
        testEval("number('- 17')", NaN);
        testEval("number('  ')", NaN);
        testEval("number('')", NaN);
        testEval("number('Infinity')", NaN);
        testEval("number('1.1e6')", NaN);
        testEval("number('34.56.7')", NaN);
        testEval("number(10)", 10.0);
        testEval("number(0)", 0.0);
        testEval("number(-0)", -0.0);
        testEval("number(-123.5)", -123.5);
        testEval("number(number('NaN'))", NaN);
        testEval("number(1 div 0)", POSITIVE_INFINITY);
        testEval("number(-1 div 0)", NEGATIVE_INFINITY);
        testEval("number(date('1970-01-01'))", 0.0);
        testEval("number(date('1970-01-02'))", 1.0);
        testEval("number(date('1969-12-31'))", -1.0);
        testEval("number(date('2008-09-05'))", 14127.0);
        testEval("number(date('1941-12-07'))", -10252.0);
        testEval("number(convertible())", null, ec, 5.0);
        testEval("number(inconvertible())", null, ec, new XPathTypeMismatchException());
        testEval("string(true())", "true");
        testEval("string(false())", "false");
        testEval("string(number('NaN'))", "NaN");
        testEval("string(1 div 0)", "Infinity");
        testEval("string(-1 div 0)", "-Infinity");
        testEval("string(0)", "0");
        testEval("string(-0)", "0");
        testEval("string(123456.0000)", "123456");
        testEval("string(-123456)", "-123456");
        testEval("string(1)", "1");
        testEval("string(-1)", "-1");
        testEval("string(.557586)", "0.557586");
        //broken: testEval("string(1230000000000000000000)", "1230000000000000000000");
        //broken: testEval("string(0.00000000000000000123)", "0.00000000000000000123");
        testEval("string('')", "");
        testEval("string('  ')", "  ");
        testEval("string('a string')", "a string");
        testEval("string(date('1989-11-09'))", "1989-11-09");
        testEval("string(convertible())", null, ec, "hi");
        testEval("string(inconvertible())", null, ec, new XPathTypeMismatchException());

        logTestCategory("substring functions");
        testEval("substr('hello',0)", "hello");
        testEval("substr('hello',0,5)", "hello");
        testEval("substr('hello',1)", "ello");
        testEval("substr('hello',1,5)", "ello");
        testEval("substr('hello',1,4)", "ell");
        testEval("substr('hello',-2)", "lo");
        testEval("substr('hello',0,-1)", "hell");
        testEval("substring-before('hello','l')", "he");
        testEval("substring-before('hello','q')", "");
        testEval("substring-before('hello','')", "");
        testEval("substring-before('','')", "");
        testEval("substring-before('','q')", "");
        testEval("substring-after('hello','l')", "lo");
        testEval("substring-after('hello','q')", "");
        testEval("substring-after('hello','')", "hello");
        testEval("substring-after('','')", "");
        testEval("substring-after('','q')", "");
        testEval("translate('hello','l','L')", "heLLo");
        testEval("translate('hello','q','Q')", "hello");
        testEval("translate('hello','','L')", "hello");
        testEval("translate('hello','l','')", "heo");
        testEval("translate('','l','L')", "");
        testEval("translate('hello','lo','LO')", "heLLO");
        testEval("translate('hello world','hello','')", " wrd");
        testEval("translate('hello wor,ld',' ,',', ')", "hello,wor ld");
        testEval("translate('2019/01/02','/','-')", "2019-01-02");
        testEval("contains('a', 'a')",      true);
        testEval("contains('a', 'b')",      false);
        testEval("contains('abc', 'b')",    true);
        testEval("contains('abc', 'bcd')",  false);
        testEval("not(contains('a', 'b'))", true);
        testEval("starts-with('abc', 'a')", true);
        testEval("starts-with('', 'a')",    false);
        testEval("starts-with('', '')",     true);
        testEval("ends-with('abc', 'a')",   false);
        testEval("ends-with('abc', 'c')",   true);
        testEval("ends-with('', '')",       true);

        logTestCategory("date functions");
        testEval("date('2000-01-01')", DateUtils.getDate(2000, 1, 1));
        testEval("date('1945-04-26')", DateUtils.getDate(1945, 4, 26));
        testEval("date('1996-02-29')", DateUtils.getDate(1996, 2, 29));
        testEval("date('1983-09-31')", new XPathTypeMismatchException());
        testEval("date('not a date')", new XPathTypeMismatchException());
        testEval("date(0)", DateUtils.getDate(1970, 1, 1));
        testEval("date(6.5)", DateUtils.getDate(1970, 1, 7));
        testEval("date(1)", DateUtils.getDate(1970, 1, 2));
        testEval("date(-1)", DateUtils.getDate(1969, 12, 31));
        testEval("date(14127)", DateUtils.getDate(2008, 9, 5));
        testEval("date(-10252)", DateUtils.getDate(1941, 12, 7));
        testEval("date(date('1989-11-09'))", DateUtils.getDate(1989, 11, 9));
        testEval("date(true())", new XPathTypeMismatchException());
        testEval("date(convertible())", null, ec, new XPathTypeMismatchException());
        //note: there are lots of time and timezone-like issues with dates that should be tested (particularly DST changes),
        //    but it's just too hard and client-dependent, so not doing it now
        //  basically:
        //        dates cannot reliably be compared/used across time zones (an issue with the code)
        //        any time-of-day or DST should be ignored when comparing/using a date (an issue with testing)
        /* other built-in functions */
        testEval("format-date('2018-01-02T10:20:30.123', \"%Y-%m-%e %H:%M:%S\")", "2018-01-2 10:20:30");

        logTestCategory("boolean functions");
        testEval("not(true())", FALSE);
        testEval("not(false())", TRUE);
        testEval("not('')", TRUE);
        testEval("boolean-from-string('true')", TRUE);
        testEval("boolean-from-string('false')", FALSE);
        testEval("boolean-from-string('whatever')", FALSE);
        testEval("boolean-from-string('1')", TRUE);
        testEval("boolean-from-string('0')", FALSE);
        testEval("boolean-from-string(1)", TRUE);
        testEval("boolean-from-string(1.0)", TRUE);
        testEval("boolean-from-string(1.0001)", FALSE);
        testEval("boolean-from-string(true())", TRUE);
        testEval("if(true(), 5, 'abc')", 5.0);
        testEval("if(false(), 5, 'abc')", "abc");
        testEval("if(6 > 7, 5, 'abc')", "abc");
        testEval("if('', 5, 'abc')", "abc");
        testEval("selected('apple baby crimson', 'apple')", TRUE);
        testEval("selected('apple baby crimson', 'baby')", TRUE);
        testEval("selected('apple baby crimson', 'crimson')", TRUE);
        testEval("selected('apple baby crimson', '  baby  ')", TRUE);
        testEval("selected('apple baby crimson', 'babby')", FALSE);
        testEval("selected('apple baby crimson', 'bab')", FALSE);
        testEval("selected('apple', 'apple')", TRUE);
        testEval("selected('apple', 'ovoid')", FALSE);
        testEval("selected('', 'apple')", FALSE);

        logTestCategory("math operators");
        testEval("5.5 + 5.5" , 11.0);
        testEval("0 + 0" , 0.0);
        testEval("6.1 - 7.8" , -1.7);
        testEval("-3 + 4" , 1.0);
        testEval("3 + -4" , -1.0);
        testEval("1 - 2 - 3" , -4.0);
        testEval("1 - (2 - 3)" , 2.0);
        testEval("-(8*5)" , -40.0);
        testEval("-'19'" , -19.0);
        testEval("1.1 * -1.1" , -1.21);
        testEval("-10 div -4" , 2.5);
        testEval("2 * 3 div 8 * 2" , 1.5);
        testEval("3 + 3 * 3" , 12.0);
        testEval("1 div 0" , POSITIVE_INFINITY);
        testEval("-1 div 0" , NEGATIVE_INFINITY);
        testEval("0 div 0" , NaN);
        testEval("3.1 mod 3.1" , 0.0);
        testEval("5 mod 3.1" , 1.9);
        testEval("2 mod 3.1" , 2.0);
        testEval("0 mod 3.1" , 0.0);
        testEval("5 mod -3" , 2.0);
        testEval("-5 mod 3" , -2.0);
        testEval("-5 mod -3" , -2.0);
        testEval("5 mod 0" , NaN);
        testEval("5 * (6 + 7)" , 65.0);
        testEval("'123' * '456'" , 56088.0);

        logTestCategory("math functions");
        testEval("abs(-3.5)", 3.5);
        // round, with a single argument
        testEval("round('14.29123456789')", 14.0);
        testEval("round('14.6')",           15.0);
        // round, with two arguments
        testEval("round('14.29123456789', 0)", 14.0);
        testEval("round('14.29123456789', 1)", 14.3);
        testEval("round('14.29123456789', 1.5)", 14.3);
        testEval("round('14.29123456789', 2)", 14.29);
        testEval("round('14.29123456789', 3)", 14.291);
        testEval("round('14.29123456789', 4)", 14.2912);
        testEval("round('12345.14',     1)", 12345.1);
        testEval("round('-12345.14',    1)", -12345.1);
        testEval("round('12345.12345',  0)", 12345.0);
        testEval("round('12345.12345', -1)", 12350.0);
        testEval("round('12345.12345', -2)", 12300.0);
        testEval("round('12350.12345', -2)", 12400.0);
        testEval("round('12345.12345', -3)", 12000.0);
        // round, with a comma instead of a decimal point
        testEval("round('4,6'            )", 5.0);
        // XPath specification tests
        testEval("round('1 div 0', 0)", NaN);
        testEval("round('14.5')", 15.0);
        testEval("round('NaN')", NaN);
        testEval("round('-NaN')", -NaN);
        testEval("round('0')", 0.0);
        testEval("round('-0')", -0.0);
        testEval("round('-0.5')", -0.0);
        // non US format
        testEval("round('14,6')", 15.0);
        // Java 8 tests deprecated by XPath 3.0 specification
        // See discussion at https://github.com/opendatakit/javarosa/pull/42#issuecomment-299527754
        testEval("round('12345.15',     1)", 12345.2);
        testEval("round('-12345.15',    1)", -12345.1);

        logTestCategory("strange operators");
        testEval("true() + 8" , 9.0);
        testEval("date('2008-09-08') - date('1983-10-06')" , 9104.0);
        testEval("true() and true()" , TRUE);
        testEval("true() and false()" , FALSE);
        testEval("false() and false()" , FALSE);
        testEval("true() or true()" , TRUE);
        testEval("true() or false()" , TRUE);
        testEval("false() or false()" , FALSE);
        testEval("true() or true() and false()" , TRUE);
        testEval("(true() or true()) and false()" , FALSE);
        testEval("true() or date('')" , TRUE); //short-circuiting
        testEval("false() and date('')" , FALSE); //short-circuiting
        testEval("'' or 17" , TRUE);
        testEval("false() or 0 + 2" , TRUE);
        testEval("(false() or 0) + 2" , 2.0);
        testEval("4 < 5" , TRUE);
        testEval("5 < 5" , FALSE);
        testEval("6 < 5" , FALSE);
        testEval("4 <= 5" , TRUE);
        testEval("5 <= 5" , TRUE);
        testEval("6 <= 5" , FALSE);
        testEval("4 > 5" , FALSE);
        testEval("5 > 5" , FALSE);
        testEval("6 > 5" , TRUE);
        testEval("4 >= 5" , FALSE);
        testEval("5 >= 5" , TRUE);
        testEval("6 >= 5" , TRUE);
        testEval("-3 > -6" , TRUE);

        logTestCategory("odd comparisons");
        testEval("true() > 0.9999" , TRUE);
        testEval("'-17' > '-172'" , TRUE); //no string comparison: converted to number
        testEval("'abc' < 'abcd'" , FALSE); //no string comparison: converted to NaN
        testEval("date('2001-12-26') > date('2001-12-25')" , TRUE);
        testEval("date('1969-07-20') < date('1969-07-21')" , TRUE);
        testEval("false() and false() < true()" , FALSE);
        testEval("(false() and false()) < true()" , TRUE);
        testEval("6 < 7 - 4" , FALSE);
        testEval("(6 < 7) - 4" , -3.0);
        testEval("3 < 4 < 5" , TRUE);
        testEval("3 < (4 < 5)" , FALSE);
        testEval("true() = true()" , TRUE);
        testEval("true() = false()" , FALSE);
        testEval("true() != true()" , FALSE);
        testEval("true() != false()" , TRUE);
        testEval("3 = 3" , TRUE);
        testEval("3 = 4" , FALSE);
        testEval("3 != 3" , FALSE);
        testEval("3 != 4" , TRUE);
        testEval("6.1 - 7.8 = -1.7" , TRUE); //handle floating point rounding
        testEval("'abc' = 'abc'" , TRUE);
        testEval("'abc' = 'def'" , FALSE);
        testEval("'abc' != 'abc'" , FALSE);
        testEval("'abc' != 'def'" , TRUE);
        testEval("'' = ''" , TRUE);
        testEval("true() = 17" , TRUE);
        testEval("0 = false()" , TRUE);
        testEval("true() = 'true'" , TRUE);
        testEval("17 = '17.0000000'" , TRUE);
        testEval("'0017.' = 17" , TRUE);
        testEval("'017.' = '17.000'", FALSE);
        testEval("date('2004-05-01') = date('2004-05-01')" , TRUE);
        testEval("true() != date('1999-09-09')" , FALSE);
        testEval("false() and true() != true()" , FALSE);
        testEval("(false() and true()) != true()" , TRUE);
        testEval("-3 < 3 = 6 >= 6" , TRUE);

        logTestCategory("functions, including custom function handlers");
        testEval("true(5)", new XPathUnhandledException());
        testEval("number()", new XPathUnhandledException());
        testEval("string('too', 'many', 'args')", new XPathUnhandledException());
        testEval("not-a-function()", new XPathUnhandledException());
        testEval("testfunc()", null, ec, TRUE);
        testEval("add(3, 5)", null, ec, 8.0);
        testEval("add('17', '-14')", null, ec, 3.0);

        logTestCategory("proto");
        testEval("proto()", null, ec, new XPathTypeMismatchException());
        testEval("proto(5, 5)", null, ec, "[Double:5.0,Double:5.0]");
        testEval("proto(6)", null, ec, "[Double:6.0]");
        testEval("proto('asdf')", null, ec, "[Double:NaN]");
        testEval("proto('7', '7')", null, ec, "[Double:7.0,Double:7.0]"); //note: args treated as doubles because
            //(double, double) prototype takes precedence and strings are convertible to doubles
        testEval("proto(1.1, 'asdf', true())", null, ec, "[Double:1.1,String:asdf,Boolean:true]");
        testEval("proto(false(), false(), false())", null, ec, "[Double:0.0,String:false,Boolean:false]");
        testEval("proto(1.1, 'asdf', inconvertible())", null, ec, new XPathTypeMismatchException());
        testEval("proto(1.1, 'asdf', true(), 16)", null, ec, new XPathTypeMismatchException());

        logTestCategory("raw");
        testEval("raw()", null, ec, "[]");
        testEval("raw(5, 5)", null, ec, "[Double:5.0,Double:5.0]");
        testEval("raw('7', '7')", null, ec, "[String:7,String:7]");
        testEval("raw('1.1', 'asdf', 17)", null, ec, "[Double:1.1,String:asdf,Boolean:true]"); //convertible to prototype
        testEval("raw(get-custom(false()), get-custom(true()))", null, ec, "[CustomType:,CustomSubType:]");

        logTestCategory("concat");
        testEval("concat()", null, ec, "");
        testEval("concat('a')", null, ec, "a");
        testEval("concat('a','b','')", null, ec, "ab");
        testEval("concat('ab','cde','','fgh',1,false(),'ijklmnop')", null, ec, "abcdefgh1falseijklmnop");
        testEval("check-types(55, '55', false(), '1999-09-09', get-custom(false()))", null, ec, TRUE);
        testEval("check-types(55, '55', false(), '1999-09-09', get-custom(true()))", null, ec, TRUE);

        logTestCategory("regex");
        testEval("regex('12345','[0-9]+')", null, ec, TRUE);
        testEval("pow(2, 2)", 4.0);
        testEval("pow(2, 0)", 1.0);
        testEval("pow(0, 4)", 0.0);
        testEval("pow(2.5, 2)", 6.25);
        testEval("pow(0.5, 2)", .25);
        testEval("pow(-1, 2)", 1.0);
        testEval("pow(-1, 3)", -1.0);
        //So raising things to decimal powers is.... very hard
        //to evaluated exactly due to double floating point
        //precision. We'll try for things with clean answers
        testEval("pow(4, 0.5)", 2.0);
        testEval("pow(16, 0.25)", 2.0);

        logTestCategory("variable refs");
        EvaluationContext varContext = getVariableContext();
        testEval("$var_float_five", null, varContext, 5.0);
        testEval("$var_string_five", null, varContext, "five");
        testEval("$var_int_five", null, varContext, 5.0);
        testEval("$var_double_five", null, varContext, 5.0);

        logTestCategory("node referencing");
        // happy flow scenario where the index node is not blank
        FormInstance testInstance = createTestDataForIndexedRepeatFunction(1);
        XPathNodeset expected = createExpectedNodesetFromIndexedRepeatFunction(testInstance, 1, "name");
        testEval("indexed-repeat( /data/repeat/name , /data/repeat , /data/index1 )", testInstance, null, expected);

        // situation where the referenced index node is blank and the default value (0 which means the first repeat group) is used
        testInstance = createTestDataForIndexedRepeatFunction(null);
        expected = createExpectedNodesetFromIndexedRepeatFunction(testInstance, 0, "name");
        testEval("indexed-repeat( /data/repeat/name , /data/repeat , /data/index1 )", testInstance, null, expected);

        logTestCategory("crypto functions");
        // Support for all 5 supported digest algorithms (required and optional) and default base64 encoding
        testEval("digest('some text', 'MD5', 'base64')", "VS4hzUzZkYZ448Gg30kbww==");
        testEval("digest('some text', 'SHA-1', 'base64')", "N6pjx3OY2VRHMmLhoAV8HmMu2nc=");
        testEval("digest('some text', 'SHA-256', 'base64')", "uU9vElx546X/qoJvWEwQ1SraZp5nYgUbgmtVd20FrtI=");
        testEval("digest('some text', 'SHA-384', 'base64')", "zJTsPphzwLmnJIZEKVj2cQZ833e5QnQW0DFEDMYgQeLuE0RJhEfsDO2fcENGG9Hz");
        testEval("digest('some text', 'SHA-512', 'base64')", "4nMrrtyj6sFAeChjfeHbynAsP8ns4Wz1Nt241hOc2F3+dGS4I1spgm9gjM9KxkPimxnGN4WKPYcQpZER30LdtQ==");
        // Support for hexadecimal encoding
        testEval("digest('some text', 'MD5', 'hex')", "552e21cd4cd9918678e3c1a0df491bc3");
        // Support for optional third argument (defaults to 'base64')
        testEval("digest('some text', 'MD5')", "VS4hzUzZkYZ448Gg30kbww==");

        //Attribute XPath References
        //testEval("/@blah", new XPathUnsupportedException());
        //TODO: Need to test with model, probably in a different file


        try {
            testEval("null-proto()", null, ec, new XPathUnhandledException());
            fail("Did not get expected null pointer");
        } catch (NullPointerException npe) {
            //expected
        }

        ec.addFunctionHandler(read);
        ec.addFunctionHandler(write);

        read.val = "testing-read";
        testEval("read()", null, ec, "testing-read");

        testEval("write('testing-write')", null, ec, TRUE);
        if (!"testing-write".equals(write.val))
            fail("Custom function handler did not successfully send data to external source");
    }

    private XPathNodeset createExpectedNodesetFromIndexedRepeatFunction(FormInstance testInstance, int repeatIndex, String nodeName) {
        TreeReference referencedNode =
                testInstance.getRoot().getChildAt(repeatIndex).getChildrenWithName(nodeName).get(0).getRef();
        return new XPathNodeset(
                Collections.singletonList(referencedNode),
                testInstance,
                new EvaluationContext(new EvaluationContext(testInstance), referencedNode)
        );
    }

    private FormInstance createTestDataForIndexedRepeatFunction(Integer indexNodeValue) {
        TreeElement root = new TreeElement("data");
        TreeElement repeat = new TreeElement("repeat");
        TreeElement repeatChild = new TreeElement("name");
        repeatChild.setAnswer(new StringData("A"));
        repeat.addChild(repeatChild);
        root.addChild(repeat);

        repeat = new TreeElement("repeat");
        repeatChild = new TreeElement("name");
        repeatChild.setAnswer(new StringData("B"));
        repeat.addChild(repeatChild);
        root.addChild(repeat);

        repeat = new TreeElement("repeat");
        repeatChild = new TreeElement("name");
        repeatChild.setAnswer(new StringData("C"));
        repeat.addChild(repeatChild);
        root.addChild(repeat);

        TreeElement index = new TreeElement("index1");
        if (indexNodeValue != null) {
            index.setValue(new IntegerData(1));
        }
        return new FormInstance(root);
    }

    public FormInstance createTestInstance() {
        TreeElement data = new TreeElement("data");
        data.addChild(new TreeElement("path"));
        return new FormInstance(data);
    }

    public FormInstance createCountNonEmptyTestInstance() {
        TreeElement data = new TreeElement("data");

        TreeElement path = new TreeElement("path", 0);
        path.addChild(new TreeElement("child", 0));
        path.addChild(new TreeElement("child", 1));
        data.addChild(path);

        data.addChild(new TreeElement("path", 1));

        path = new TreeElement("path", 2);
        path.setValue(new StringData("some value"));
        data.addChild(path);

        path = new TreeElement("path", 3);
        path.addChild(new TreeElement("child", 0));
        data.addChild(path);

        data.addChild(new TreeElement("path", 4));

        return new FormInstance(data);
    }
    
    private void logTestCategory(String message) {
        logger.info("Running {} test: {}", this.getClass().getName(), message);
    } 

    private EvaluationContext getFunctionHandlers () {
        EvaluationContext ec = new EvaluationContext(null);
        final Class[][] allPrototypes = {
                {Double.class, Double.class},
                {Double.class},
                {String.class, String.class},
                {Double.class, String.class, Boolean.class},
                {Boolean.class},
                {Boolean.class, Double.class, String.class, Date.class, CustomType.class}
        };

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "testfunc"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(new Class[0]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return TRUE; }
        });

        ec.addFunctionHandler(new IFunctionHandler(){

            @Override
            public String getName() { return "regex";    }
            @Override
            public Object eval(Object[] args, EvaluationContext ec) {
                logger.info("EVAL REGEX TESTS:");
                for (Object arg : args) {
                    logger.info("REGEX ARGS: {}", arg.toString());
                }


                return TRUE; // String.re  args[0].

            }
            @Override
            public List<Class[]> getPrototypes () {
                List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[2]);
                return p;
            }
            @Override
            public boolean rawArgs() { return false; }
            @Override
            public boolean realTime() {    return false; }

        });


        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "add"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[0]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return (Double) args[0] + (Double) args[1]; }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "proto"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[0]);
                p.add(allPrototypes[1]);
                p.add(allPrototypes[2]);
                p.add(allPrototypes[3]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return printArgs(args); }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "raw"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[3]);
                return p;
            }
            @Override
            public boolean rawArgs () { return true; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return printArgs(args); }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "null-proto"; }
            @Override
            public List<Class[]> getPrototypes () { return null; }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return FALSE; }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "concat"; }
            @Override
            public List<Class[]> getPrototypes () { return new ArrayList<Class[]>(); }
            @Override
            public boolean rawArgs () { return true; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++)
                    sb.append(XPathFuncExpr.toString(args[i]));
                return sb.toString();
            }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "convertible"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(new Class[0]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return new IExprDataType() {
                    @Override
                    public Boolean toBoolean () { return TRUE; }
                    @Override
                    public Double toNumeric () { return 5.0; }
                    public String toString () { return "hi"; }
                };
            }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "inconvertible"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(new Class[0]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return new Object(); }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "get-custom"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[4]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) { return (Boolean) args[0] ? new CustomSubType() : new CustomType(); }
        });

        ec.addFunctionHandler(new IFunctionHandler () {
            @Override
            public String getName () { return "check-types"; }
            @Override
            public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
                p.add(allPrototypes[5]);
                return p;
            }
            @Override
            public boolean rawArgs () { return false; }
            @Override
            public boolean realTime () { return false; }
            @Override
            public Object eval (Object[] args, EvaluationContext ec) {
                if (args.length != 5 || !(args[0] instanceof Boolean) || !(args[1] instanceof Double) ||
                        !(args[2] instanceof String) || !(args[3] instanceof Date) || !(args[4] instanceof CustomType))
                    fail("Types in custom function handler not converted properly/prototype not matched properly");

                return TRUE;
            }
        });
        return ec;
    }

    private EvaluationContext getVariableContext() {
        EvaluationContext ec = new EvaluationContext(null);

        ec.setVariable("var_float_five", 5.0f);
        ec.setVariable("var_string_five", "five");
        ec.setVariable("var_int_five", 5);
        ec.setVariable("var_double_five", 5.0);

        return ec;
    }

    private String printArgs (Object[] oa) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < oa.length; i++) {
            String fullName = oa[i].getClass().getName();
            int lastIndex = Math.max(fullName.lastIndexOf('.'), fullName.lastIndexOf('$'));
            sb.append(fullName.substring(lastIndex + 1, fullName.length()));
            sb.append(":");
            sb.append(oa[i] instanceof Date ? DateUtils.formatDate((Date)oa[i], DateUtils.FORMAT_ISO8601) : oa[i].toString());
            if (i < oa.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private class CustomType {
        public String val () { return "custom"; }
        public String toString() { return ""; }
        public boolean equals (Object o) { return o instanceof CustomType; }
    }

    private class CustomSubType extends CustomType {
        @Override
        public String val () { return "custom-sub"; }
    }

    private abstract class StatefulFunc implements IFunctionHandler {
        public String val;
        @Override
        public boolean rawArgs () { return false; }
        @Override
        public boolean realTime () { return false; }
    }

    StatefulFunc read = new StatefulFunc () {
        @Override
        public String getName () { return "read"; }
        @Override
        public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
            p.add(new Class[0]);
            return p;
        }
        @Override
        public Object eval (Object[] args, EvaluationContext ec) { return val; }
    };

    StatefulFunc write = new StatefulFunc () {
        @Override
        public String getName () { return "write"; }
        @Override
        public List<Class[]> getPrototypes () { List<Class[]> p = new ArrayList<Class[]>();
            Class[] proto = {String.class};
            p.add(proto);
            return p;
        }
        @Override
        public Object eval (Object[] args, EvaluationContext ec) { val = (String)args[0]; return TRUE; }
    };
}


