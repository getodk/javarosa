package org.javarosa.core.model.utils.test;

import static org.javarosa.core.model.utils.DateFormatter.FORMAT_ISO8601;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.javarosa.core.model.utils.DateFormatter;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;

public class DateUtilsSCTOTests {

    @Test
    public void testParseDateTime() {
        withTimeZone(TimeZone.getTimeZone("GMT+02"), () -> {
            Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
            String str = DateFormatter.formatDateTime(date, FORMAT_ISO8601);

            assertEquals("2014-10-04T23:03:05.244+02:00", str);
        });
    }

    @Test
    public void testParseDateTime_withDST() {
        withTimeZone(buildDstTimeZone(), () -> {
            Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
            String str = DateFormatter.formatDateTime(date, FORMAT_ISO8601);

            assertEquals("2014-10-05T00:03:05.244+03:00", str);
        });
    }

    @Test
    public void testParseTime() {
        withTimeZone(TimeZone.getTimeZone("GMT+02"), () -> {
            String time = "12:03:05.011+03";
            Date date = DateUtils.parseTime(time);
            String formatted = DateFormatter.formatTime(date, FORMAT_ISO8601);
            assertEquals("11:03:05.011+02:00", formatted);
        });
    }

    private SimpleTimeZone buildDstTimeZone() {
        return new SimpleTimeZone(
            2 * 60 * 60 * 1000,
            "Europe/Athens",
            Calendar.JANUARY, 1, 0,
            0, SimpleTimeZone.UTC_TIME,
            Calendar.DECEMBER, 31, 0,
            24 * 60 * 60 * 1000, SimpleTimeZone.UTC_TIME,
            60 * 60 * 1000);
    }
}
