package org.javarosa.xpath.test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.javarosa.core.model.utils.DateUtils.getDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnhandledException;
import org.javarosa.xpath.XPathUnsupportedException;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class XPathEvalJunitTest {
  private static final EvaluationContext EVALUATION_CONTEXT = createEvaluationContext();
  private static final EvaluationContext VARIABLE_CONTEXT = createVariableContext();
  private static final FormInstance INDEXED_REPEAT_INSTANCE_INDEX_ONE = createTestDataForIndexedRepeatFunction(1);
  private static final FormInstance INDEXED_REPEAT_INSTANCE_INDEX_NULL = createTestDataForIndexedRepeatFunction(null);
  @Parameterized.Parameter(value = 0)
  public String expression;

  @Parameterized.Parameter(value = 1)
  public Object expectedOutput;

  @Parameterized.Parameter(value = 2)
  public FormInstance instance;

  @Parameterized.Parameter(value = 3)
  public EvaluationContext evaluationContext;

  @Parameterized.Parameters(name = "expression: \"{0}\"")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"count(/data/path)", 5.0, createCountNonEmptyTestInstance(), null},
        {"count-non-empty(/data/path)", 3, createCountNonEmptyTestInstance(), null},
        {"(filter-expr)[5]", new XPathUnsupportedException(), createTestInstance(), null},
        {"(filter-expr)/data", new XPathUnsupportedException(), createTestInstance(), null},
        {"boolean(convertible())", TRUE, null, EVALUATION_CONTEXT},
        {"boolean(inconvertible())", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"number(convertible())", 5.0, null, EVALUATION_CONTEXT},
        {"number(inconvertible())", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"string(convertible())", "hi", null, EVALUATION_CONTEXT},
        {"string(inconvertible())", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"date(convertible())", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"testfunc()", TRUE, null, EVALUATION_CONTEXT},
        {"add(3,  5)", 8.0, null, EVALUATION_CONTEXT},
        {"add('17',  '-14')", 3.0, null, EVALUATION_CONTEXT},
        {"proto()", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"proto(5,  5)", "[Double:5.0,Double:5.0]", null, EVALUATION_CONTEXT},
        {"proto(6)", "[Double:6.0]", null, EVALUATION_CONTEXT},
        {"proto('asdf')", "[Double:NaN]", null, EVALUATION_CONTEXT},
        {"proto('7',  '7')", "[Double:7.0,Double:7.0]", null, EVALUATION_CONTEXT}, //note: args treated as doubles because
        {"proto(1.1,  'asdf', true())", "[Double:1.1,String:asdf,Boolean:true]", null, EVALUATION_CONTEXT},
        {"proto(false(),  false(), false())", "[Double:0.0,String:false,Boolean:false]", null, EVALUATION_CONTEXT},
        {"proto(1.1,  'asdf', inconvertible())", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"proto(1.1,  'asdf', true(), 16)", new XPathTypeMismatchException(), null, EVALUATION_CONTEXT},
        {"raw()", "[]", null, EVALUATION_CONTEXT},
        {"raw(5,  5)", "[Double:5.0,Double:5.0]", null, EVALUATION_CONTEXT},
        {"raw('7',  '7')", "[String:7,String:7]", null, EVALUATION_CONTEXT},
        {"raw('1.1',  'asdf', 17)", "[Double:1.1,String:asdf,Boolean:true]", null, EVALUATION_CONTEXT}, //convertible to prototype
        {"raw(get-custom(false()),  get-custom(true()))", "[CustomType:,CustomSubType:]", null, EVALUATION_CONTEXT},
        {"concat()", "", null, EVALUATION_CONTEXT},
        {"concat('a')", "a", null, EVALUATION_CONTEXT},
        {"concat('a', 'b','')", "ab", null, EVALUATION_CONTEXT},
        {"concat('ab', 'cde','','fgh',1,false(),'ijklmnop')", "abcdefgh1falseijklmnop", null, EVALUATION_CONTEXT},
        {"check-types(55,  '55', false(), '1999-09-09', get-custom(false()))", TRUE, null, EVALUATION_CONTEXT},
        {"check-types(55,  '55', false(), '1999-09-09', get-custom(true()))", TRUE, null, EVALUATION_CONTEXT},
        {"regex('12345', '[0-9]+')", TRUE, null, EVALUATION_CONTEXT},
        {"indexed-repeat( /data/repeat/name ,  /data/repeat , /data/index1 )", createExpectedNodesetFromIndexedRepeatFunction(INDEXED_REPEAT_INSTANCE_INDEX_ONE, 1, "name"), INDEXED_REPEAT_INSTANCE_INDEX_ONE, null},
        {"indexed-repeat( /data/repeat/name ,  /data/repeat , /data/index1 )", createExpectedNodesetFromIndexedRepeatFunction(INDEXED_REPEAT_INSTANCE_INDEX_NULL, 0, "name"), INDEXED_REPEAT_INSTANCE_INDEX_NULL, null},
        {"null-proto()", new NullPointerException(), null, EVALUATION_CONTEXT},
        //{"read()", "testing-read", null, EVALUATION_CONTEXT},
        //{"write('testing-write')", TRUE, null, EVALUATION_CONTEXT},
        {"$var_float_five", 5.0, null, VARIABLE_CONTEXT},
        {"$var_string_five", "five", null, VARIABLE_CONTEXT},
        {"$var_int_five", 5.0, null, VARIABLE_CONTEXT},
        {"$var_double_five", 5.0, null, VARIABLE_CONTEXT},
        {"/union | /expr", new XPathUnsupportedException(), null, null},
        {"/descendant::blah", new XPathUnsupportedException(), null, null},
        {"/cant//support", new XPathUnsupportedException(), null, null},
        {"/text()", new XPathUnsupportedException(), null, null},
        {"/namespace:*", new XPathUnsupportedException(), null, null},
        {"5", 5.0, null, null},
        {"555555.555", 555555.555, null, null},
        {".000555", 0.000555, null, null},
        {"0", 0.0, null, null},
        {"-5", -5.0, null, null},
        {"-0", -0.0, null, null},
        {"1230000000000000000000", 1.23e21, null, null},
        {"0.00000000000000000123", 1.23e-18, null, null},
        {"''", "", null, null},
        {"'\"'", "\"", null, null},
        {"\"test string\"", "test string", null, null},
        {"'   '", "   ", null, null},
        {"true()", TRUE, null, null},
        {"false()", FALSE, null, null},
        {"boolean(true())", TRUE, null, null},
        {"boolean(false())", FALSE, null, null},
        {"boolean(1)", TRUE, null, null},
        {"boolean(-1)", TRUE, null, null},
        {"boolean(0.0001)", TRUE, null, null},
        {"boolean(0)", FALSE, null, null},
        {"boolean(-0)", FALSE, null, null},
        {"boolean(number('NaN'))", FALSE, null, null},
        {"boolean(1 div 0)", TRUE, null, null},
        {"boolean(-1 div 0)", TRUE, null, null},
        {"boolean('')", FALSE, null, null},
        {"boolean('asdf')", TRUE, null, null},
        {"boolean('  ')", TRUE, null, null},
        {"boolean('false')", TRUE, null, null},
        {"boolean(date('2000-01-01'))", TRUE, null, null},
        {"number(true())", 1.0, null, null},
        {"number(false())", 0.0, null, null},
        {"number('100')", 100.0, null, null},
        {"number('100.001')", 100.001, null, null},
        {"number('.1001')", 0.1001, null, null},
        {"number('1230000000000000000000')", 1.23e21, null, null},
        {"number('0.00000000000000000123')", 1.23e-18, null, null},
        {"number('0')", 0.0, null, null},
        {"number('-0')", -0.0, null, null},
        {"number(' -12345.6789  ')", -12345.6789, null, null},
        {"number('NaN')", NaN, null, null},
        {"number('not a number')", NaN, null, null},
        {"number('- 17')", NaN, null, null},
        {"number('  ')", NaN, null, null},
        {"number('')", NaN, null, null},
        {"number('Infinity')", NaN, null, null},
        {"number('1.1e6')", NaN, null, null},
        {"number('34.56.7')", NaN, null, null},
        {"number(10)", 10.0, null, null},
        {"number(0)", 0.0, null, null},
        {"number(-0)", -0.0, null, null},
        {"number(-123.5)", -123.5, null, null},
        {"number(number('NaN'))", NaN, null, null},
        {"number(1 div 0)", POSITIVE_INFINITY, null, null},
        {"number(-1 div 0)", NEGATIVE_INFINITY, null, null},
        {"number(date('1970-01-01'))", 0.0, null, null},
        {"number(date('1970-01-02'))", 1.0, null, null},
        {"number(date('1969-12-31'))", -1.0, null, null},
        {"number(date('2008-09-05'))", 14127.0, null, null},
        {"number(date('1941-12-07'))", -10252.0, null, null},
        {"string(true())", "true", null, null},
        {"string(false())", "false", null, null},
        {"string(number('NaN'))", "NaN", null, null},
        {"string(1 div 0)", "Infinity", null, null},
        {"string(-1 div 0)", "-Infinity", null, null},
        {"string(0)", "0", null, null},
        {"string(-0)", "0", null, null},
        {"string(123456.0000)", "123456", null, null},
        {"string(-123456)", "-123456", null, null},
        {"string(1)", "1", null, null},
        {"string(-1)", "-1", null, null},
        {"string(.557586)", "0.557586", null, null},
        {"string('')", "", null, null},
        {"string('  ')", "  ", null, null},
        {"string('a string')", "a string", null, null},
        {"string(date('1989-11-09'))", "1989-11-09", null, null},
        {"substr('hello', 0)", "hello", null, null},
        {"substr('hello', 0,5)", "hello", null, null},
        {"substr('hello', 1)", "ello", null, null},
        {"substr('hello', 1,5)", "ello", null, null},
        {"substr('hello', 1,4)", "ell", null, null},
        {"substr('hello', -2)", "lo", null, null},
        {"substr('hello', 0,-1)", "hell", null, null},
        {"contains('a',  'a')", true, null, null},
        {"contains('a',  'b')", false, null, null},
        {"contains('abc',  'b')", true, null, null},
        {"contains('abc',  'bcd')", false, null, null},
        {"not(contains('a',  'b'))", true, null, null},
        {"starts-with('abc',  'a')", true, null, null},
        {"starts-with('',  'a')", false, null, null},
        {"starts-with('',  '')", true, null, null},
        {"ends-with('abc',  'a')", false, null, null},
        {"ends-with('abc',  'c')", true, null, null},
        {"ends-with('',  '')", true, null, null},
        {"date('2000-01-01')", getDate(2000, 1, 1), null, null},
        {"date('1945-04-26')", getDate(1945, 4, 26), null, null},
        {"date('1996-02-29')", getDate(1996, 2, 29), null, null},
        {"date('1983-09-31')", new XPathTypeMismatchException(), null, null},
        {"date('not a date')", new XPathTypeMismatchException(), null, null},
        {"date(0)", getDate(1970, 1, 1), null, null},
        {"date(6.5)", getDate(1970, 1, 7), null, null},
        {"date(1)", getDate(1970, 1, 2), null, null},
        {"date(-1)", getDate(1969, 12, 31), null, null},
        {"date(14127)", getDate(2008, 9, 5), null, null},
        {"date(-10252)", getDate(1941, 12, 7), null, null},
        {"date(date('1989-11-09'))", getDate(1989, 11, 9), null, null},
        {"date(true())", new XPathTypeMismatchException(), null, null},
        {"format-date('2018-01-02T10:20:30.123',  \"%Y-%m-%e %H:%M:%S\")", "2018-01-2 10:20:30", null, null},
        {"not(true())", FALSE, null, null},
        {"not(false())", TRUE, null, null},
        {"not('')", TRUE, null, null},
        {"boolean-from-string('true')", TRUE, null, null},
        {"boolean-from-string('false')", FALSE, null, null},
        {"boolean-from-string('whatever')", FALSE, null, null},
        {"boolean-from-string('1')", TRUE, null, null},
        {"boolean-from-string('0')", FALSE, null, null},
        {"boolean-from-string(1)", TRUE, null, null},
        {"boolean-from-string(1.0)", TRUE, null, null},
        {"boolean-from-string(1.0001)", FALSE, null, null},
        {"boolean-from-string(true())", TRUE, null, null},
        {"if(true(),  5, 'abc')", 5.0, null, null},
        {"if(false(),  5, 'abc')", "abc", null, null},
        {"if(6 > 7,  5, 'abc')", "abc", null, null},
        {"if('',  5, 'abc')", "abc", null, null},
        {"selected('apple baby crimson',  'apple')", TRUE, null, null},
        {"selected('apple baby crimson',  'baby')", TRUE, null, null},
        {"selected('apple baby crimson',  'crimson')", TRUE, null, null},
        {"selected('apple baby crimson',  '  baby  ')", TRUE, null, null},
        {"selected('apple baby crimson',  'babby')", FALSE, null, null},
        {"selected('apple baby crimson',  'bab')", FALSE, null, null},
        {"selected('apple',  'apple')", TRUE, null, null},
        {"selected('apple',  'ovoid')", FALSE, null, null},
        {"selected('',  'apple')", FALSE, null, null},
        {"5.5 + 5.5", 11.0, null, null},
        {"0 + 0", 0.0, null, null},
        {"6.1 - 7.8", -1.7, null, null},
        {"-3 + 4", 1.0, null, null},
        {"3 + -4", -1.0, null, null},
        {"1 - 2 - 3", -4.0, null, null},
        {"1 - (2 - 3)", 2.0, null, null},
        {"-(8*5)", -40.0, null, null},
        {"-'19'", -19.0, null, null},
        {"1.1 * -1.1", -1.21, null, null},
        {"-10 div -4", 2.5, null, null},
        {"2 * 3 div 8 * 2", 1.5, null, null},
        {"3 + 3 * 3", 12.0, null, null},
        {"1 div 0", POSITIVE_INFINITY, null, null},
        {"-1 div 0", NEGATIVE_INFINITY, null, null},
        {"0 div 0", NaN, null, null},
        {"3.1 mod 3.1", 0.0, null, null},
        {"5 mod 3.1", 1.9, null, null},
        {"2 mod 3.1", 2.0, null, null},
        {"0 mod 3.1", 0.0, null, null},
        {"5 mod -3", 2.0, null, null},
        {"-5 mod 3", -2.0, null, null},
        {"-5 mod -3", -2.0, null, null},
        {"5 mod 0", NaN, null, null},
        {"5 * (6 + 7)", 65.0, null, null},
        {"'123' * '456'", 56088.0, null, null},
        {"abs(-3.5)", 3.5, null, null},
        {"round('14.29123456789')", 14.0, null, null},
        {"round('14.6')", 15.0, null, null},
        {"round('14.29123456789',  0)", 14.0, null, null},
        {"round('14.29123456789',  1)", 14.3, null, null},
        {"round('14.29123456789',  1.5)", 14.3, null, null},
        {"round('14.29123456789',  2)", 14.29, null, null},
        {"round('14.29123456789',  3)", 14.291, null, null},
        {"round('14.29123456789',  4)", 14.2912, null, null},
        {"round('12345.14',      1)", 12345.1, null, null},
        {"round('-12345.14',     1)", -12345.1, null, null},
        {"round('12345.12345',   0)", 12345.0, null, null},
        {"round('12345.12345',  -1)", 12350.0, null, null},
        {"round('12345.12345',  -2)", 12300.0, null, null},
        {"round('12350.12345',  -2)", 12400.0, null, null},
        {"round('12345.12345',  -3)", 12000.0, null, null},
        {"round('4,6'            )", 5.0, null, null},
        {"round('1 div 0',  0)", NaN, null, null},
        {"round('14.5')", 15.0, null, null},
        {"round('NaN')", NaN, null, null},
        {"round('-NaN')", -NaN, null, null},
        {"round('0')", 0.0, null, null},
        {"round('-0')", -0.0, null, null},
        {"round('-0.5')", -0.0, null, null},
        {"round('14,6')", 15.0, null, null},
        {"round('12345.15',      1)", 12345.2, null, null},
        {"round('-12345.15',     1)", -12345.1, null, null},
        {"true() + 8", 9.0, null, null},
        {"date('2008-09-08') - date('1983-10-06')", 9104.0, null, null},
        {"true() and true()", TRUE, null, null},
        {"true() and false()", FALSE, null, null},
        {"false() and false()", FALSE, null, null},
        {"true() or true()", TRUE, null, null},
        {"true() or false()", TRUE, null, null},
        {"false() or false()", FALSE, null, null},
        {"true() or true() and false()", TRUE, null, null},
        {"(true() or true()) and false()", FALSE, null, null},
        {"true() or date('')", TRUE, null, null},
        {"false() and date('')", FALSE, null, null},
        {"'' or 17", TRUE, null, null},
        {"false() or 0 + 2", TRUE, null, null},
        {"(false() or 0) + 2", 2.0, null, null},
        {"4 < 5", TRUE, null, null},
        {"5 < 5", FALSE, null, null},
        {"6 < 5", FALSE, null, null},
        {"4 <= 5", TRUE, null, null},
        {"5 <= 5", TRUE, null, null},
        {"6 <= 5", FALSE, null, null},
        {"4 > 5", FALSE, null, null},
        {"5 > 5", FALSE, null, null},
        {"6 > 5", TRUE, null, null},
        {"4 >= 5", FALSE, null, null},
        {"5 >= 5", TRUE, null, null},
        {"6 >= 5", TRUE, null, null},
        {"-3 > -6", TRUE, null, null},
        {"true() > 0.9999", TRUE, null, null},
        {"'-17' > '-172'", TRUE, null, null},
        {"'abc' < 'abcd'", FALSE, null, null},
        {"date('2001-12-26') > date('2001-12-25')", TRUE, null, null},
        {"date('1969-07-20') < date('1969-07-21')", TRUE, null, null},
        {"false() and false() < true()", FALSE, null, null},
        {"(false() and false()) < true()", TRUE, null, null},
        {"6 < 7 - 4", FALSE, null, null},
        {"(6 < 7) - 4", -3.0, null, null},
        {"3 < 4 < 5", TRUE, null, null},
        {"3 < (4 < 5)", FALSE, null, null},
        {"true() = true()", TRUE, null, null},
        {"true() = false()", FALSE, null, null},
        {"true() != true()", FALSE, null, null},
        {"true() != false()", TRUE, null, null},
        {"3 = 3", TRUE, null, null},
        {"3 = 4", FALSE, null, null},
        {"3 != 3", FALSE, null, null},
        {"3 != 4", TRUE, null, null},
        {"6.1 - 7.8 = -1.7", TRUE, null, null},
        {"'abc' = 'abc'", TRUE, null, null},
        {"'abc' = 'def'", FALSE, null, null},
        {"'abc' != 'abc'", FALSE, null, null},
        {"'abc' != 'def'", TRUE, null, null},
        {"'' = ''", TRUE, null, null},
        {"true() = 17", TRUE, null, null},
        {"0 = false()", TRUE, null, null},
        {"true() = 'true'", TRUE, null, null},
        {"17 = '17.0000000'", TRUE, null, null},
        {"'0017.' = 17", TRUE, null, null},
        {"'017.' = '17.000'", FALSE, null, null},
        {"date('2004-05-01') = date('2004-05-01')", TRUE, null, null},
        {"true() != date('1999-09-09')", FALSE, null, null},
        {"false() and true() != true()", FALSE, null, null},
        {"(false() and true()) != true()", TRUE, null, null},
        {"-3 < 3 = 6 >= 6", TRUE, null, null},
        {"true(5)", new XPathUnhandledException(), null, null},
        {"number()", new XPathUnhandledException(), null, null},
        {"string('too',  'many', 'args')", new XPathUnhandledException(), null, null},
        {"not-a-function()", new XPathUnhandledException(), null, null},
        {"pow(2,  2)", 4.0, null, null},
        {"pow(2,  0)", 1.0, null, null},
        {"pow(0,  4)", 0.0, null, null},
        {"pow(2.5,  2)", 6.25, null, null},
        {"pow(0.5,  2)", .25, null, null},
        {"pow(-1,  2)", 1.0, null, null},
        {"pow(-1,  3)", -1.0, null, null},
        {"pow(4,  0.5)", 2.0, null, null},
        {"pow(16,  0.25)", 2.0, null, null},
        {"digest('some text',  'MD5', 'base64')", "VS4hzUzZkYZ448Gg30kbww==", null, null},
        {"digest('some text',  'SHA-1', 'base64')", "N6pjx3OY2VRHMmLhoAV8HmMu2nc=", null, null},
        {"digest('some text',  'SHA-256', 'base64')", "uU9vElx546X/qoJvWEwQ1SraZp5nYgUbgmtVd20FrtI=", null, null},
        {"digest('some text',  'SHA-384', 'base64')", "zJTsPphzwLmnJIZEKVj2cQZ833e5QnQW0DFEDMYgQeLuE0RJhEfsDO2fcENGG9Hz", null, null},
        {"digest('some text',  'SHA-512', 'base64')", "4nMrrtyj6sFAeChjfeHbynAsP8ns4Wz1Nt241hOc2F3+dGS4I1spgm9gjM9KxkPimxnGN4WKPYcQpZER30LdtQ==", null, null},
        {"digest('some text',  'MD5', 'hex')", "552e21cd4cd9918678e3c1a0df491bc3", null, null},
        {"digest('some text',  'MD5')", "VS4hzUzZkYZ448Gg30kbww==", null, null},
    });
  }

  @Test
  public void executeTest() {
    testEval(expression, instance, evaluationContext, expectedOutput);
  }

  private void testEval(String expr, FormInstance model, EvaluationContext ec, Object expected) {
    XPathExpression xpe = null;
    boolean exceptionExpected = (expected instanceof Throwable);
    if (ec == null) {
      ec = new EvaluationContext(model);
    }

    try {
      xpe = XPathParseTool.parseXPath(expr);
    } catch (XPathSyntaxException xpse) {
      System.out.println("Parsing " + expr + " failed with " + xpse.toString());
    }

    if (xpe == null) {
      fail("Null expression or syntax error");
    }

    try {
      Object result = xpe.eval(model, ec);

      if (exceptionExpected) {
        fail("Expected exception, expression : " + expr);
      } else if ((result instanceof Double && expected instanceof Double)) {
        if (!expected.equals(result)) {
          System.out.println(String.format("%s result %s differed from expected %s",
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
    } catch (Throwable e) {
      if (!exceptionExpected) {
        fail("Unexpected exception: " + e);
      } else if (e.getClass() != expected.getClass()) {
        fail("Did not get expected exception type");
      }
    }
  }

  private static FormInstance createCountNonEmptyTestInstance() {
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

  private static FormInstance createTestInstance() {
    TreeElement data = new TreeElement("data");
    data.addChild(new TreeElement("path"));
    return new FormInstance(data);
  }

  private static EvaluationContext createEvaluationContext() {
    EvaluationContext ec = new EvaluationContext(null);
    final Class[][] allPrototypes = {
        {Double.class, Double.class},
        {Double.class},
        {String.class, String.class},
        {Double.class, String.class, Boolean.class},
        {Boolean.class},
        {Boolean.class, Double.class, String.class, Date.class, CustomType.class}
    };

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "testfunc";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(new Class[0]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return TRUE;
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {

      @Override
      public String getName() {
        return "regex";
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        System.out.println("EVAL REGEX TESTS:");
        for (Object arg : args) {
          System.out.println("REGEX ARGS: " + arg.toString());
        }
        return TRUE; // String.re  args[0].
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[2]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

    });


    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "add";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[0]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return (Double) args[0] + (Double) args[1];
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "proto";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[0]);
        p.add(allPrototypes[1]);
        p.add(allPrototypes[2]);
        p.add(allPrototypes[3]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return printArgs(args);
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "raw";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[3]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return true;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return printArgs(args);
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "null-proto";
      }

      @Override
      public List<Class[]> getPrototypes() {
        return null;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return FALSE;
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "concat";
      }

      @Override
      public List<Class[]> getPrototypes() {
        return new ArrayList<>();
      }

      @Override
      public boolean rawArgs() {
        return true;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args)
          sb.append(XPathFuncExpr.toString(arg));
        return sb.toString();
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "convertible";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(new Class[0]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return new IExprDataType() {
          @Override
          public Boolean toBoolean() {
            return TRUE;
          }

          @Override
          public Double toNumeric() {
            return 5.0;
          }

          public String toString() {
            return "hi";
          }
        };
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "inconvertible";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(new Class[0]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return new Object();
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "get-custom";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[4]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        return (Boolean) args[0] ? new CustomSubType() : new CustomType();
      }
    });

    ec.addFunctionHandler(new IFunctionHandler() {
      @Override
      public String getName() {
        return "check-types";
      }

      @Override
      public List<Class[]> getPrototypes() {
        List<Class[]> p = new ArrayList<>();
        p.add(allPrototypes[5]);
        return p;
      }

      @Override
      public boolean rawArgs() {
        return false;
      }

      @Override
      public boolean realTime() {
        return false;
      }

      @Override
      public Object eval(Object[] args, EvaluationContext ec) {
        if (args.length != 5 || !(args[0] instanceof Boolean) || !(args[1] instanceof Double) ||
            !(args[2] instanceof String) || !(args[3] instanceof Date) || !(args[4] instanceof CustomType))
          fail("Types in custom function handler not converted properly/prototype not matched properly");

        return TRUE;
      }
    });
    return ec;
  }

  private static String printArgs(Object[] oa) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < oa.length; i++) {
      String fullName = oa[i].getClass().getName();
      int lastIndex = Math.max(fullName.lastIndexOf('.'), fullName.lastIndexOf('$'));
      sb.append(fullName.substring(lastIndex + 1, fullName.length()));
      sb.append(":");
      sb.append(oa[i] instanceof Date ? DateUtils.formatDate((Date) oa[i], DateUtils.FORMAT_ISO8601) : oa[i].toString());
      if (i < oa.length - 1)
        sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

  private static FormInstance createTestDataForIndexedRepeatFunction(Integer indexNodeValue) {
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

  private static XPathNodeset createExpectedNodesetFromIndexedRepeatFunction(FormInstance testInstance, int repeatIndex, String nodeName) {
    TreeReference referencedNode =
        testInstance.getRoot().getChildAt(repeatIndex).getChildrenWithName(nodeName).get(0).getRef();
    return new XPathNodeset(
        Collections.singletonList(referencedNode),
        testInstance,
        new EvaluationContext(new EvaluationContext(testInstance), referencedNode)
    );
  }

  private static EvaluationContext createVariableContext() {
    EvaluationContext ec = new EvaluationContext(null);

    ec.setVariable("var_float_five", 5.0f);
    ec.setVariable("var_string_five", "five");
    ec.setVariable("var_int_five", 5);
    ec.setVariable("var_double_five", 5.0);

    return ec;
  }

  private static class CustomType {
    public String val() {
      return "custom";
    }

    public String toString() {
      return "";
    }

    public boolean equals(Object o) {
      return o instanceof CustomType;
    }
  }

  private static class CustomSubType extends CustomType {
    @Override
    public String val() {
      return "custom-sub";
    }
  }
}


