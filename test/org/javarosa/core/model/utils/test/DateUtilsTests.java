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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.model.utils.DateUtils.DateFields;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateUtilsTests extends TestCase {

    private static int NUM_TESTS = 8;

    Date currentTime;
    Date minusOneHour;

    public DateUtilsTests(String name){
        super(name);
        System.out.println("Running " + this.getClass().getName() + " test: " + name + "...");
    }

    public void setUp() throws Exception{
        super.setUp();

        currentTime = new Date();
        minusOneHour = new Date(new Date().getTime() - (1000*60));

    }

    public static Test suite(){

        TestSuite dateSuite = new TestSuite();

        for (int i = 1; i <= NUM_TESTS; i++) {
            final int testID = i;

            dateSuite.addTest(new DateUtilsTests(testMaster(testID)));
        }

        return dateSuite;

    }

    public static String testMaster(int testID) {


        switch (testID) {
        case 1: return "testGetXMLStringValueFormat";
        case 2: return "testSetDates";
        case 3: return "testNullDates";
        case 4: return "testTimeParses";
        case 5: return "testParity";
        case 6: return "testParseTime_with_DST";
        case 7: return "testDateTimeParses";
        case 8: return "testFormatting";
        }
        throw new IllegalStateException("Unexpected index");
    }

    /**
     * This test ensures that the Strings returned
     * by the getXMLStringValue function are in
     * the proper XML compliant format.
     */
    public void testGetXMLStringValueFormat() {
        String currentDate = DateUtils.getXMLStringValue(currentTime);
        assertEquals("The date string was not of the proper length", currentDate.length(),"YYYY-MM-DD".length());
        assertEquals("The date string does not have proper year formatting", currentDate.indexOf("-"),"YYYY-".indexOf("-"));
        try {
            Integer.parseInt(currentDate.substring(0, 4));
        } catch (NumberFormatException e) {
            fail("The Year value was not a valid integer");
        }
        try {
            Integer.parseInt(currentDate.substring(5, 7));
        } catch (NumberFormatException e) {
            fail("The Month value was not a valid integer");
        }
        try {
            Integer.parseInt(currentDate.substring(8, 10));
        } catch (NumberFormatException e) {
            fail("The Day value was not a valid integer");
        }
    }

    public void testNullDates() {
        // TODO Auto-generated method stub

    }

    public void testSetDates() {
        // TODO Auto-generated method stub

    }

    public void testDateTimeParses() {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        testDateTime("2016-04-13T16:26:00.000-07", 1460589960000L);
        testDateTime("2015-12-16T16:09:00.000-08", 1450310940000L); // wraps day!!!
        testDateTime("2015-12-16T07:09:00.000+08", 1450220940000L); // wraps day!!!

        testDateTime("2015-11-30T16:09:00.000-08", 1448928540000L); // wraps month!!!
        testDateTime("2015-11-01T07:09:00.000+08", 1446332940000L); // wraps month!!!

        testDateTime("2015-12-31T16:09:00.000-08", 1451606940000L); // wraps year!!!
        testDateTime("2015-01-01T07:09:00.000+08", 1420067340000L); // wraps year!!!

        testDateTime("2016-01-26T10:39:00.000-08", 1453833540000L);

        TimeZone.setDefault(TimeZone.getTimeZone("PST"));

        testDateTime("2016-04-13T16:26:00.000-07", 1460589960000L);
        testDateTime("2015-12-16T16:09:00.000-08", 1450310940000L); // wraps day!!!
        testDateTime("2015-12-16T07:09:00.000+08", 1450220940000L); // wraps day!!!

        testDateTime("2015-11-30T16:09:00.000-08", 1448928540000L); // wraps month!!!
        testDateTime("2015-11-01T07:09:00.000+08", 1446332940000L); // wraps month!!!

        testDateTime("2015-12-31T16:09:00.000-08", 1451606940000L); // wraps year!!!
        testDateTime("2015-01-01T07:09:00.000+08", 1420067340000L); // wraps year!!!

        testDateTime("2016-01-26T10:39:00.000-08", 1453833540000L);

        TimeZone.setDefault(TimeZone.getTimeZone("PDT"));

        testDateTime("2016-04-13T16:26:00.000-07", 1460589960000L);
        testDateTime("2015-12-16T16:09:00.000-08", 1450310940000L); // wraps day!!!
        testDateTime("2015-12-16T07:09:00.000+08", 1450220940000L); // wraps day!!!

        testDateTime("2015-11-30T16:09:00.000-08", 1448928540000L); // wraps month!!!
        testDateTime("2015-11-01T07:09:00.000+08", 1446332940000L); // wraps month!!!

        testDateTime("2015-12-31T16:09:00.000-08", 1451606940000L); // wraps year!!!
        testDateTime("2015-01-01T07:09:00.000+08", 1420067340000L); // wraps year!!!

        testDateTime("2016-01-26T10:39:00.000-08", 1453833540000L);
    }

    private void testDateTime(String in, long test) {
        try{
            Date d = DateUtils.parseDateTime(in);

            long value = d.getTime();

            assertEquals("Fail: " + in + "(" + TimeZone.getDefault().getDisplayName() + ")", test, value);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + in + e.getMessage());
        }
    }

    public void testTimeParses() {
        //This is all kind of tricky. We need to assume J2ME level compliance, so
        //dates won't every be assumed to have an intrinsic timezone, they'll be
        //assumed to be in the phone's default timezone

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        testTime("10:00", 1000*60*60*10 - getOffset());
        testTime("10:00Z", 1000*60*60*10);

        testTime("10:00+02", 1000*60*60*8);
        testTime("10:00-02", 1000*60*60*12);

        testTime("10:00+02:30", 1000*60*(60*10-150));
        testTime("10:00-02:30", 1000*60*(60*10+150));

        TimeZone offsetTwoHours = TimeZone.getTimeZone("GMT+02");

        TimeZone.setDefault(offsetTwoHours);

        testTime("10:00", 1000*60*60*10 - getOffset());
        testTime("10:00Z", 1000*60*60*10);

        testTime("10:00+02", 1000*60*60*8);
        testTime("10:00-02", 1000*60*60*12);

        testTime("10:00+02:30", 1000*60*(60*10-150));
        testTime("10:00-02:30", 1000*60*(60*10+150));

        TimeZone offsetMinusTwoHours = TimeZone.getTimeZone("GMT-02");

        TimeZone.setDefault(offsetMinusTwoHours);

        testTime("14:00", 1000*60*60*14 - getOffset());
        testTime("14:00Z", 1000*60*60*14);

        testTime("14:00+02", 1000*60*60*12);
        testTime("14:00-02", 1000*60*60*16);

        testTime("14:00+02:30", 1000*60*(60*14-150));
        testTime("14:00-02:30", 1000*60*(60*14+150));


        TimeZone offsetPlusHalf = TimeZone.getTimeZone("GMT+0230");

        TimeZone.setDefault(offsetPlusHalf);

        testTime("14:00", 1000*60*60*14 - getOffset());
        testTime("14:00Z", 1000*60*60*14);

        testTime("14:00+02", 1000*60*60*12);
        testTime("14:00-02", 1000*60*60*16);

        testTime("14:00+02:30", 1000*60*(60*14-150));
        testTime("14:00-02:30", 1000*60*(60*14+150));

        testTime("14:00+04:00", 1000*60*60*10);

        TimeZone.setDefault(null);
    }

    private void testTime(String in, long test) {
        try{
            Date d = DateUtils.parseTime(in);

            long value = d.getTime();

            assertEquals("Fail: " + in + "(" + TimeZone.getDefault().getDisplayName() + ")", test, value);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + in + e.getMessage());
        }
    }

    private long getOffset() {
        DateFields df = new DateFields();
        Date d = DateUtils.getDate(df);

        return -d.getTime();
    }

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

    public void testParseTime_with_DST() throws Exception {
        Locale.setDefault(Locale.US);

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

        String time = "12:03:05.000Z";
        testTime(time, 43385000L);

        Date date = DateUtils.parseTime(time);

        DateFormat formatter = DateFormat.getTimeInstance();
        String formatted = formatter.format(date);

        // It should shift 3 hours, 2 for the zone and 1 for DST.
        assertEquals("3:03:05 PM", formatted);

        TimeZone.setDefault(backupZone);
    }

    private void testCycle(Date in) {
        try{
            String formatted =DateUtils.formatDateTime(in, DateUtils.FORMAT_ISO8601);
            Date out = DateUtils.parseDateTime(formatted);
            assertEquals("Fail:", in.getTime(), out.getTime());

        } catch(Exception e) {
            e.printStackTrace();
            fail("Error: " + in + e.getMessage());
        }
    }

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

        LangJanSun langJanSuns[] = new LangJanSun[] {
            new LangJanSun("en", "Jan",   "Sun"),
            new LangJanSun("es", "ene",   "dom"),
            new LangJanSun("fr", "janv.", "dim.")
        };

        Locale savedLocale = Locale.getDefault();

        for (LangJanSun ljs : langJanSuns) {
            Locale.setDefault(Locale.forLanguageTag(ljs.language));

            String month = DateUtils.format(new DateTime().withMonthOfYear(DateTimeConstants.JANUARY).toDate(), "%b");
            assertEquals(ljs.january, month);

            String day = DateUtils.format(new DateTime().withDayOfWeek(DateTimeConstants.SUNDAY).toDate(), "%a");
            assertEquals(ljs.sunday, day);
        }

        Locale.setDefault(savedLocale);
    }
}
