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

    @Test
    public void getDate_andGetFields_useConsistentTimeZones() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            DateUtils.DateFields fields = DateUtils.DateFields.of(
                2024, 7, 15, 12, 30, 0, 0
            );

            Date date = DateUtils.getDate(fields);
            DateUtils.DateFields result = DateUtils.getFields(date);

            assertThat(result.year, is(2024));
            assertThat(result.month, is(7));
            assertThat(result.day, is(15));
            assertThat(result.hour, is(12));
            assertThat(result.minute, is(30));
        });
    }

    @Test
    public void dateAdd_andDateDiff_areConsistentAcrossDST() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            Date start = DateUtils.parseDate("2024-03-09");
            Date end = DateUtils.dateAdd(start, 2);

            assertThat(DateUtils.formatDate(end, DateUtils.FORMAT_ISO8601),
                is("2024-03-11"));
            assertThat(DateUtils.dateDiff(start, end), is(2));
        });
    }
}