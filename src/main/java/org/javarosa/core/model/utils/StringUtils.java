package org.javarosa.core.model.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {
    /**
     * Tokenizes a string based on the given delimiter string
     *
     * @param str       The string to be split
     * @param delimiter The delimiter to be used. will be wrapped so may be a special regex character
     * @return An array of strings contained in original which were
     * seperated by the delimiter
     */
    public static List<String> split(String str, String delimiter, boolean combineMultipleDelimiters) {
        String[] split = str.split("[" + delimiter + "]");
        Stream<String> stringStream = Arrays.stream(split);
        if (combineMultipleDelimiters) {
            stringStream = stringStream.filter(string -> !string.trim().isEmpty());
        }
        return stringStream.collect(Collectors.toList());
    }
}
