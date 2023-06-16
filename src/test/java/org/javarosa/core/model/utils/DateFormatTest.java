package org.javarosa.core.model.utils;
import org.junit.Before;
import org.junit.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_HTTP;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_SUFFIX;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDateTime;
import static org.junit.Assert.assertEquals;
public class DateFormatTest {
    private LocalDateTime localDateTime;

    @Before
    public void setUp(){
        Instant instant = Instant.parse("2023-06-11T11:22:33.123Z");
        Clock clock = Clock.fixed(instant, ZoneId.of("Europe/London"));
        ZonedDateTime someDateTime = Instant.now(clock).atZone(ZoneId.of("UTC"));
        localDateTime = someDateTime.toLocalDateTime();
    }

    @Test
    public void formatsDateAsISO8601() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("2023-06-11", ISO8601.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsHumanShort() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("11/06/23", HUMAN_READABLE_SHORT.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsTimeStampSuffix() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("20230611", TIMESTAMP_SUFFIX.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsTimeStampHTTP() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("Sun, 11 Jun 2023", TIMESTAMP_HTTP.formatDate(dateToTest));
    }

    @Test
    public void formatsTimeAsISO8601() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("11:22:33.123+01:00", ISO8601.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsHumanShort() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("11:22", HUMAN_READABLE_SHORT.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsTimeStampSuffix() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("112233", TIMESTAMP_SUFFIX.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsTimeStampHTTP() {
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        assertEquals("10:22:33 UTC", TIMESTAMP_HTTP.formatTime(dateToTest));
    }
     /*
org.javarosa.core.model.utils.DateFormatterTest > formatsDateAsTimeStampHTTP FAILED
    org.junit.ComparisonFailure at DateFormatterTest.java:47
     */

}