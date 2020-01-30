/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.utils.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.DateUtils.DateFields;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DateUtilsTests {

    private Locale backupLocale;
    private TimeZone backupZone;
    private Date testDate;
    private LocalDateTime testLocalDateTime;

    @Before
    public void setUp() {
        backupLocale = Locale.getDefault();
        backupZone = TimeZone.getDefault();
        testLocalDateTime = LocalDateTime.of(2018, 1, 1, 10, 20, 30, 400);
        testDate = Date.from(testLocalDateTime.toInstant(OffsetDateTime.now().getOffset()));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(backupZone);
        Locale.setDefault(backupLocale);
    }

    /**
     * This test ensures that the Strings returned
     * by the getXMLStringValue function are in
     * the proper XML compliant format.
     */
    @Test
    public void testGetXMLStringValueFormat() {
        String currentDate = DateUtils.getXMLStringValue(testDate);
        assertEquals("The date string was not of the proper length", currentDate.length(), "YYYY-MM-DD".length());
        assertEquals("The date string does not have proper year formatting", currentDate.indexOf("-"), "YYYY-".indexOf("-"));
        assertEquals(testLocalDateTime.getYear(), Integer.parseInt(currentDate.substring(0, 4)));
        assertEquals(testLocalDateTime.getMonth().getValue(), Integer.parseInt(currentDate.substring(5, 7)));
        assertEquals(testLocalDateTime.getDayOfMonth(), Integer.parseInt(currentDate.substring(8, 10)));
    }

    @Test
    public void testDateTimeParses() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        assertThat(parseDateTime("2016-04-13T16:26:00.000-07"), is(OffsetDateTime.parse("2016-04-13T16:26:00.000-07:00")));
        assertThat(parseDateTime("2015-12-16T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-16T16:09:00.000-08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-12-16T07:09:00.000+08"), is(OffsetDateTime.parse("2015-12-16T07:09:00.000+08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-11-30T16:09:00.000-08"), is(OffsetDateTime.parse("2015-11-30T16:09:00.000-08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-11-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-11-01T07:09:00.000+08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-12-31T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-31T16:09:00.000-08:00"))); // wraps year!!!
        assertThat(parseDateTime("2015-01-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-01-01T07:09:00.000+08:00"))); // wraps year!!!
        assertThat(parseDateTime("2016-01-26T10:39:00.000-08"), is(OffsetDateTime.parse("2016-01-26T10:39:00.000-08:00")));

        TimeZone.setDefault(TimeZone.getTimeZone("PST"));

        assertThat(parseDateTime("2016-04-13T16:26:00.000-07"), is(OffsetDateTime.parse("2016-04-13T16:26:00.000-07:00")));
        assertThat(parseDateTime("2015-12-16T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-16T16:09:00.000-08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-12-16T07:09:00.000+08"), is(OffsetDateTime.parse("2015-12-16T07:09:00.000+08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-11-30T16:09:00.000-08"), is(OffsetDateTime.parse("2015-11-30T16:09:00.000-08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-11-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-11-01T07:09:00.000+08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-12-31T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-31T16:09:00.000-08:00"))); // wraps year!!!
        assertThat(parseDateTime("2015-01-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-01-01T07:09:00.000+08:00"))); // wraps year!!!
        assertThat(parseDateTime("2016-01-26T10:39:00.000-08"), is(OffsetDateTime.parse("2016-01-26T10:39:00.000-08:00")));

        TimeZone.setDefault(TimeZone.getTimeZone("PDT"));

        assertThat(parseDateTime("2016-04-13T16:26:00.000-07"), is(OffsetDateTime.parse("2016-04-13T16:26:00.000-07:00")));
        assertThat(parseDateTime("2015-12-16T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-16T16:09:00.000-08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-12-16T07:09:00.000+08"), is(OffsetDateTime.parse("2015-12-16T07:09:00.000+08:00"))); // wraps day!!!
        assertThat(parseDateTime("2015-11-30T16:09:00.000-08"), is(OffsetDateTime.parse("2015-11-30T16:09:00.000-08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-11-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-11-01T07:09:00.000+08:00"))); // wraps month!!!
        assertThat(parseDateTime("2015-12-31T16:09:00.000-08"), is(OffsetDateTime.parse("2015-12-31T16:09:00.000-08:00"))); // wraps year!!!
        assertThat(parseDateTime("2015-01-01T07:09:00.000+08"), is(OffsetDateTime.parse("2015-01-01T07:09:00.000+08:00"))); // wraps year!!!
        assertThat(parseDateTime("2016-01-26T10:39:00.000-08"), is(OffsetDateTime.parse("2016-01-26T10:39:00.000-08:00")));
    }

    private Temporal parseDateTime(String input) {
        Instant inputInstant = Objects.requireNonNull(DateUtils.parseDateTime(input)).toInstant();

        String timePart = input.substring(11);
        if (timePart.contains("+") || timePart.contains("-")) {
            // The input declares some positive or negative time offset
            int beginOfOffsetPart = timePart.contains("+") ? timePart.indexOf("+") : timePart.indexOf("-");
            String offsetPart = timePart.substring(beginOfOffsetPart);
            // The input declares some positive or negative time offset
            String offset = offsetPart.length() == 3 ? offsetPart + ":00" : offsetPart;
            return OffsetDateTime.ofInstant(inputInstant, ZoneId.of(offset));
        }

        if (input.endsWith("Z"))
            // The input time is at UTC
            return OffsetDateTime.ofInstant(inputInstant, ZoneId.of("Z"));

        // No time offset declared. Return a LocalTime
        return LocalDateTime.ofInstant(inputInstant, ZoneId.systemDefault());
    }

    private void testTime(String in, long test) {
        Date d = DateUtils.parseTime(in);

        long value = d.getTime();

        assertEquals("Fail: " + in + "(" + TimeZone.getDefault().getDisplayName() + ")", test, value);
    }

    @Test
    public void testParity() {

        testCycle(new Date(1300139579000L));
        testCycle(new Date(0));

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        testCycle(new Date(1300139579000L));
        testCycle(new Date(0));

        TimeZone offsetTwoHours = TimeZone.getTimeZone("GMT+02");

        TimeZone.setDefault(offsetTwoHours);

        testCycle(new Date(1300139579000L));
        testCycle(new Date(0));


        TimeZone offTwoHalf = TimeZone.getTimeZone("GMT+0230");

        TimeZone.setDefault(offTwoHalf);

        testCycle(new Date(1300139579000L));
        testCycle(new Date(0));

        TimeZone offMinTwoHalf = TimeZone.getTimeZone("GMT-0230");

        TimeZone.setDefault(offMinTwoHalf);

        testCycle(new Date(1300139579000L));
        testCycle(new Date(0));


    }

    @Test
    @Ignore
    // This test doesn't make sense:
    // - A time has no offset nor zone. It can only have one
    //   when bound to a date, which is not the case
    // - We're effectively binding all times to the EPOCH date
    //   (1970-01-01, UTC), which has no DST
    public void testParseTime_with_DST() {// testFormatting
        Locale.setDefault(Locale.US);

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

        String time = "12:03:05.000Z";
        testTime(time, 43385000L);

        Date date = DateUtils.parseTime(time);

        DateFormat formatter = DateFormat.getTimeInstance();
        String formatted = formatter.format(date);

        // It should shift 3 hours, 2 for the zone and 1 for DST.
        assertEquals("3:03:05 PM", formatted);
    }

    private void testCycle(Date in) {
        String formatted = DateUtils.formatDateTime(in, DateUtils.FORMAT_ISO8601);
        Date out = DateUtils.parseDateTime(formatted);
        assertEquals("Fail:", in.getTime(), out.getTime());
    }

    @Test
    public void testFormatting() {
        class LangJanSun {
            private LangJanSun(String language, String january, String sunday) {
                this.language = language;
                this.january = january;
                this.sunday = sunday;
            }

            private String language;
            private String january;
            private String sunday;
        }

        LangJanSun langJanSuns[] = new LangJanSun[]{
            new LangJanSun("en", "Jan", "Sun"),
            new LangJanSun("es", "ene", "dom"),
            new LangJanSun("fr", "janv.", "dim.")
        };

        for (LangJanSun ljs : langJanSuns) {
            Locale.setDefault(Locale.forLanguageTag(ljs.language));

            String month = DateUtils.format(DateFields.of(2018, 1, 1, 10, 20, 30, 400), "%b");
            assertEquals(ljs.january, month);

            // 2018-04-01 was sunday
            String day = DateUtils.format(DateFields.of(2018, 4, 1, 10, 20, 30, 400), "%a");
            assertEquals(ljs.sunday, day);
        }
    }
}
