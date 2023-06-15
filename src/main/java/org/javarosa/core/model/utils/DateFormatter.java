package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormatter {
    public static final int FORMAT_ISO8601 = 1;
    public static final int FORMAT_HUMAN_READABLE_SHORT = 2;
    public static final int FORMAT_TIMESTAMP_SUFFIX = 7;
    /** RFC 822 **/
    public static final int FORMAT_TIMESTAMP_HTTP = 9;

    public static String formatDateTime(Date date, int format) {
        return getDateFormat(format).formatDateTime(date);
    }

    private static DateFormat getDateFormat(int format) {
        return DateFormat.getByKey(format);
    }

    public static String formatTime(Date date, int format) {
        return getDateFormat(format).formatTime(date);
    }

    public static String formatDate(Date date, int format) {
        return getDateFormat(format).formatDate(date);
    }

    @NotNull
    public static String format(Date d, String format) {
        DateTimeFormatter formatter =
                format != null
                        ? formatTheFormat(format)
                        : DateTimeFormatter.ofPattern(("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        return format(d, formatter);
    }

    @NotNull
    public static String format(Date d, DateTimeFormatter formatter) {
        LocalDateTime localDate = DateUtils.localDateTimeFromDate(d);
        return formatter.format(localDate);
    }

    public static DateTimeFormatter formatTheFormat(String format) {
        String replaced = format
                .replace("%", "")
                .replace("T", "'T'") //some formats add a T to delineate date and time

                //see the XPathEvalTest for in context examples of why these substitutions are necessary
                .replace("m", "_mon_")
                .replace("M", "_MIN_")
                .replace("_mon_", "MM")
                .replace("_MIN_", "m")
                .replace("s", "_s_")
                .replace("S", "_S_")
                .replace("_s_", "SSS")
                .replace("_S_", "ss")
                .replace("w", "_w_")
                .replace("W", "_W_")
                .replace("_w_", "W")
                .replace("_W_", "w")

                //translate XPATH notation to java.time notation
                .replace("d", "dd")
                .replace("e", "d")
                .replace("H", "HH")
                .replace("a", "EEE")
                .replace("b", "MMM");

        //see DateFormatterTest.canFormatXPathFormFormat()
        char lastChar = replaced.charAt(replaced.length() - 1);
        try {
            int count = Integer.parseInt(String.valueOf(lastChar));
            if(count > 0) {
                replaced = replaced.substring(0, replaced.length() - 1);
                for (int i = 0; i < count; i++) {
                    replaced = replaced.concat("S");
                }
            }
        } catch (NumberFormatException nfe) {/* ignore */}

        return DateTimeFormatter.ofPattern(replaced);
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
