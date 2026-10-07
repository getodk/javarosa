package org.javarosa.xpath.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDouble;
import static org.javarosa.xpath.expr.XPathFuncExpr.toLongHash;
import static org.javarosa.xpath.expr.XPathFuncExpr.toNumeric;

import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;

public class XPathFuncAsSomethingTest {

    @Test
    public void toLongHashHashesWell() {
        assertThat(toLongHash("Hello"), equalTo(1756278180214341157L));
        assertThat(toLongHash(""), equalTo(0L)); // the empty string would actually hash to -2039914840885289964L; but we've added this quirk for backward compatibility.
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
    public void toNumericAtStartOfDay_returnsWholeDay() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toNumeric(new Date(24 * 60 * 60 * 1000L)), equalTo(1.0))
        );
    }

    @Test
    public void toNumericWithTimeOfDay_discardsFractionalDay() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toNumeric(new Date(30 * 60 * 60 * 1000L)), equalTo(1.0))
        );
    }

    @Test
    public void toDoubleWithTimeOfDay_includesFractionalDay() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toDouble(new Date(30 * 60 * 60 * 1000L)), equalTo(1.25))
        );
    }
}
