package org.javarosa.core.model.utils;

import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import static java.time.DayOfWeek.SUNDAY;
import static java.time.Month.JANUARY;
import static java.time.format.TextStyle.SHORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_ISO8601;
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_TIMESTAMP_HTTP;
import static org.javarosa.core.model.utils.DateFormatter.FORMAT_TIMESTAMP_SUFFIX;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDateTime;
import static org.junit.Assert.assertEquals;

public class DateFormatterTest {

    private LocalDateTime localDateTime;

    @Before
    public void setUp(){
        Instant instant = Instant.parse("2023-06-11T11:22:33.123Z");
        Clock clock = Clock.fixed(instant, ZoneId.of("Europe/London"));
        ZonedDateTime someDateTime = Instant.now(clock).atZone(ZoneId.of("UTC"));
        localDateTime = someDateTime.toLocalDateTime();
    }

    @Test
    public void formatsDateAsISO8601(){
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        String formattedDate = DateFormatter.formatDate(dateToTest, FORMAT_ISO8601);
        assertEquals("2023-06-11", formattedDate);
    }

    @Test public void formatsDateAsHumanShort(){
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        String formattedDate = DateFormatter.formatDate(dateToTest, FORMAT_HUMAN_READABLE_SHORT);
        assertEquals("11/06/23", formattedDate);
    }

    @Test public void formatsDateAsTimeStampSuffix(){
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        String formattedDate = DateFormatter.formatDate(dateToTest, FORMAT_TIMESTAMP_SUFFIX);
        assertEquals("20230611", formattedDate);
    }

    @Test public void formatsDateAsTimeStampHTTP(){
        Date dateToTest = dateFromLocalDateTime(localDateTime);
        String formattedDate = DateFormatter.formatDate(dateToTest, FORMAT_TIMESTAMP_HTTP);
        assertEquals("Sun, 11 Jun 2023", formattedDate);
    }

    @Test public void canFormatFreestyleMonth(){
        // Use a Sunday in January for our test
        LocalDateTime localDateTime = LocalDateTime.parse("2018-01-07T10:20:30.400");
        Date date = Date.from(localDateTime.toInstant(OffsetDateTime.now().getOffset()));
        assertThat(DateFormatter.format(date, "%b"), is(JANUARY.getDisplayName(SHORT, Locale.ENGLISH)));
        assertThat(DateFormatter.format(date, "%a"), is(SUNDAY.getDisplayName(SHORT, Locale.ENGLISH)));
    }

    /* from testEval("format-date('2018-01-02T10:20:30.123', \"%Y-%m-%e %H:%M:%S\")", "2018-01-2 10:20:30"); */
    @Test public void canFormatFreestyleDateTime(){
        LocalDateTime localDateTime = LocalDateTime.parse("2018-01-02T10:20:30.123");
        Date date = dateFromLocalDateTime(localDateTime);
        assertThat(DateFormatter.format(date, "%Y-%m-%e %H:%M:%S"), is("2018-01-2 10:20:30"));
    }

    /* from testEval("date-time('2000-01-01T10:20:30.000')", DateUtils.parseDateTime("2000-01-01T10:20:30.000")); */
    @Test public void dateTimeFromXPathEval(){
        Date parseDateTime = DateUtils.parseDateTime("2000-01-01T10:20:30.000");
        LocalDateTime localDateTime = LocalDateTime.parse("2000-01-01T10:20:30.000");
        assertThat(dateFromLocalDateTime(localDateTime), is(parseDateTime));
    }
}