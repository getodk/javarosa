package org.javarosa.core.model.utils;

import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.TimeZone;

public class DateFormatter {
    public static final int FORMAT_ISO8601 = 1;
    public static final int FORMAT_HUMAN_READABLE_SHORT = 2;
    public static final int FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY = 5;
    public static final int FORMAT_TIMESTAMP_SUFFIX = 7;
    /** RFC 822 **/
    public static final int FORMAT_TIMESTAMP_HTTP = 9;

    public static String formatDateTime(Date d, int format) {
        if (d == null) {
            return "";
        }

        DateFields fields = DateFields.getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null);

        String delim;
        switch (format) {
            case FORMAT_ISO8601:
                delim = "T";
                break;
            case FORMAT_TIMESTAMP_SUFFIX:
                delim = "";
                break;
            case FORMAT_TIMESTAMP_HTTP:
                delim = " ";
                break;
            default:
                delim = " ";
                break;
        }

        return formatDate(fields, format) + delim + formatTime(fields, format);
    }

    public static String formatTime(Date d, int format) {
        return (d == null ? "" : formatTime(DateFields.getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null), format));
    }

    public static String formatDate(Date d, int format) {
        return (d == null ? "" : formatDate(DateFields.getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null), format));
    }

    static String formatDate(DateFields f, int format) {
        switch (format) {
            case FORMAT_ISO8601:
                return formatDateISO8601(f);
            case FORMAT_HUMAN_READABLE_SHORT:
                return formatDateColloquial(f);
            case FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY:
                return formatDaysFromToday(f);
            case FORMAT_TIMESTAMP_SUFFIX:
                return formatDateSuffix(f);
            case FORMAT_TIMESTAMP_HTTP:
                return formatDateHttp(f);
            default:
                return null;
        }
    }

    private static String formatTime(DateFields f, int format) {
        switch (format) {
            case FORMAT_ISO8601:
                return formatTimeISO8601(f);
            case FORMAT_HUMAN_READABLE_SHORT:
                return formatTimeColloquial(f);
            case FORMAT_TIMESTAMP_SUFFIX:
                return formatTimeSuffix(f);
            case FORMAT_TIMESTAMP_HTTP:
                return formatTimeHttp(f);
            default:
                return null;
        }
    }

    /** RFC 822 **/
    private static String formatDateHttp(DateFields f) {
        return format(f, "%a, %d %b %Y");
    }

    /** RFC 822 **/
    private static String formatTimeHttp(DateFields f) {
        return format(f, "%H:%M:%S GMT");
    }

    private static String formatDateISO8601(DateFields f) {
        return f.year + "-" + intPad(f.month, 2) + "-" + intPad(f.day, 2);
    }

    private static String formatDateColloquial(DateFields f) {
        String year = Integer.valueOf(f.year).toString();

        //Normal Date
        if (year.length() == 4) {
            year = year.substring(2, 4);
        }
        //Otherwise we have an old or bizarre date, don't try to do anything

        return intPad(f.day, 2) + "/" + intPad(f.month, 2) + "/" + year;
    }

    private static String formatDateSuffix(DateFields f) {
        return f.year + intPad(f.month, 2) + intPad(f.day, 2);
    }

    private static String formatTimeISO8601(DateFields f) {
        String time = intPad(f.hour, 2) + ":" + intPad(f.minute, 2) + ":" + intPad(f.second, 2) + "." + intPad(f.secTicks, 3);

        //Time Zone ops (1 in the first field corresponds to 'CE' ERA)
        int milliday = ((f.hour * 60 + f.minute) * 60 + f.second) * 1000 + f.secTicks;
        int offset = TimeZone.getDefault().getOffset(1, f.year, f.month - 1, f.day, f.dow, milliday);

        //NOTE: offset is in millis
        if (offset == 0) {
            time += "Z";
        } else {

            //Start with sign
            String offsetSign = offset > 0 ? "+" : "-";

            int value = Math.abs(offset) / 1000 / 60;

            String hrs = intPad(value / 60, 2);
            String mins = ":" + intPad(value % 60, 2);

            time += offsetSign + hrs + mins;
        }
        return time;
    }

    private static String formatTimeColloquial(DateFields f) {
        return intPad(f.hour, 2) + ":" + intPad(f.minute, 2);
    }

    private static String formatTimeSuffix(DateFields f) {
        return intPad(f.hour, 2) + intPad(f.minute, 2) + intPad(f.second, 2);
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
     * Provides text representing a span of time.
     *
     * @param f The fields for the date to be compared against the current date.
     * @return a string which is a human readable representation of the difference between
     * the provided date and the current date.
     */
    private static String formatDaysFromToday(DateFields f) {
        return formatDate(f, FORMAT_HUMAN_READABLE_SHORT);
//        Date d = DateUtils.getDate(f);
//        int daysAgo = DateUtils.daysSinceEpoch(new Date()) - DateUtils.daysSinceEpoch(d);
//        if (daysAgo == 0) {
//            return Localization.get("date.today");
//        } else if (daysAgo == 1) {
//            return Localization.get("date.yesterday");
//        } else if (daysAgo == 2) {
//            return Localization.get("date.twoago", new String[]{String.valueOf(daysAgo)});
//        } else if (daysAgo > 2 && daysAgo <= 6) {
//            return Localization.get("date.nago", new String[]{String.valueOf(daysAgo)});
//        } else if (daysAgo == -1) {
//            return Localization.get("date.tomorrow");
//        } else if (daysAgo < -1 && daysAgo >= -6) {
//            return Localization.get("date.nfromnow", new String[]{String.valueOf(-daysAgo)});
//        } else {
//            return formatDate(f, FORMAT_HUMAN_READABLE_SHORT);
//        }
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
