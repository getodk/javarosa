package org.javarosa.xpath.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
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
    public void toNumericForDates_atStartOfDay_returnsWholeDay() {
        Date jan2_1970Utc = new Date(24 * 60 * 60 * 1000L);

        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toNumeric(jan2_1970Utc), equalTo(1.0))
        );
    }

    @Test
    public void toNumericForDates_usesDefaultTimezone() {
        Date jan2_1970Utc = new Date(24 * 60 * 60 * 1000L);

        // The instant at midnight UTC on Jan 2, 1970 was still Jan 1, 1970 in GMT-8
        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () ->
            assertThat(toNumeric(jan2_1970Utc), equalTo(0.0))
        );
    }

    @Test
    public void toNumericForDateTime_discardsFractionalDay() {
        Date jan2_1970Utc_6am = new Date(30 * 60 * 60 * 1000L);

        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toNumeric(jan2_1970Utc_6am), equalTo(1.0))
        );
    }

    @Test
    public void toDoubleForDateTime_includesFractionalDay() {
        Date jan2_1970Utc_6am = new Date(30 * 60 * 60 * 1000L);

        withTimeZone(TimeZone.getTimeZone("UTC"), () ->
            assertThat(toDouble(jan2_1970Utc_6am), equalTo(1.25))
        );
    }

    @Test
    public void toDoubleForDateTime_usesDefaultTimezone() {
        Date jan2_1970Utc = new Date(24 * 60 * 60 * 1000L);

        // Midnight UTC on Jan 2, 1970 is 16:00 on Jan 1 in GMT-8
        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () ->
            assertThat(toDouble(jan2_1970Utc), closeTo(16.0 / 24, 1e-10))
        );
    }
}
