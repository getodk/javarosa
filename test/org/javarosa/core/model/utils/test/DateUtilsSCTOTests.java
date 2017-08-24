/*
 * Copyright (C) 2012-14 Dobility, Inc.
 *
 * All rights reserved.
 */

package org.javarosa.core.model.utils.test;

import junit.framework.TestCase;
import org.javarosa.core.model.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateUtilsSCTOTests extends TestCase {

    public void testParseDateTime() throws Exception {
        TimeZone timeZone = clearDST("GMT+02");

        Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
        String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("2014-10-04T23:03:05.244+02", str);

        restoreTimeZone(timeZone);
    }

    public void testParseDateTime_withDST() throws Exception {
        TimeZone timeZone = applyDST();

        Date date = DateUtils.parseDateTime("2014-10-05T00:03:05.244+03");
        String str = DateUtils.formatDateTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("2014-10-05T00:03:05.244+03", str);

        restoreTimeZone(timeZone);
    }

    public void testParseTime() throws Exception {
        TimeZone timeZone = clearDST("GMT+02");

        String time = "12:03:05.011+03";

        Date date = DateUtils.parseTime(time);

        String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("12:03:05.011+02", formatted);

        restoreTimeZone(timeZone);
    }

    public void testParseTime_withDST() throws Exception {
        TimeZone timeZone = applyDST();

        String time = "12:03:05.011+03";

        Date date = DateUtils.parseTime(time);

        String formatted = DateUtils.formatTime(date, DateUtils.FORMAT_ISO8601);

        assertEquals("12:03:05.011+03", formatted);

        restoreTimeZone(timeZone);
    }

    protected TimeZone clearDST(String id) {
        TimeZone backupZone = TimeZone.getDefault();

        TimeZone.setDefault(TimeZone.getTimeZone(id));

        return backupZone;
    }

    protected TimeZone applyDST() {
        TimeZone backupZone = TimeZone.getDefault();

        // this is a timezone that operates DST every day of the year!
        SimpleTimeZone dstTimezone = new SimpleTimeZone(
                2*60*60*1000,
                "Europe/Athens",
                Calendar.JANUARY, 1, 0,
                0, SimpleTimeZone.UTC_TIME,
                Calendar.DECEMBER, 31, 0,
                24*60*60*1000, SimpleTimeZone.UTC_TIME,
                60*60*1000);
        TimeZone.setDefault(dstTimezone);

        return backupZone;
    }

    private void restoreTimeZone(TimeZone backupZone) {
        TimeZone.setDefault(backupZone);
    }
}