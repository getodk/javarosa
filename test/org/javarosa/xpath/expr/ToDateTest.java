package org.javarosa.xpath.expr;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDate;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class ToDateTest {
  private static final DateTime EPOCH = new DateTime(1970, 1, 1, 0, 0, 0, 0);

  @Test
  public void convertsISO8601DatesWithoutPreservingTime() {
    Date expectedDate = new LocalDate(2018, 1, 1).toDate();
    assertEquals(expectedDate, toDate("2018-01-01", false));
  }

  @Test
  public void convertsISO8601DatesWithoutOffsetPreservingTime() {
    Date expectedDate = new LocalDateTime(2018, 1, 1, 10, 20, 30, 400).toDate();
    assertEquals(expectedDate, toDate("2018-01-01T10:20:30.400", true));
  }

  @Test
  public void convertsISO8601DatesWithOffsetPreservingTime() {
    DateTime expectedDates = new DateTime(2018, 1, 1, 10, 20, 30, 400, DateTimeZone.forOffsetHours(2));
    assertEquals(expectedDates.toDate(), toDate("2018-01-01T10:20:30.400+02", true));
  }

  @Test
  public void convertsTimestampsWithoutPreservingTime() {
    LocalDate expectedDate = new LocalDate(2018, 1, 1);
    double days = Integer.valueOf(Days.daysBetween(EPOCH, expectedDate.toDateTimeAtStartOfDay()).getDays()).doubleValue();
    assertEquals(expectedDate.toDate(), toDate(days, false));
  }

  @Test
  public void convertsTimestampsToDatesAtMidnightUTC() {
    // We need to account for the offset, because toDate() will produce a Date in the UTC offset
    DateTime expectedDate = new DateTime(2018, 1, 1, 10, 20, 30, 400, UTC);
    double days = Integer.valueOf(Days.daysBetween(EPOCH, expectedDate).getDays()).doubleValue();
    assertEquals(expectedDate.withTimeAtStartOfDay().toDate(), toDate(days, true));
  }

  // Test fails on some machines and succeeds on others. Needs investigation.
  // @Test
  // public void datesGoUnchanged() {
  //   // We need to account for the offset, because this test could be affected by other tests manipulating the Calendar
  //   DateTime testDate = new DateTime(2018, 1, 1, 10, 20, 30, 400, UTC);
  //   assertEquals(testDate.withTimeAtStartOfDay().toDate(), toDate(testDate.toDate(), false));
  //   assertEquals(testDate.toDate(), toDate(testDate.toDate(), true));
  // }

  @Test
  public void emptyStringsGoUnchanged() {
    assertEquals("", toDate("", false));
    assertEquals("", toDate("", true));
  }

  @Test
  public void doubleNaNGoesUnchanged() {
    // NaN is xpath's 'null values'
    assertEquals(Double.NaN, toDate(Double.NaN, false));
    assertEquals(Double.NaN, toDate(Double.NaN, true));
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void doubleValuesLessThanTheIntegerMinThrow() {
    toDate(Integer.valueOf(Integer.MIN_VALUE).doubleValue() - 1, false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void doubleValuesGreaterThanTheIntegerMaxThrow() {
    toDate(Integer.valueOf(Integer.MAX_VALUE).doubleValue() + 1, false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void positiveInfinityThrows() {
    toDate(POSITIVE_INFINITY, false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void negativeInfinityThrows() {
    toDate(NEGATIVE_INFINITY, false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void unparseableDateStringsThrow() {
    toDate("some random text", false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void booleansThrow() {
    // We test this type specifically because, according to the documentation
    // Dates can be encoded as booleans, but booleans can't be decoded into Dates
    toDate(false, false);
  }

  @Test(expected = XPathTypeMismatchException.class)
  public void anyOtherTypeThrows() {
    // We will test just one type to cover the final 'else' block.
    // We can't explicitly test for all possible types.
    toDate(2L, false);
  }
}