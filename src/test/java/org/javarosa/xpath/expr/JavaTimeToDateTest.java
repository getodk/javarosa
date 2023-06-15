package org.javarosa.xpath.expr;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDate;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDateTime;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDate;
import static org.junit.Assert.assertEquals;

// TODO Migrate to java.time for conformity
//copy of JodaTimeToDateTest to implement same tests in java.time before changing the xpath expressions
public class JavaTimeToDateTest {
    private static final DateTime EPOCH
            = new DateTime(1970, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
    public static final ZonedDateTime EPOCH_UTC_ZONED_DATE_TIME
            = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")); // 1970-01-01T00:00:00 UTC

    @NotNull
    private static ZonedDateTime adjustLocalDateToOffsetTimeZone(LocalDateTime localDateTime, int hoursToOffset) {
        ZoneOffset offsetFromLocal = ZoneOffset.ofHours(hoursToOffset);
        ZoneId zoneIdFromLocal = ZoneId.ofOffset("GMT", offsetFromLocal);
        ZoneId resetZoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(0));
        return ZonedDateTime.of(localDateTime.toLocalDate(), localDateTime.toLocalTime(), zoneIdFromLocal).withZoneSameInstant(resetZoneId);
    }

    private static int secTicksAsNanoSeconds(int millis) {
        return Math.toIntExact(TimeUnit.NANOSECONDS.convert(millis, TimeUnit.MILLISECONDS));
    }

    private static Date date(int year, int month, int day, int hour, int minute, int second, int milli) {
        return dateFromLocalDateTime(localDateTime(year, month, day, hour, minute, second, milli));
    }

    @NotNull
    private static LocalDateTime localDateTime(int year, int month, int day, int hour, int minute, int second, int milli) {
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, secTicksAsNanoSeconds(milli)));
    }

    @NotNull
    private static Date dateFromZonedDateTime(ZonedDateTime zonedDateTime) {
        return DateUtils.dateFromLocalDateTime(zonedDateTime.toLocalDateTime());
    }

    @NotNull
    private static Date epochPlusAYear(String tzID, int daysFromEpoch) {
        ZonedDateTime zonedDateTime = EPOCH_UTC_ZONED_DATE_TIME.plusDays(daysFromEpoch); // 1971-01-01T00:00:00 UTC
        zonedDateTime = zonedDateTime.withZoneSameLocal(ZoneId.of(tzID));
        return dateFromLocalDateTime(zonedDateTime.toLocalDateTime());
    }


    @Test
    public void convertsISO8601DatesWithoutPreservingTime() {
        assertEquals(
                dateFromLocalDate(LocalDate.of(2018, 1, 1)),
                toDate("2018-01-01", false)
        );
    }

    @Test
    public void convertsISO8601DatesWithoutOffsetPreservingTime() {
        //old
        assertEquals(
                date(2018, 1, 1, 10, 20, 30, 400).toInstant().toEpochMilli(),
                ((Date) toDate("2018-01-01T10:20:30.400", true)).toInstant().toEpochMilli()
        );
        //new
        assertEquals(
                date(2018, 1, 1, 10, 20, 30, 400),
                toDate("2018-01-01T10:20:30.400", true)
        );
    }

    @Test
    public void convertsISO8601DatesWithOffsetPreservingTime() {
        //old
        assertEquals(
                new DateTime(2018, 1, 1, 10, 20, 30, 400, DateTimeZone.forOffsetHours(2)).toDate(),
                toDate("2018-01-01T10:20:30.400+02", true)
        );

        //new
        LocalDateTime localDateTime = localDateTime(2018, 1, 1, 10, 20, 30, 400);
        ZonedDateTime zonedDateTime = adjustLocalDateToOffsetTimeZone(localDateTime, 2);
        assertEquals(
                dateFromZonedDateTime(zonedDateTime),
                toDate("2018-01-01T10:20:30.400+02", true)
        );
    }

    @Test
    public void convertsTimestampsWithoutPreservingTime() {
        //old
        withTimeZone(TimeZone.getTimeZone("Z"), () -> assertEquals(
                EPOCH.withZone(DateTimeZone.getDefault()).plusDays(365).toDate(),
                toDate(365d, false)
        ));
        //new
        assertEquals(
                dateFromZonedDateTime(EPOCH_UTC_ZONED_DATE_TIME.plusDays(365)), // 1971-01-01T00:00:00 UTC
                toDate(365d, false));
    }

    @Test
    public void convertsTimestampsWithoutPreservingTimeOnLocalTimeZone() {
        //new
        TimeZone PST = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"));
        withTimeZone(PST, tz -> {
                    Date datedFromLocalDateTime = epochPlusAYear(tz.getID(), 365);
                    assertEquals(
                            datedFromLocalDateTime,
                            toDate(365d, false)
                    );
                }
        );
    }

    @Test
    public void convertsTimestampsToDatesAtMidnightUTC() {
        //old
        DateTime expectedDate = EPOCH.withZone(DateTimeZone.UTC).plusDays(365);
        assertEquals(
                expectedDate.toDate(),
                toDate(365d, true)
        );
    }

    @Test
    public void datesGoUnchanged() {
        Date date = dateFromLocalDate(LocalDate.of(2018, 1, 1));
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

}
