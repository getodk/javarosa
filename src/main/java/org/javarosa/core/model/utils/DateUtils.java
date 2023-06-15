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

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    @NotNull
    public static Date dateFromLocalDate(LocalDate someDate) {
        return Date.from(someDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate localDateFromDate(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static java.time.LocalDateTime localDateTimeFromDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date dateFromLocalDate(java.time.LocalDateTime someDateTime) {
        return Date.from(someDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static final long DAY_IN_MS = 86400000L;

    public DateUtils() {
    }

    public static Date getDate(DateFields f) {
        return getDate(f, TimeZone.getDefault());
    }

    public static Date getDate(DateFields f, TimeZone tz) {
        return getLocalDateTime(f).toDate(tz);
    }

    public static org.joda.time.LocalDateTime getLocalDateTime(DateFields f) {
        return new org.joda.time.LocalDateTime(f.year, f.month, f.day, f.hour, f.minute, f.second, f.secTicks);
    }

    //Convert {@link org.joda.time.DateTime} to {@link java.time.LocalDateTime}
    static java.time.LocalDateTime toJavaTimeLocalDateTime(DateTime dateTime) {
        int millisOfSecond = dateTime.getMillisOfSecond();
        int nanoseconds = Math.toIntExact(TimeUnit.NANOSECONDS.convert(millisOfSecond, TimeUnit.MILLISECONDS));
        System.out.println(nanoseconds);
        return java.time.LocalDateTime.of(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute(),
                nanoseconds);
    }

    /* ==== PARSING DATES/TIMES FROM STANDARD STRINGS ==== */

    public static Date parseDateTime(String str) {
        DateFields fields = new DateFields();
        int i = str.indexOf("T");
        if (i != -1) {
            if (stringDoesntHaveDateFields(str.substring(0, i)) || !parseTime(str.substring(i + 1), fields)) {
                return null;
            } else {
                String dateStr = str.substring(0, i);
                List<String> pieces = split(dateStr, "-", false);
                if (pieces.size() != 3)
                    throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateStr);

                DateFields newDate = DateFields.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
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
        List<String> pieces = split(str, "-", false);
        if (pieces.size() != 3) throw new IllegalArgumentException("Wrong number of fields to parse date: " + str);

        return getDate(DateFields.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2))));
    }

    public static Date parseTime(String str) {
        Date d = new Date();
        DateFields fields = DateFields.getFields(d, TimeZone.getDefault());
        fields.second = 0;
        fields.secTicks = 0;
        if (!parseTime(str, fields)) {
            return null;
        }
        return getDate(fields);
    }


    private static boolean stringDoesntHaveDateFields(String dateStr) {
        try {
            List<String> pieces = split(dateStr, "-", false);
            if (pieces.size() != 3)
                throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateStr);

            DateFields.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean parseTime(String timeStr, DateFields f) {
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
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(utc);
        long msecOffset = (((60L * timeOffset.hour) + timeOffset.minute) * 60 * 1000L);
        c.setTime(new Date(DateUtils.getDate(f, utc).getTime() + msecOffset));
        //c is now in the timezone of the parsed value, so put
        //it in the local timezone.
        c.setTimeZone(TimeZone.getDefault());

        Date d = c.getTime();
        DateFields adjusted = DateFields.getFields(d, TimeZone.getDefault());

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
        DateFields f = DateFields.getFields(d, TimeZone.getDefault());
        return getDate(DateFields.of(f.year, f.month, f.day));
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
     * @param includeToday Whether today's date can count as the last day of the period
     * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
     * @return a Date object representing the amount of time between the
     * reference date, and the given parameters.
     */
    public static Date getPastPeriodDate(Date ref, String type, String start, boolean beginning, boolean includeToday, int nAgo) {
        if (type.equals("week")) {
            Calendar cd = Calendar.getInstance();
            cd.setTime(ref);
            int current_dow = cd.get(Calendar.DAY_OF_WEEK) - 1;
            int target_dow = DOW.valueOf(start).order;
            int offset = (includeToday ? 1 : 0);
            int diff = ((current_dow - target_dow + 7 + offset) % 7 - offset)
                    + (7 * nAgo)
                    - (beginning ? 0 : 6); //booyah
            return new Date(ref.getTime() - diff * DAY_IN_MS);
        } else if (type.equals("month")) {
            //not supported
            return null;
        } else {
            throw new IllegalArgumentException();
        }
    }

    //convenience, should go away soon
    private enum DOW {
        sun(0), mon(1), tue(2), wed(3), thu(4), fri(5), sat(6);
        final int order;
        DOW(int ordinal) {
            this.order = ordinal;
        }
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
