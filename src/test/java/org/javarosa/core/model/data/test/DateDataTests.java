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

package org.javarosa.core.model.data.test;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.javarosa.test.BindBuilderXFormsElement.bind;
import static org.javarosa.test.XFormsElement.body;
import static org.javarosa.test.XFormsElement.head;
import static org.javarosa.test.XFormsElement.html;
import static org.javarosa.test.XFormsElement.input;
import static org.javarosa.test.XFormsElement.mainInstance;
import static org.javarosa.test.XFormsElement.model;
import static org.javarosa.test.XFormsElement.t;
import static org.javarosa.test.XFormsElement.title;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.test.Scenario;
import org.javarosa.test.XFormsElement;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.junit.Before;
import org.junit.Test;

public class DateDataTests {

    Date today;
    Date notToday;


    @Before
    public void setUp() throws Exception {

        today = DateUtils.roundDate(new Date());
        notToday = DateUtils.roundDate(new Date(today.getTime() - today.getTime()/2));
    }


    @Test
    public void testGetData() {
        DateData data = new DateData(today);
        assertEquals("DateData's getValue returned an incorrect date", data.getValue(), today);
        Date temp = new Date(today.getTime());
        today.setTime(1234);
        assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);

        Date rep = (Date)data.getValue();
        rep.setTime(rep.getTime() - 1000);

