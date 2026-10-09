/*
 * Copyright (C) 2012-14 Dobility, Inc.
 *
 * All rights reserved.
 */

package org.javarosa.core.model.utils.test;

import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Ignore;
import org.junit.Test;

public class DateUtilsParsingFormatting {

    @Test
    public void parseDateTime_convertsExplicitOffsetToSystemTimeZone() {
        withTimeZone(TimeZone.getTimeZone("GMT+02"), () -> {
            Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
            String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

            assertEquals("2014-10-04T23:03:05.244+02:00", str);
        });
    }

    @Test
    public void parseDateTime_appliesDaylightSavingTimeWhenConvertingOffsets() {
        withTimeZone(buildDstTimeZone(), () -> {
            Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
            String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

            assertEquals("2014-10-05T00:03:05.244+03:00", str);
        });
    }

    @Test
    public void parseTime_convertsExplicitOffsetToSystemTimeZone() {
        withTimeZone(TimeZone.getTimeZone("GMT+02"), () -> {
            String time = "12:03:05.011+03";

            Date date = DateUtils.parseTime(time);

            String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

            assertEquals("11:03:05.011+02:00", formatted);
        });
    }

    @Test
    @Ignore
    // parseTime() uses the current date to resolve timezone offsets. This test depends on whether DST
    // is active on the day it runs, so its expected result is not deterministic.
    public void parseTime_appliesDaylightSavingTimeWhenConvertingOffsets() {
        withTimeZone(buildDstTimeZone(), () -> {
            String time = "12:03:05.011+03";

            Date date = DateUtils.parseTime(time);

            String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

            assertEquals("12:03:05.011+03", formatted);
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
