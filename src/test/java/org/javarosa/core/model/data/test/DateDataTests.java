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


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
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

import java.time.Instant;
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
    public void invalidDateInInstance_isBlank() {
        XFormsElement form = html(
            head(
                title("Invalid date"),
                model(
                    mainInstance(t("data id=\"invalid-date\"",
                        t("date", "2026-02-30"),
                        t("reference")
                    )),
                    bind("/data/date").type("date"),
                    bind("/data/reference").type("date").calculate("/data/date")
                )
            ),
            body(input("/data/date"))
        );

        Scenario scenario = Scenario.init(form);
        assertThat(scenario.answerOf("/data/date"), is(nullValue()));
        assertThat(scenario.answerOf("/data/reference"), is(nullValue()));
    }

    @Test
    public void invalidDateCalculation_isBlank() {
        XFormsElement form = html(
            head(
                title("Invalid date calculation"),
                model(
                    mainInstance(t("data id=\"invalid-date-calculation\"",
                        t("date")
                    )),
                    bind("/data/date").type("date").calculate("'2026-02-30'")
                )
            ),
            body(input("/data/date"))
        );

        Scenario scenario = Scenario.init(form);
        assertThat(scenario.answerOf("/data/date"), is(nullValue()));
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
                            t("twoWeeksLater"),
                            t("fractionalAddition"),
                            t("fractionalSubtraction")
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
            Scenario scenario = Scenario.init(form);

            assertThat(scenario.answerOf("/data/nextDay").uncast().getString(), is(nextDay));
            assertThat(scenario.answerOf("/data/previousDay").uncast().getString(), is(previousDay));
            assertThat(scenario.answerOf("/data/twoWeeksLater").uncast().getString(), is(twoWeeksLater));
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
            {"Asia/Tokyo", 20650.0, 2026, 7, 16},
            {"UTC", -1.0, 1969, 12, 31},
            {"America/Los_Angeles", -1.0, 1969, 12, 31},
            {"Asia/Tokyo", -365.0, 1969, 1, 1}
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
            {2026, 12, 31},
            {1969, 12, 31}
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

    @Test
    public void dateArithmetic_beforeEpoch_truncatesFractionalResults() {
        XFormsElement form = html(
            head(
                title("Fractional date arithmetic before epoch"),
                model(
                    mainInstance(
                        t("data id=\"date-arithmetic\"",
                            t("date", "1958-07-01"),
                            t("add11"),
                            t("add19"),
                            t("subtract11"),
                            t("subtract19")
                        )
                    ),
                    bind("/data/date").type("date"),
                    bind("/data/add11").type("date").calculate("/data/date + 1.1"),
                    bind("/data/add19").type("date").calculate("/data/date + 1.9"),
                    bind("/data/subtract11").type("date").calculate("/data/date - 1.1"),
                    bind("/data/subtract19").type("date").calculate("/data/date - 1.9")
                )
            ),
            body(input("/data/date"))
        );

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Scenario scenario = Scenario.init(form);

            assertThat(scenario.answerOf("/data/add11").uncast().getString(), is("1958-07-03"));
            assertThat(scenario.answerOf("/data/add19").uncast().getString(), is("1958-07-03"));
            assertThat(scenario.answerOf("/data/subtract11").uncast().getString(), is("1958-06-30"));
            assertThat(scenario.answerOf("/data/subtract19").uncast().getString(), is("1958-06-30"));
        });
    }

    @Test
    public void dateArithmetic_afterEpoch_truncatesFractionalResults() {
        XFormsElement form = html(
            head(
                title("Fractional date arithmetic after epoch"),
                model(
                    mainInstance(
                        t("data id=\"date-arithmetic\"",
                            t("date", "2026-07-15"),
                            t("add11"),
                            t("add19"),
                            t("subtract11"),
                            t("subtract19")
                        )
                    ),
                    bind("/data/date").type("date"),
                    bind("/data/add11").type("date").calculate("/data/date + 1.1"),
                    bind("/data/add19").type("date").calculate("/data/date + 1.9"),
                    bind("/data/subtract11").type("date").calculate("/data/date - 1.1"),
                    bind("/data/subtract19").type("date").calculate("/data/date - 1.9")
                )
            ),
            body(input("/data/date"))
        );

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Scenario scenario = Scenario.init(form);

            assertThat(scenario.answerOf("/data/add11").uncast().getString(), is("2026-07-16"));
            assertThat(scenario.answerOf("/data/add19").uncast().getString(), is("2026-07-16"));
            assertThat(scenario.answerOf("/data/subtract11").uncast().getString(), is("2026-07-13"));
            assertThat(scenario.answerOf("/data/subtract19").uncast().getString(), is("2026-07-13"));
        });
    }

    @Test
    public void dateArithmetic_whenInstantFallsOnSameLocalDate_preservesDateType() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Scenario scenario;
            scenario = Scenario.init(html(
                head(title("Date arithmetic"),
                    model(
                        mainInstance(XFormsElement.t("data id=\"date-arithmetic\"",
                            t("date"),
                            t("nextDate")
                        )),
                        bind("/data/date").type("date"),
                        bind("/data/nextDate").type("date").calculate("/data/date + 1")
                    )
                ),
                body(
                    input("/data/date")
                )
            ));

            scenario.answer("/data/date", Instant.parse("2021-11-30T12:00:00Z"));

            assertThat(scenario.answerOf("/data/nextDate"), instanceOf(DateData.class));
            assertThat(scenario.answerOf("/data/nextDate").uncast().getValue(), is("2021-12-01"));
        });
    }

    @Test
    public void dateArithmetic_whenInstantFallsOnNextLocalDate_preservesDateType() {
        withTimeZone(TimeZone.getTimeZone("Pacific/Auckland"), () -> {
            Scenario scenario;
            scenario = Scenario.init(html(
                head(title("Date arithmetic"),
                    model(
                        mainInstance(XFormsElement.t("data id=\"date-arithmetic\"",
                            t("date"),
                            t("nextDate")
                        )),
                        bind("/data/date").type("date"),
                        bind("/data/nextDate").type("date").calculate("/data/date + 1")
                    )
                ),
                body(
                    input("/data/date")
                )
            ));

            // In UTC+13, it's 2021-12-01 already
            scenario.answer("/data/date", Instant.parse("2021-11-30T12:00:00Z"));

            assertThat(scenario.answerOf("/data/nextDate"), instanceOf(DateData.class));
            assertThat(scenario.answerOf("/data/nextDate").uncast().getValue(), is("2021-12-02"));
        });
    }

    @Test
    public void dateCalculation_stringResult_isConvertedToDateData() {
        XFormsElement form = html(
            head(
                title("Date calculation"),
                model(
                    mainInstance(t("data id=\"date-calculation\"",
                        t("date")
                    )),
                    bind("/data/date").type("date").calculate("'2026-02-02'")
                )
            ),
            body(input("/data/date"))
        );

        Scenario scenario = Scenario.init(form);

        assertThat(scenario.answerOf("/data/date"), instanceOf(DateData.class));
        assertThat(scenario.answerOf("/data/date").uncast().getValue(), is("2026-02-02"));
    }

}
