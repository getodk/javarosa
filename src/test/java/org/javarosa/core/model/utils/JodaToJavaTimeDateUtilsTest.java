package org.javarosa.core.model.utils;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class JodaToJavaTimeDateUtilsTest {
    @Test
    public void nowWorksSameInJodaAndJavaTime() {
        Date usingJava = DateUtils.now();
        Date usingJoda = new org.joda.time.DateTime().toDate();
        assertThat(((double) usingJava.getTime()), closeTo((double) usingJoda.getTime(), 10));
    }

//        @Test
//    public void jodaToJavaTest() {
//        Date now = new Date(Instant.now().toEpochMilli());
//        LocalDateTime javaDateTime = DateUtils.localDateTimeFromDate(now);
//        org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime(now);
//        assertEquals(javaDateTime, DateUtils.toJavaTimeLocalDateTime(jodaDateTime));
//    }

    //Convert {@link org.joda.time.DateTime} to {@link java.time.LocalDateTime}
//    static LocalDateTime toJavaTimeLocalDateTime(org.joda.time.DateTime dateTime) {
//        return LocalDateTime.of(
//                dateTime.getYear(),
//                dateTime.getMonthOfYear(),
//                dateTime.getDayOfMonth(),
//                dateTime.getHourOfDay(),
//                dateTime.getMinuteOfHour(),
//                dateTime.getSecondOfMinute(),
//                secTicksAsNanoSeconds(dateTime.getMillisOfSecond()));
//    }

}
