package org.javarosa.xpath.expr;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.javarosa.xpath.expr.XPathFuncExpr.toLongHash;
import static org.javarosa.xpath.expr.XPathFuncExpr.toNumeric;

import java.util.Date;

public class XPathFuncAsSomethingTest {

    @Test
    public void toLongHashHashesWell() {
        assertThat(toLongHash("Hello"), equalTo(1756278180214341157L));
        assertThat(toLongHash(""), equalTo(-2039914840885289964L));
    }

    @Test
    public void toNumericHandlesBooleans() {
        assertThat(toNumeric(true), equalTo(1.0));
        assertThat(toNumeric(false), equalTo(0.0));
    }

    @Test
    public void toNumericHandlesStrings() {
        assertThat(toNumeric("  123  "), equalTo(123.0));
        assertThat(toNumeric("  123.0  "), equalTo(123.0));
        assertThat(toNumeric("  123.4  "), equalTo(123.4));
        assertThat(toNumeric("  123,4  "), equalTo(123.4));

        assertThat(toNumeric("0x12"), not(18.0));
        assertThat(toNumeric("0x12"), equalTo(Double.NaN));
    }

    @Test
    public void toNumericHandlesDates() {
        assertThat(toNumeric(new Date(86400 * 1000L)), equalTo(1.0));
    }
}