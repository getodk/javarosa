package org.javarosa.core.model.data.test;

import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 This test is intended to show the limitation of {@link TimeData}.

 Using this data type in countries which change their time (summer/winter - DST) will cause that forms saved during
 wintertime and then edited during summertime (and vice versa) will be treated as saved in a neighbor's timezone.
 It's because we have just time and time offset like: 10:00:00.000+02:00 but we don't know when the form has been
 saved so we parse it using the current date.

 Example:
 If we saved 10:00:00.000+02:00 during summertime (in Poland) and we are editing the form during winter time our
 timezone is +01:00 not +02:00. As mentioned above javarosa doesn't know that the form has been saved in the same location
 but different timezone because of DST so it treats the value like saved in the neighbor timezone
 (in Kiev or London for example).

 Related issues:
 https://github.com/opendatakit/javarosa/pull/478
 https://github.com/opendatakit/collect/issues/170
 */
public class TimeDataLimitationsTest {

    private TimeZone backupTimeZone;

    @Before    public void setUp() {
        backupTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(backupTimeZone);
    }

    @Test
    public void editingFormsSavedInDifferentTimezoneTest() {
        // A user is in Warsaw (GMT+2) saved a form with the time question
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));

        boolean isSummerTime = TimeZone.getDefault().inDaylightTime(new Date());
        String savedTime = isSummerTime ? "10:00:00.000+02:00" : "10:00:00.000+01:00";

        // A user opens saved form in Warsaw as well - the hour should be the same
        TimeData timeData = new TimeData(DateUtils.parseTime(savedTime));
        assertEquals("10:00", timeData.getDisplayText());

        // A user travels to Kiev (GMT+3) and opens the saved form again - the hour should be edited +1h
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));
        timeData = new TimeData(DateUtils.parseTime(savedTime));
        assertEquals("11:00", timeData.getDisplayText());
    }

    @Test
    public void editingFormsSavedInTheSameLocationButAfterDSTChangeTest() {
        DateUtils.DateFields dateFields = new DateUtils.DateFields();
        dateFields.year = 2019;
        dateFields.month = 8;
        dateFields.day = 1;

        // A user is in Warsaw (during summer time - GMT+2) and saved a form with the time question
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));
        String savedTime = "10:00:00.000+02:00";

        // A user opens saved form in Warsaw and during summertime as well - the hour should be the same
        TimeData timeData = new TimeData(DateUtils.parseTimeWithFixedDate(savedTime, dateFields));
        assertEquals("10:00", timeData.getDisplayText());

        // A user opens saved form in Warsaw as well but during wintertime - the hour is edited -1h (the mentioned limitation)
        dateFields.month = 12;
        timeData = new TimeData(DateUtils.parseTimeWithFixedDate(savedTime, dateFields));
        assertEquals("09:00", timeData.getDisplayText());
    }
}
