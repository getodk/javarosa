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
