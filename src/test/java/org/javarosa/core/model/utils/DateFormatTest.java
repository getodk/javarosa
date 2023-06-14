package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_HTTP;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_SUFFIX;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDate;
import static org.junit.Assert.assertEquals;

public class DateFormatTest {
    @Test
    public void formatsDateAsISO8601() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("2023-06-11", ISO8601.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsHumanShort() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("11/06/23", HUMAN_READABLE_SHORT.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsTimeStampSuffix() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("20230611", TIMESTAMP_SUFFIX.formatDate(dateToTest));
    }

    @Test
    public void formatsDateAsTimeStampHTTP() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("Sat, 10 Jun 2023", TIMESTAMP_HTTP.formatDate(dateToTest));
    }

    @Test
    public void formatsTimeAsISO8601() {
        LocalDateTime someLocalDate = LocalDateTime.parse("2023-06-13T11:22:33.123");
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("11:22:33.123+01:00", ISO8601.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsHumanShort() {
        LocalDateTime someLocalDate = LocalDateTime.parse("2023-06-13T11:22:33.123");
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("11:22", HUMAN_READABLE_SHORT.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsTimeStampSuffix() {
        LocalDateTime someLocalDate = LocalDateTime.parse("2023-06-13T11:22:33.123");
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("112233", TIMESTAMP_SUFFIX.formatTime(dateToTest));
    }

    @Test
    public void formatsTimeAsTimeStampHTTP() {
        LocalDateTime someLocalDate = LocalDateTime.parse("2023-06-13T11:22:33.123");
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("10:22:33 UTC", TIMESTAMP_HTTP.formatTime(dateToTest));
    }
}