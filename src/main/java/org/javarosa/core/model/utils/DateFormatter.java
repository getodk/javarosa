package org.javarosa.core.model.utils;

import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.Optional;

public class DateFormatter {
    public static final int FORMAT_ISO8601 = 1;
    public static final int FORMAT_HUMAN_READABLE_SHORT = 2;
    public static final int FORMAT_TIMESTAMP_SUFFIX = 7;
    /** RFC 822 **/
    public static final int FORMAT_TIMESTAMP_HTTP = 9;

    public static String formatDateTime(Date date, int format) {
        //TODO - is emptyString what we want?
        if (date == null) return "";

        Optional<DateFormat> optional = DateFormat.getByKey(format);
        if (!optional.isPresent()) {
            //TODO - is emptyString what we want?
            return "";
        }

        DateFormat dateFormat = optional.get();
        return dateFormat.formatDate(date) + dateFormat.delimiter + dateFormat.formatTime(date);
    }

    public static String formatTime(Date date, int format) {
        //TODO - is emptyString what we want?
        if (date == null) return "";

        Optional<DateFormat> optional = DateFormat.getByKey(format);
        if (!optional.isPresent()) {
            //TODO - is emptyString what we want?
            return "";
        }
        DateFormat dateFormat = optional.get();
        return dateFormat.formatTime(date);
    }

    public static String formatDate(Date date, int format) {
        //TODO - is emptyString what we want?
        if (date == null) return "";

        Optional<DateFormat> optional = DateFormat.getByKey(format);
        if (!optional.isPresent()) {
            //TODO - is emptyString what we want?
            return "";
        }
        DateFormat dateFormat = optional.get();
        return dateFormat.formatDate(date);
    }

    public static String format(Date d, String format) {
        return format(DateUtils.getFields(d), format);
    }

    public static String format(DateFields f, String format) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            if (c == '%') {
                i++;
                if (i >= format.length()) {
                    throw new RuntimeException("date format string ends with %");
                } else {
                    c = format.charAt(i);
                }

                if (c == '%') {            //literal '%'
                    sb.append("%");
                } else if (c == 'Y') {    //4-digit year
                    sb.append(intPad(f.year, 4));
                } else if (c == 'y') {    //2-digit year
                    sb.append(intPad(f.year, 4).substring(2));
                } else if (c == 'm') {    //0-padded month
                    sb.append(intPad(f.month, 2));
                } else if (c == 'n') {    //numeric month
                    sb.append(f.month);
                } else if (c == 'b') {    //short text month
                    sb.append(DateUtils.getLocalDateTime(f).toString(DateTimeFormat.forPattern("MMM")));
                } else if (c == 'd') {    //0-padded day of month
                    sb.append(intPad(f.day, 2));
                } else if (c == 'e') {    //day of month
                    sb.append(f.day);
                } else if (c == 'H') {    //0-padded hour (24-hr time)
                    sb.append(intPad(f.hour, 2));
                } else if (c == 'h') {    //hour (24-hr time)
                    sb.append(f.hour);
                } else if (c == 'M') {    //0-padded minute
                    sb.append(intPad(f.minute, 2));
                } else if (c == 'S') {    //0-padded second
                    sb.append(intPad(f.second, 2));
                } else if (c == '3') {    //0-padded millisecond ticks (000-999)
                    sb.append(intPad(f.secTicks, 3));
                } else if (c == 'a') {    //Three letter short text day
                    sb.append(DateUtils.getLocalDateTime(f).toString(DateTimeFormat.forPattern("EEE")));
                } else if (c == 'W') { // week of the year
                    sb.append(f.week);
                } else if (c == 'Z' || c == 'A' || c == 'B') {
                    throw new RuntimeException("unsupported escape in date format string [%" + c + "]");
                } else {
                    throw new RuntimeException("unrecognized escape in date format string [%" + c + "]");
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Converts an integer to a string, ensuring that the string
     * contains a certain number of digits
     *
     * @param n   The integer to be converted
     * @param pad The length of the string to be returned
     * @return A string representing n, which has pad - #digits(n)
     * 0's preceding the number.
     */
    public static String intPad(int n, int pad) {
        String s = String.valueOf(n);
        while (s.length() < pad) s = String.format("0%s", s);
        return s;
    }
}
