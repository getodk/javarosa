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

package org.javarosa.core.model.utils;

import org.javarosa.core.util.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateUtils {
    @NotNull
    public static Date dateFromLocalDate(LocalDate someDate) {
        return Date.from(someDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
    }

    public static LocalDate localDateFromDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }


    public static final long DAY_IN_MS = 86400000L;

    public DateUtils() {
        super();
    }

    public static DateFields getFields(Date d) {
        return DateFields.getFields(d, null);
    }

    public static Date getDate(DateFields f) {
        return getDate(f, null);
    }

    public static Date getDate(DateFields f, String timezone) {
        TimeZone tz = (timezone == null) ? TimeZone.getDefault() : TimeZone.getTimeZone(timezone);
        return getLocalDateTime(f).toDate(tz);
    }

    public static LocalDateTime getLocalDateTime(DateFields f) {
        return new LocalDateTime(f.year, f.month, f.day, f.hour, f.minute, f.second, f.secTicks);
    }

    /* ==== PARSING DATES/TIMES FROM STANDARD STRINGS ==== */

    public static Date parseDateTime(String str) {
        DateFields fields = new DateFields();
        int i = str.indexOf("T");
        if (i != -1) {
            if (stringDoesntHaveDateFields(str.substring(0, i)) || !parseTime(str.substring(i + 1), fields)) {
                return null;
            } else {
                DateFields newDate = dateFieldsFromString(str.substring(0, i));
                parseTime(str.substring(i + 1), newDate);
                return getDate(newDate);
            }
        } else {
            return parseDate(str);
        }
    }

    public static Date parseDate(String str) {
        if (stringDoesntHaveDateFields(str)) {
            throw new IllegalArgumentException("Fields = " + str);
        }
        return getDate(dateFieldsFromString(str));
    }

    public static Date parseTime(String str) {
        DateFields fields = getFields(new Date());
        fields.second = 0;
        fields.secTicks = 0;
        if (!parseTime(str, fields)) {
            return null;
        }
        return getDate(fields);
    }


    public static Date parseTimeWithFixedDate(String str, DateFields fields) {
        if (!parseTime(str, fields)) {
            return null;
        }
        return getDate(fields);
    }

    private static boolean stringDoesntHaveDateFields(String dateStr) {
        try {
            dateFieldsFromString(dateStr);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static DateFields dateFieldsFromString(String dateStr) {
        List<String> pieces = split(dateStr, "-", false);
        if (pieces.size() != 3) throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateStr);

        return DateFields.of(
                Integer.parseInt(pieces.get(0)),
                Integer.parseInt(pieces.get(1)),
                Integer.parseInt(pieces.get(2))
        );
    }

    private static boolean parseTime(String timeStr, DateFields f) {
        //get timezone information first. Make a Datefields set for the possible offset
        //NOTE: DO NOT DO DIRECT COMPUTATIONS AGAINST THIS. It's a holder for hour/minute
        //data only, but has data in other fields
        DateFields timeOffset = null;

        if (timeStr.charAt(timeStr.length() - 1) == 'Z') {
            //UTC!

            //Clean up string for later processing
            timeStr = timeStr.substring(0, timeStr.length() - 1);
            timeOffset = new DateFields();
        } else if (timeStr.contains("+") || timeStr.contains("-")) {
            timeOffset = new DateFields();

            List<String> pieces = split(timeStr, "+", false);

            //We're going to add the Offset straight up to get UTC
            //so we need to invert the sign on the offset string
            int offsetSign = -1;

            if (pieces.size() <= 1) {
                pieces = split(timeStr, "-", false);
                offsetSign = 1;
            }

            timeStr = pieces.get(0);

            String offset = pieces.get(1);
            String hours = offset;
            if (offset.contains(":")) {
                List<String> tzPieces = split(offset, ":", false);
                hours = tzPieces.get(0);
                int mins = Integer.parseInt(tzPieces.get(1));
                timeOffset.minute = mins * offsetSign;
            }
            timeOffset.hour = Integer.parseInt(hours) * offsetSign;
        }

        //Do the actual parse for the real time values;
        if (!parseRawTime(timeStr, f)) {
            return false;
        }

        if (!(f.check())) {
            return false;
        }

        //Time is good, if there was no timezone info, just return that;
        if (timeOffset == null) {
            return true;
        }

        //Now apply any relevant offsets from the timezone.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        long msecOffset = (((60L * timeOffset.hour) + timeOffset.minute) * 60 * 1000L);
        c.setTime(new Date(DateUtils.getDate(f, "UTC").getTime() + msecOffset));

        //c is now in the timezone of the parsed value, so put
        //it in the local timezone.

        c.setTimeZone(TimeZone.getDefault());

        DateFields adjusted = getFields(c.getTime());

        // time zone adjustment may +/- across midnight
        // which can result in +/- across a year
        f.year = adjusted.year;
        f.month = adjusted.month;
        f.day = adjusted.day;
        f.dow = adjusted.dow;
        f.hour = adjusted.hour;
        f.minute = adjusted.minute;
        f.second = adjusted.second;
        f.secTicks = adjusted.secTicks;

        return f.check();
    }

    /**
     * Parse the raw components of time (hh:mm:ss) with no timezone information
     */
    private static boolean parseRawTime(String timeStr, DateFields f) {
        List<String> pieces = split(timeStr, ":", false);
        if (pieces.size() != 2 && pieces.size() != 3) return false;

        try {
            f.hour = Integer.parseInt(pieces.get(0));
            f.minute = Integer.parseInt(pieces.get(1));

            if (pieces.size() == 3) {
                String secStr = pieces.get(2);
                int i;
                for (i = 0; i < secStr.length(); i++) {
                    char c = secStr.charAt(i);
                    if (!Character.isDigit(c) && c != '.') break;
                }
                secStr = secStr.substring(0, i);

                int idxDec = secStr.indexOf('.');
                if (idxDec == -1) {
                    if (secStr.length() == 0) {
                        f.second = 0;
                    } else {
                        f.second = Integer.parseInt(secStr);
                    }
                    f.secTicks = 0;
                } else {
                    String secPart = secStr.substring(0, idxDec);
                    if (secPart.length() == 0) {
                        f.second = 0;
                    } else {
                        f.second = Integer.parseInt(secPart);
                    }
                    String secTickStr = secStr.substring(idxDec + 1);
                    if (secTickStr.length() > 0) {
                        f.secTicks = Integer.parseInt(secTickStr);
                    } else {
                        f.secTicks = 0;
                    }
                }

                double fsec = Double.parseDouble(secStr);
                f.second = (int) fsec;
                f.secTicks = (int) (1000.0 * fsec - 1000.0 * f.second);
            }
        } catch (NumberFormatException nfe) {
            return false;
        }

        return f.check();
    }


    /* ==== DATE UTILITY FUNCTIONS ==== */

    public static Date getDate(int year, int month, int day) {
        return getDate(DateFields.of(year, month, day));
    }

    /**
     * @return new Date object with same date but time set to midnight (in current timezone)
     */
    public static Date roundDate(Date d) {
        if (d == null) return null;
        DateFields f = getFields(d);
        return getDate(DateFields.of(f.year, f.month, f.day));
//        return getDate(DateFields.of(d.getYear(), d.getMonth(), d.getDay()));
    }

    public static Date today() {
        return roundDate(new Date());
    }

    /* ==== CALENDAR FUNCTIONS ==== */

    /**
     * Returns the fractional time within the local day.
     */
    public static double decimalTimeOfLocalDay(Date d) {
        long milli = d.getTime();
        // time is local time.
        // We want to obtain milliseconds from start of local day.
        // the Math.floor() function below will do milliseconds from
        // start of UTC day. Adjust back to UTC time-of-day.
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        long milliOff = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));
        milli += milliOff;
        // and now convert to fractional day.
        Double v = ((double) milli) / DAY_IN_MS;
        return v - Math.floor(v);
    }

    /**
     * Returns the number of days in the month given for
     * a given year.
     *
     * @param month The month to be tested
     * @param year  The year in which the month is to be tested
     * @return the number of days in the given month on the given
     * year.
     */
    public static int daysInMonth(int month, int year) {
        if (month == Calendar.APRIL || month == Calendar.JUNE || month == Calendar.SEPTEMBER || month == Calendar.NOVEMBER) {
            return 30;
        } else if (month == Calendar.FEBRUARY) {
            return 28 + (isLeap(year) ? 1 : 0);
        } else {
            return 31;
        }
    }

    /**
     * Determines whether a year is a leap year in the
     * proleptic Gregorian calendar.
     *
     * @param year The year to be tested
     * @return True, if the year given is a leap year,
     * false otherwise.
     */
    public static boolean isLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }


    /* ==== Parsing to Human Text ==== */

    /* ==== DATE OPERATIONS ==== */

    /**
     * Creates a Date object representing the amount of time between the
     * reference date, and the given parameters.
     *
     * @param ref          The starting reference date
     * @param type         "week", or "month", representing the time period which is to be returned.
     * @param start        "sun", "mon", ... etc. representing the start of the time period.
     * @param beginning    true=return first day of period, false=return last day of period
     * @param includeToday Whether to include the current date in the returned calculation
     * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
     * @return a Date object representing the amount of time between the
     * reference date, and the given parameters.
     */
    public static Date getPastPeriodDate(Date ref, String type, String start, boolean beginning, boolean includeToday, int nAgo) {
        // this method isnt tested yet...
        // if(true)throw new IllegalArgumentException("booyah");
        if (type.equals("week")) {
            //1 week period
            //start: day of week that starts period
            //beginning: true=return first day of period, false=return last day of period
            //includeToday: whether today's date can count as the last day of the period
            //nAgo: how many periods ago; 1=most recent period, 0=period in progress

            int target_dow = -1, current_dow, diff;
            int offset = (includeToday ? 1 : 0);

            switch (start) {
                case "sun": target_dow = 0;
                    break;
                case "mon": target_dow = 1;
                    break;
                case "tue": target_dow = 2;
                    break;
                case "wed": target_dow = 3;
                    break;
                case "thu": target_dow = 4;
                    break;
                case "fri": target_dow = 5;
                    break;
                case "sat": target_dow = 6;
                    break;
            }

            if (target_dow == -1) throw new RuntimeException();

            Calendar cd = Calendar.getInstance();
            cd.setTime(ref);

            switch (cd.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
                    current_dow = 0;
                    break;
                case Calendar.MONDAY:
                    current_dow = 1;
                    break;
                case Calendar.TUESDAY:
                    current_dow = 2;
                    break;
                case Calendar.WEDNESDAY:
                    current_dow = 3;
                    break;
                case Calendar.THURSDAY:
                    current_dow = 4;
                    break;
                case Calendar.FRIDAY:
                    current_dow = 5;
                    break;
                case Calendar.SATURDAY:
                    current_dow = 6;
                    break;
                default:
                    throw new RuntimeException(); //something is wrong
            }

            diff = (((current_dow - target_dow) + (7 + offset)) % 7 - offset) + (7 * nAgo) - (beginning ? 0 : 6); //booyah
            return new Date(ref.getTime() - diff * DAY_IN_MS);
        } else if (type.equals("month")) {
            //not supported
            return null;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param date the date object to be analyzed
     * @return The number of days (as a double precision floating point) since the Epoch
     */
    public static int daysSinceEpoch(Date date) {
        return daysBetween(getDate(1970, 1, 1), date);
    }

    public static Double fractionalDaysSinceEpoch(Date a) {
        return (a.getTime() - getDate(1970, 1, 1).getTime()) / (double) DAY_IN_MS;
    }

    public static Date dateAdd(Date d, int n) {
        return roundDate(new Date(roundDate(d).getTime() + DAY_IN_MS * n + DAY_IN_MS / 2));
        //half-day offset is needed to handle differing DST offsets!
    }

    public static int daysBetween(Date a, Date b) {
        return (int) MathUtils.divLongNotSuck(roundDate(b).getTime() - roundDate(a).getTime() + DAY_IN_MS / 2, DAY_IN_MS);
        //half-day offset is needed to handle differing DST offsets!
    }

    /* ==== UTILITY ==== */

    /**
     * Tokenizes a string based on the given delimiter string
     *
     * @param str       The string to be split
     * @param delimiter The delimiter to be used
     * @return An array of strings contained in original which were
     * seperated by the delimiter
     */
    public static List<String> split(String str, String delimiter, boolean combineMultipleDelimiters) {

        int index = str.indexOf(delimiter);
        List<String> pieces = new ArrayList<>(index + 1);
        while (index >= 0) {
            pieces.add(str.substring(0, index));
            str = str.substring(index + delimiter.length());
            index = str.indexOf(delimiter);
        }
        pieces.add(str);

        if (combineMultipleDelimiters) {
            for (int i = 0; i < pieces.size(); i++) {
                if (pieces.get(i).length() == 0) {
                    pieces.remove(i);
                    i--;
                }
            }
        }

        return pieces;
    }

}
