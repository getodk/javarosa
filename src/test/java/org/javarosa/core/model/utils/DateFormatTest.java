package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_HTTP;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_SUFFIX;
import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDate;
import static org.junit.Assert.assertEquals;

public class DateFormatTest {
    @Test
    public void formatsDateAsISO8601(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("2023-06-11", ISO8601.formatDate(dateToTest));
    }

    @Test public void formatsDateAsHumanShort(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("11/06/23", HUMAN_READABLE_SHORT.formatDate(dateToTest));
    }
    @Test public void formatsDateAsTimeStampSuffix(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("20230611", TIMESTAMP_SUFFIX.formatDate(dateToTest));
    }

    @Test public void formatsDateAsTimeStampHTTP(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        assertEquals("Sat, 10 Jun 2023", TIMESTAMP_HTTP.formatDate(dateToTest));
    }

}