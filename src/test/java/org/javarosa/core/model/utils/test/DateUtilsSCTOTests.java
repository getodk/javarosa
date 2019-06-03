/*
 * Copyright (C) 2012-14 Dobility, Inc.
 *
 * All rights reserved.
 */

package org.javarosa.core.model.utils.test;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DateUtilsSCTOTests {

    private Locale backupLocale;
    private TimeZone backupZone;

    @Before
    public void setUp() {
        backupLocale = Locale.getDefault();
        backupZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(backupZone);
        Locale.setDefault(backupLocale);
    }

    @Test
    public void testParseDateTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+02"));

        Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
        String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("2014-10-04T23:03:05.244+02:00", str);
    }

    @Test
    public void testParseDateTime_withDST() {
        applyDST();

        Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
        String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("2014-10-05T00:03:05.244+03:00", str);
    }

    @Test
    public void testParseTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+02"));

        String time = "12:03:05.011+03";

        Date date = DateUtils.parseTime(time);

        String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("11:03:05.011+02:00", formatted);
    }

    @Test
    @Ignore
    // This test doesn't make sense:
    // - A time has no offset nor zone. It can only have one
    //   when bound to a date, which is not the case
    // - We're effectively binding all times to the EPOCH date
    //   (1970-01-01, UTC), which has no DST
    public void testParseTime_withDST() {
        applyDST();

        String time = "12:03:05.011+03";

        Date date = DateUtils.parseTime(time);

        String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("12:03:05.011+03", formatted);
    }

    private void applyDST() {
        // this is a timezone that operates DST every day of the year!
        SimpleTimeZone dstTimezone = new SimpleTimeZone(
                2 * 60 * 60 * 1000,
                "Europe/Athens",
                Calendar.JANUARY, 1, 0,
                0, SimpleTimeZone.UTC_TIME,
                Calendar.DECEMBER, 31, 0,
                24 * 60 * 60 * 1000, SimpleTimeZone.UTC_TIME,
                60 * 60 * 1000);
        TimeZone.setDefault(dstTimezone);
    }
}
