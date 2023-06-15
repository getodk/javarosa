package org.javarosa.core.model.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Locale;

import static org.javarosa.core.model.utils.DateUtils.secTicksAsNanoSeconds;


public class DateFields {
    public static final int MONTH_OFFSET = (1 - Calendar.JANUARY);

    public static DateFields of(int year, int month, int day, int hour, int minute, int second, int secTicks) {
        WeekFields weekNumbering = WeekFields.of(Locale.getDefault());
        LocalDate date = LocalDate.of(year, month, day);
        int currentWeek = date.get(weekNumbering.weekOfWeekBasedYear());

        return new DateFields(year, month, day, hour, minute, second, secTicks, date.getDayOfWeek().getValue(), currentWeek);
    }

    public static DateFields of(int year, int month, int day) {
        // The official API returns an ISO 8601 day of week
        // with a range of values from 1 for Monday to 7 for Sunday].
        // TODO migrate dow field to a DayOfWeek type to avoid any possible
        // interpretation errors
        return DateFields.of(year, month, day, 0, 0, 0, 0);
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

        WeekFields weekNumbering = WeekFields.of(Locale.getDefault());
        LocalDate date = LocalDate.of(year, month, day);
        int currentWeek = date.get(weekNumbering.weekOfWeekBasedYear());
        dow = date.getDayOfWeek().getValue();
        week = currentWeek;

        if (!check()) throw new IllegalArgumentException("Fields = " + this);
    }

    public DateFields(int year, int month, int day, int hour, int minute, int second, int secTicks, int dow, int week) {
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
        return (inRange(month, 1, 12)
                && inRange(day, 1, daysInMonth(month, year))
                && inRange(hour, 0, 23)
                && inRange(minute, 0, 59)
                && inRange(second, 0, 59)
                && inRange(secTicks, 0, 999)
                && inRange(week, 1, 53));
    }

    public LocalDateTime asLocalDateTime() {
        LocalDate localDate = LocalDate.of(year, month, day);
        LocalTime localTime = LocalTime.of(hour, minute, second, secTicksAsNanoSeconds(secTicks));
        return LocalDateTime.of(localDate, localTime);
    }

    private int daysInMonth(int month, int year) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    private static boolean inRange(int x, int min, int max) {
        return x >= min && x <= max;
    }

}
