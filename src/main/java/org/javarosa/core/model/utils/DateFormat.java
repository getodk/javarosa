package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

public enum DateFormat {

    ISO8601(1, "'T'", "Y-MM-dd", "HH:mm:ss.SSS") {  //"11:22:33.123+01:00" yyyy-MM-dd'T'HH:mm:ss.SSS

        public String formatDateTime(Date date) {
            return DateFormatter
                    .format(date, DateTimeFormatter
                            .ofPattern(datePattern + delimiter + timePattern + offset(date)));
        }

        public String formatDate(Date date) {
            return DateFormatter.format(date, DateTimeFormatter.ofPattern(datePattern));
        }

        public String formatTime(Date date) {
            return DateFormatter.format(date, DateTimeFormatter.ofPattern((timePattern + offset(date))));
        }

        private String offset(Date date) {
            int offset = date.getTimezoneOffset();
            if (offset == 0) return "";
            String sign = (offset > 0) ? "-" : "+";
            return sign
                    + DateFormatter.intPad(Math.abs(offset / 60), 2) + ":"
                    + DateFormatter.intPad(Math.abs(offset % 60), 2);
        }
    },
    HUMAN_READABLE_SHORT(2, " ", "%d/%m/YY", "HH:mm") {
    },
    TIMESTAMP_SUFFIX(7, "", "%Y%m%d", "HHmmss") {
    },
    /** RFC 822 */
    TIMESTAMP_HTTP(9, " ", "E, d MMM Y", "HH:mm:ss z") {
        public String formatDateTime(Date d) {
            return toUTC(d, datePattern+delimiter+timePattern);
        }
        public String formatDate(Date date) {
            return toUTC(date, datePattern);
        }
        public String formatTime(Date date) {
            return toUTC(date, timePattern);
        }
        private String toUTC(Date currentDate, String pattern) {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.format(currentDate);
        }
    };

    @NotNull
    public static DateFormat getByKey(int format) {
        Stream<DateFormat> formatStream = Arrays.stream(values()).filter(dateFormat -> dateFormat.key == format);
        Optional<DateFormat> optional = formatStream.findFirst();
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("DateFormat unknown: " + format);
        }
        return optional.get();
    }

    public final int key;
    public final String delimiter;
    public final String datePattern;
    public final String timePattern;

    DateFormat(int key, String delimiter, String datePattern, String timePattern) {
        this.key = key;
        this.delimiter = delimiter;
        this.datePattern = datePattern;
        this.timePattern = timePattern;
    }

    public String formatDateTime(Date d) {
        //TODO - is emptyString what we want?
        if (d == null) return "";

        return DateFormatter.format(d, datePattern+delimiter+timePattern);
    }

    public String formatDate(Date d) {
        //TODO - is emptyString what we want?
        if (d == null) return "";

        return DateFormatter.format(d, datePattern);
    }

    public String formatTime(Date date) {
        return DateFormatter.format(date, DateTimeFormatter.ofPattern(timePattern));
    }
}