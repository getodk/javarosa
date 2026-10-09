package org.javarosa.core.model.utils.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;

import java.util.Date;
import java.util.TimeZone;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;

public class DateUtilsTimeZoneConsistencyTests {

    @Test
    public void parseDate_andDaysSinceEpoch_useConsistentTimeZones() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Date date = DateUtils.getDateFromString("1970-01-02");

            assertThat(DateUtils.daysSinceEpoch(date), is(1));
        });

        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () -> {
            Date date = DateUtils.getDateFromString("1970-01-02");

            assertThat(DateUtils.daysSinceEpoch(date), is(1));
        });

        withTimeZone(TimeZone.getTimeZone("GMT+08:00"), () -> {
            Date date = DateUtils.parseDate("1970-01-02");

            assertThat(DateUtils.daysSinceEpoch(date), is(1));
        });
    }

    @Test
    public void parseDateTime_andFractionalDaysSinceEpoch_useConsistentTimeZones() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Date date = DateUtils.getDateTimeFromString("1970-01-02T06:00:00");

            assertThat(DateUtils.fractionalDaysSinceEpoch(date), is(1.25));
        });

        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () -> {
            Date date = DateUtils.getDateTimeFromString("1970-01-02T06:00:00");

            assertThat(DateUtils.fractionalDaysSinceEpoch(date), is(1.25));
        });

        withTimeZone(TimeZone.getTimeZone("GMT+08:00"), () -> {
            Date date = DateUtils.parseDateTime("1970-01-02T06:00:00");

            assertThat(DateUtils.fractionalDaysSinceEpoch(date), is(1.25));
        });
    }

    @Test
    public void parseDate_andDaysSinceEpoch_handleDatesBeforeEpoch() {
        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Date date = DateUtils.parseDate("1969-12-31");

            assertThat(DateUtils.daysSinceEpoch(date), is(-1));
        });

        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () -> {
            Date date = DateUtils.parseDate("1969-12-31");

            assertThat(DateUtils.daysSinceEpoch(date), is(-1));
        });

        withTimeZone(TimeZone.getTimeZone("GMT+08:00"), () -> {
            Date date = DateUtils.parseDate("1969-12-31");

            assertThat(DateUtils.daysSinceEpoch(date), is(-1));
        });
    }

}