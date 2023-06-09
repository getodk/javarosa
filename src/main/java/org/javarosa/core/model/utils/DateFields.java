package org.javarosa.core.model.utils;

import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class DateFields {
    public static DateFields of(int year, int month, int day, int hour, int minute, int second, int secTicks) {
        // The official API returns an ISO 8601 day of week
        // with a range of values from 1 for Monday to 7 for Sunday].
        // TODO migrate dow field to a DayOfWeek type to avoid any possible
        // interpretation errors
        LocalDateTime ldt = new LocalDateTime(year, month, day, hour, minute, second, secTicks);
        int iso8601Dow = ldt.getDayOfWeek();
        int dow = iso8601Dow == 7 ? 0 : iso8601Dow;
        int week = ldt.getWeekOfWeekyear();
        return new DateFields(year, month, day, hour, minute, second, secTicks, dow, week);
    }

    public static DateFields of(int year, int month, int day) {
        // The official API returns an ISO 8601 day of week
        // with a range of values from 1 for Monday to 7 for Sunday].
        // TODO migrate dow field to a DayOfWeek type to avoid any possible
        // interpretation errors
        return DateFields.of(year, month, day, 0, 0, 0, 0);
    }

    public static DateFields getFields(Date d, String timezone) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(d);
        if (timezone != null) {
            cd.setTimeZone(TimeZone.getTimeZone(timezone));
        }

        return new DateFields(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + DateUtils.MONTH_OFFSET, cd.get(Calendar.DAY_OF_MONTH), cd.get(Calendar.HOUR_OF_DAY), cd.get(Calendar.MINUTE), cd.get(Calendar.SECOND), cd.get(Calendar.MILLISECOND), cd.get(Calendar.DAY_OF_WEEK), cd.get(Calendar.WEEK_OF_YEAR));
    }

    public int year;
    public int month; //1-12
    public int day; //1-31
    public int hour; //0-23
    public int minute; //0-59
    public int second; //0-59
    public int secTicks; //0-999 (ms)
    public int week; // 1-53
    public int dow; //1-7;

    public DateFields() {
        year = 1970;
        month = 1;
        day = 1;
        hour = 0;
        minute = 0;
        second = 0;
        secTicks = 0;

        LocalDateTime ldt = new LocalDateTime(year, month, day, hour, minute, second, secTicks);
        int iso8601Dow = ldt.getDayOfWeek();
        dow = iso8601Dow == 7 ? 0 : iso8601Dow;
        week = ldt.getWeekOfWeekyear();
        if (!check()) throw new IllegalArgumentException("Fields = " + this);
    }

    private DateFields(int year, int month, int day, int hour, int minute, int second, int secTicks, int dow, int week) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.secTicks = secTicks;
        this.dow = dow;
        this.week = week;
    }

    public boolean check() {
        return (inRange(month, 1, 12) && inRange(day, 1, DateUtils.daysInMonth(month - DateUtils.MONTH_OFFSET, year)) && inRange(hour, 0, 23) && inRange(minute, 0, 59) && inRange(second, 0, 59) && inRange(secTicks, 0, 999) && inRange(week, 1, 53));
    }

    private static boolean inRange(int x, int min, int max) {
        return (x >= min && x <= max);
    }

    public DateFields withTime(DateFields fields) {
        return of(this.year, this.month, this.day, fields.hour, fields.minute, fields.second, fields.secTicks);
    }
}
