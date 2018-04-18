/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javarosa.xform.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class contains all the code needed to implement the xform randomize()
 * function.
 */
public final class RandomizeHelper {
    /**
     * Looks for a seed in an xform randomize() expression. If it is present and
     * it can be parsed into a {@link Long}, it returns it. If it is not present,
     * returns null.
     * <p>
     * Can throw an {@link IllegalArgumentException} if the expression doesn't conform
     * to an xform randomize() call.
     * Can throw a {@link NumberFormatException} if it can't parse the seed into a {@link Long}
     *
     * @param nodesetStr an xform randomize() expression
     * @return a {@link Long} seed or null if it is not found inside the expression
     */
    static Long parseSeed(String nodesetStr) {
        String[] args = getArgs(nodesetStr);
        return args.length == 2
            ? Long.parseLong(args[1].trim())
            : null;
    }

    /**
     * Cleans an xform randomize() expression to leave only its first argument, which
     * should be an xpath expression.
     * <p>
     * Can throw an {@link IllegalArgumentException} if the expression doesn't conform
     * to an xform randomize() call.
     *
     * @param nodesetStr an xform randomize() expression
     * @return a {@link String} with the first argument of the xform randomize() expression
     */
    static String cleanNodesetDefinition(String nodesetStr) {
        return getArgs(nodesetStr)[0].trim();
    }

    /**
     * This method will return a new list with the same elements, randomly reordered.
     * Every call to this method will produce a different random seed, which will
     * potentially produce a different ordering each time.
     *
     * @param elements {@link List} of elements of type T to be shuffled
     * @param <T>      Type of the elements in the list
     * @return a new {@link List} with the same input elements reordered
     */
    public static <T> List<T> shuffle(List<T> elements) {
        return shuffle(elements, null);
    }

    /**
     * This method will return a new list with the same elements, randomly reordered.
     * Given the same seed, calls to this method will produce exactly the same output.
     *
     * @param elements {@link List} of elements of type T to be shuffled
     * @param seed     {@link Long} seed for the Random number generator
     * @param <T>      Type of the elements in the list
     * @return a new {@link List} with the same input elements reordered
     */
    public static <T> List<T> shuffle(List<T> elements, Long seed) {
        List<T> output = new ArrayList<>(elements);
        FisherYates.shuffle(output, seed != null ? new ParkMiller(seed) : new Random());
        return output;
    }

    private static String[] getArgs(String nodesetStr) {
        if (!nodesetStr.startsWith("randomize(") || !nodesetStr.endsWith(")"))
            throw new IllegalArgumentException("Nodeset definition must use randomize(path, seed?) function");
        return nodesetStr.substring(10, nodesetStr.length() - 1).split(",");
    }
}