        assertEquals("DateData's getValue was mutated incorrectly", data.getValue(), temp);
    }
    @Test
    public void testSetData() {
        DateData data = new DateData(notToday);
        data.setValue(today);

        assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(notToday)));
        assertEquals("DateData did not properly set value ", data.getValue(), today);

        data.setValue(notToday);
        assertTrue("DateData did not set value properly. Maintained old value.", !(data.getValue().equals(today)));
        assertEquals("DateData did not properly reset value ", data.getValue(), notToday);

        Date temp = new Date(notToday.getTime());
        notToday.setTime(notToday.getTime() - 1324);

        assertEquals("DateData's value was mutated incorrectly", data.getValue(), temp);
    }

    @Test
    public void testDisplay() {
        // We don't actually want this, because the Date's getDisplayText code should be moved to a library
    }

    @Test
    public void testNullData() {
        boolean exceptionThrown = false;
        DateData data = new DateData();
        data.setValue(today);
        try {
            data.setValue(null);
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        assertTrue("DateData failed to throw an exception when setting null data", exceptionThrown);
        assertTrue("DateData overwrote existing value on incorrect input", data.getValue().equals(today));
    }

    @Test
    public void dateArithmetic_inWarsawSummerTime_returnsCorrectDates() {
        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-07-15",
            "2026-07-16",
            "2026-07-14",
            "2026-07-29"
        );
    }

    @Test
    public void dateArithmetic_inWarsawWinterTime_returnsCorrectDates() {
        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-01-15",
            "2026-01-16",
            "2026-01-14",
            "2026-01-29"
        );
    }

    @Test
    public void dateArithmetic_acrossStartOfDaylightSavingTime_returnsCorrectDates() {
        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-03-28",
            "2026-03-29",
            "2026-03-27",
            "2026-04-11"
        );

        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-03-29",
            "2026-03-30",
            "2026-03-28",
            "2026-04-12"
        );
    }

    @Test
    public void dateArithmetic_acrossEndOfDaylightSavingTime_returnsCorrectDates() {
        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-10-24",
            "2026-10-25",
            "2026-10-23",
            "2026-11-07"
        );

        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-10-25",
            "2026-10-26",
            "2026-10-24",
            "2026-11-08"
        );
    }

    @Test
    public void dateArithmetic_inLosAngelesSummerTime_returnsCorrectDates() {
        assertDateArithmetic(
            "America/Los_Angeles",
            "2026-07-15",
            "2026-07-16",
            "2026-07-14",
            "2026-07-29"
        );
    }

    @Test
    public void dateArithmetic_inUtc_returnsCorrectDates() {
        assertDateArithmetic(
            "UTC",
            "2026-07-15",
            "2026-07-16",
            "2026-07-14",
            "2026-07-29"
        );
    }

    @Test
    public void dateArithmetic_inTokyo_returnsCorrectDates() {
        assertDateArithmetic(
            "Asia/Tokyo",
            "2026-07-15",
            "2026-07-16",
            "2026-07-14",
            "2026-07-29"
        );
    }

    @Test
    public void dateArithmetic_acrossYearBoundary_returnsCorrectDates() {
        assertDateArithmetic(
            "Europe/Warsaw",
            "2026-12-31",
            "2027-01-01",
            "2026-12-30",
            "2027-01-14"
        );
    }

    private void assertDateArithmetic(String timezone, String date, String nextDay, String previousDay, String twoWeeksLater) {
        XFormsElement form = html(
            head(
                title("Date arithmetic"),
                model(
                    mainInstance(
                        t("data id=\"date-arithmetic\"",
                            t("date", date),
                            t("nextDay"),
                            t("previousDay"),
                            t("twoWeeksLater")
                        )
                    ),
                    bind("/data/date").type("date"),
                    bind("/data/nextDay").type("date").calculate("/data/date + 1"),
                    bind("/data/previousDay").type("date").calculate("/data/date - 1"),
                    bind("/data/twoWeeksLater").type("date").calculate("/data/date + 14")
                )
            ),
            body(input("/data/date"))
        );

        withTimeZone(TimeZone.getTimeZone(timezone), () -> {
            try {
                Scenario scenario = Scenario.init(form);

                assertThat(scenario.answerOf("/data/nextDay").uncast().getString(), is(nextDay));
                assertThat(scenario.answerOf("/data/previousDay").uncast().getString(), is(previousDay));
                assertThat(scenario.answerOf("/data/twoWeeksLater").uncast().getString(), is(twoWeeksLater));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void toDate_numericDays_returnsCorrectLocalDateAcrossTimezones() {
        Object[][] cases = {
            // timezone, numeric day, expected year, month, day
            {"UTC", 20650.0, 2026, 7, 16},
            {"Europe/Warsaw", 20650.0, 2026, 7, 16},
            {"Europe/Warsaw", 20469.0, 2026, 1, 16},
            {"Europe/Warsaw", 20751.0, 2026, 10, 25},
            {"America/Los_Angeles", 20650.0, 2026, 7, 16},
            {"Asia/Tokyo", 20650.0, 2026, 7, 16}
        };

        for (Object[] testCase : cases) {
            String timezone = (String) testCase[0];
            double days = (Double) testCase[1];
            int year = (Integer) testCase[2];
            int month = (Integer) testCase[3];
            int day = (Integer) testCase[4];

            withTimeZone(TimeZone.getTimeZone(timezone), () -> {
                Date date = (Date) XPathFuncExpr.toDate(days, false);
                Date expected = DateUtils.getDate(year, month, day);

                assertEquals(timezone + " with preserveTime=false", expected, date);
            });
        }
    }

    @Test
    public void toDate_preserveTimeTrue_usesUtcEpochMilliseconds() {
        double days = 20650.0; // July 16, 2026 midnight UTC

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Date date = (Date) XPathFuncExpr.toDate(days, true);

            assertThat(date.getTime(), is((long) (days * DateUtils.DAY_IN_MS)));
            assertThat(DateUtils.roundDate(date), is(DateUtils.getDate(2026, 7, 16)));
        });

        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            Date date = (Date) XPathFuncExpr.toDate(days, true);

            assertThat(date.getTime(), is((long) (days * DateUtils.DAY_IN_MS)));
            // July 15, 2026, at 17:00 in LA.
            assertThat(DateUtils.roundDate(date), is(DateUtils.getDate(2026, 7, 15)));
        });
    }

    @Test
    public void toDate_numericDate_roundTripsAcrossTimezones() {
        String[] timezones = {
            "UTC",
            "Europe/Warsaw",
            "America/Los_Angeles",
            "Asia/Tokyo"
        };

        int[][] dates = {
            {2026, 1, 15},
            {2026, 3, 28},
            {2026, 3, 29},
            {2026, 7, 15},
            {2026, 10, 24},
            {2026, 10, 25},
            {2026, 12, 31}
        };

        for (String timezone : timezones) {
            withTimeZone(TimeZone.getTimeZone(timezone), () -> {
                for (int[] fields : dates) {
                    Date original = DateUtils.getDate(fields[0], fields[1], fields[2]);
                    double numeric = LocalDate.of(fields[0], fields[1], fields[2]).toEpochDay();
                    Date restored = (Date) XPathFuncExpr.toDate(numeric, false);

                    assertEquals(timezone + " " + fields[0] + "-" + fields[1] + "-" + fields[2], original, restored);
                }
            });
        }
    }
}
