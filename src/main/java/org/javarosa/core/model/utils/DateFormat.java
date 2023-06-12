package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

public enum DateFormat {

    ISO8601(1, "T") {
        @Override
        String formatDate(DateFields f) {
            return f.year + "-" + DateFormatter.intPad(f.month, 2) + "-" + DateFormatter.intPad(f.day, 2);
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
    },
    TIMESTAMP_SUFFIX(7, "") {
        @Override
        String formatDate(DateFields f) {
            return f.year + DateFormatter.intPad(f.month, 2) + DateFormatter.intPad(f.day, 2);
        }
    },
    /** RFC 822 */
    TIMESTAMP_HTTP(9, " ") {
        @Override
        String formatDate(DateFields f) {
            return DateFormatter.format(f, "%a, %d %b %Y");
        }
    };

    @NotNull
    public static Optional<DateFormat> getByKey(int format) {
        Stream<DateFormat> formatStream = Arrays.stream(values()).filter(keyvalue -> keyvalue.key == format);
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

    abstract String formatDate(DateFields f);
}