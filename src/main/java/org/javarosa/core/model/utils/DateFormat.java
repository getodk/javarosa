package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Stream;

public enum DateFormat {

    ISO8601(1, "T") {
        @Override
        String formatDate(DateFields f) {
            return f.year + "-" + DateFormatter.intPad(f.month, 2) + "-" + DateFormatter.intPad(f.day, 2);
        }

        @Override
        protected String formatTime(DateFields fields) {
            String time = DateFormatter.intPad(fields.hour, 2) + ":" + DateFormatter.intPad(fields.minute, 2) + ":" + DateFormatter.intPad(fields.second, 2) + "." + DateFormatter.intPad(fields.secTicks, 3);

            //Time Zone ops (1 in the first field corresponds to 'CE' ERA)
            int milliday = ((fields.hour * 60 + fields.minute) * 60 + fields.second) * 1000 + fields.secTicks;
            int offset = TimeZone.getDefault().getOffset(1, fields.year, fields.month - 1, fields.day, fields.dow, milliday);

            //NOTE: offset is in millis
            if (offset == 0) {
                time += "Z";
            } else {

                //Start with sign
                String offsetSign = offset > 0 ? "+" : "-";

                int value = Math.abs(offset) / 1000 / 60;

                String hrs = DateFormatter.intPad(value / 60, 2);
                String mins = ":" + DateFormatter.intPad(value % 60, 2);

                time += offsetSign + hrs + mins;
            }
            return time;
        }
    },
    HUMAN_READABLE_SHORT(2, " ") {
        @Override
        String formatDate(DateFields f) {
            String year = Integer.valueOf(f.year).toString();

            //Normal Date
            if (year.length() == 4) {
                year = year.substring(2, 4);
            }
            //Otherwise we have an old or bizarre date, don't try to do anything

            return DateFormatter.intPad(f.day, 2) + "/" + DateFormatter.intPad(f.month, 2) + "/" + year;
        }

        @Override
        protected String formatTime(DateFields fields) {
            return DateFormatter.intPad(fields.hour, 2) + ":" + DateFormatter.intPad(fields.minute, 2);
        }
    },
    TIMESTAMP_SUFFIX(7, "") {
        @Override
        String formatDate(DateFields f) {
            return f.year + DateFormatter.intPad(f.month, 2) + DateFormatter.intPad(f.day, 2);
        }

        @Override
        protected String formatTime(DateFields fields) {
            return DateFormatter.intPad(fields.hour, 2) + DateFormatter.intPad(fields.minute, 2) + DateFormatter.intPad(fields.second, 2);
        }
    },
    /** RFC 822 */
    TIMESTAMP_HTTP(9, " ") {
        @Override
        String formatDate(DateFields f) {
            return DateFormatter.format(f, "%a, %d %b %Y");
        }

        @Override
        protected String formatTime(DateFields fields) {
            return DateFormatter.format(fields, "%H:%M:%S GMT");
        }
    };

    @NotNull
    public static Optional<DateFormat> getByKey(int format) {
        Stream<DateFormat> formatStream = Arrays.stream(values()).filter(dateFormat -> dateFormat.key == format);
        return formatStream.findFirst();
    }

    public final int key;
    public final String delimiter;

    DateFormat(int key, String delimiter) {
        this.key = key;
        this.delimiter = delimiter;
    }


    public String formatDate(Date d) {
        //TODO - is emptyString what we want?
        if (d == null) return "";

        DateFields fields = DateFields.getFields(d, this.key == TIMESTAMP_HTTP.key ? "UTC" : null);
        return formatDate(fields);
    }

    public String formatTime(Date date) {
        return formatTime(DateFields.getFields(date, this.key == TIMESTAMP_HTTP.key ? "UTC" : null));
    }

    abstract String formatDate(DateFields f);

    protected abstract String formatTime(DateFields fields);
}