package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

import static java.time.DayOfWeek.SUNDAY;
import static java.time.Month.JANUARY;
import static java.time.format.TextStyle.SHORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDate;
import static org.junit.Assert.assertEquals;

public class DateFormatterTest {
    @Test
    public void formatsDateAsISO8601(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_ISO8601);
        assertEquals("2023-06-11", formattedDate);
    }

    @Test public void formatsDateAsHumanShort(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_HUMAN_READABLE_SHORT);
        assertEquals("11/06/23", formattedDate);
    }

    @Test public void formatsDateAsTimeStampSuffix(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_TIMESTAMP_SUFFIX);
        assertEquals("20230611", formattedDate);
    }

    @Test public void formatsDateAsTimeStampHTTP(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_TIMESTAMP_HTTP);
        assertEquals("Sat, 10 Jun 2023", formattedDate);
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
        Date date = DateUtils.dateFromLocalDate(localDateTime);
        assertThat(DateFormatter.format(date, "%Y-%m-%e %H:%M:%S"), is("2018-01-2 10:20:30"));
    }

    /* from testEval("date-time('2000-01-01T10:20:30.000')", DateUtils.parseDateTime("2000-01-01T10:20:30.000")); */
    @Test public void dateTimeFromXPathEval(){
        Date parseDateTime = DateUtils.parseDateTime("2000-01-01T10:20:30.000");
        LocalDateTime localDateTime = LocalDateTime.parse("2000-01-01T10:20:30.000");
        assertThat(dateFromLocalDate(localDateTime), is(parseDateTime));
    }
}