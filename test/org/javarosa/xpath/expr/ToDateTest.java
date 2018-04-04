package org.javarosa.xpath.expr;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.time.Instant.EPOCH;
import static java.time.ZoneId.SHORT_IDS;
import static java.time.ZoneOffset.UTC;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDate;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ToDateTest {

    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone(ZoneOffset.UTC);
    private static TimeZone backupTimeZone;

    private static Date date(int year, int month, int day) {
        return date(LocalDate.of(year, month, day).atStartOfDay().atZone(ZoneId.systemDefault()));
    }

    private static Date date(int year, int month, int day, int hour, int minute, int second, int milli) {
        return date(LocalDateTime.of(year, month, day, hour, minute, second, milli * 1_000_000).atZone(ZoneId.systemDefault()));
    }

    private static Date date(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    private static Date date(OffsetDateTime offsetDateTime) {
        return Date.from(offsetDateTime.toInstant());
    }

    @BeforeClass
    public static void forceUTCOffset() {
        // All the tests run on the UTC offset by default.
        backupTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TIME_ZONE);
    }

    @AfterClass
    public static void restoreTimeZone() {
        TimeZone.setDefault(backupTimeZone);
    }

    @Test
    public void convertsISO8601DatesWithoutPreservingTime() {
        assertEquals(
            date(2018, 1, 1),
            toDate("2018-01-01", false)
        );
    }

    @Test
    public void convertsISO8601DatesWithoutOffsetPreservingTime() {
        assertEquals(
            date(2018, 1, 1, 10, 20, 30, 400),
            toDate("2018-01-01T10:20:30.400", true)
        );
    }

    @Test
    public void convertsISO8601DatesWithOffsetPreservingTime() {
        assertEquals(
            date(OffsetDateTime.of(2018, 1, 1, 10, 20, 30, 400_000_000, ZoneOffset.ofHours(2))),
            toDate("2018-01-01T10:20:30.400+02", true)
        );
    }

    @Test
    public void convertsTimestampsWithoutPreservingTime() {
        assertEquals(
            date(EPOCH.atZone(ZoneId.systemDefault()).plusDays(365)),
            toDate(365d, false)
        );
    }

    @Test
    public void convertsTimestampsWithoutPreservingTimeOnLocalTimeZone() {
        runOnTimeZone(
            ZoneId.of("PST", SHORT_IDS),
            zoneId -> {
                ZonedDateTime utcEpoch = EPOCH.atZone(UTC); // 1970-01-01T00:00:00 UTC
                ZonedDateTime properExpectedDate = utcEpoch.plusDays(365); // 1971-01-01T00:00:00 UTC
                // Explanation for all this:
                //
                // Since our current implementation is based on java.util.Date, we are forced to
                // use the default timezone, and we need to compensate for that. Otherwise, we will
                // get offset dates by the same amount of hours as the timezone we're running this
                // test (PST aka America/Los_Angeles: -6 hours).
                //
                // By running zonedDateTime.withZoneSameLocal(zoneId), we offset the date to represent
                // the same local datetime in our target zone.
                // 1971-01-01T00:00:00 PST (or 1971-01-01T06:00:00 UTC which is the EPOCH plus
                // 365 days *and 6 hours*, but will make our code work)
                ZonedDateTime expectedDate = properExpectedDate.withZoneSameLocal(zoneId);
                assertEquals(
                    date(expectedDate),
                    toDate(365d, false)
                );
            }
        );
    }

    @Test
    public void convertsTimestampsToDatesAtMidnightUTC() {
        OffsetDateTime expectedDate = EPOCH.atOffset(UTC).plusDays(365);
        assertEquals(
            date(expectedDate),
            toDate(365d, true)
        );
    }

    @Test
    public void datesGoUnchanged() {
        Date date = date(2018, 1, 1);
        assertEquals(date, toDate(date, false));
        assertEquals(date, toDate(date, true));
    }

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

    private void runOnTimeZone(ZoneId zoneId, Consumer<ZoneId> block) {
        TimeZone backup = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId.getId()));
        block.accept(zoneId);
        TimeZone.setDefault(backup);
    }
}