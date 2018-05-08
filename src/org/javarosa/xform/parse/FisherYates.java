package org.javarosa.xform.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * This class implements the <a href=" * This class implements the out-of-place
 * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates</a>
 * list shuffling algorithm.
 * <p>
 * Combined with the {@link ParkMiller} Random Number Generator
 * implementation on this library, cross-platform reproducible results
 * can be guaranteed.
 * <p>
 * Verified compatible libraries are:
 * <ul>
 * <li><a href="https://www.npmjs.com/package/fisher-yates">Javascript - fisher-yates npm module</a></li>
 * <li><a href="https://www.npmjs.com/package/park-miller">Javascript - park-miller npm module</a></li>
 * </ul>
 * <p>
 * Using other libraries won't guarantee reproducible results.
 * <p>
 * The main difference between this implementation and the native
 * {@link java.util.Collections#shuffle} consist on the way this one
 * selects which elements to swap on each iteration. See inlined comments
 * for more information.
 */
public class FisherYates {

    /**
     * Shuffle the input list of elements using a {@link Random} Random
     * Number Generator instance to decide which positions get swapped
     * on each iteration.
     *
     * @param input {@link List} of elements to be shuffled
     * @param <T>   Type parameter of the input {@link List} of elements
     * @return A new {@link List} of elements, containing the same elements
     *     as in the input {@link List}, but in a different order.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> shuffle(List<T> input) {
        return shuffle(input, new Random());
    }

    /**
     * Shuffle the input list of elements using a {@link ParkMiller} Random
     * Number Generator instance to decide which positions get swapped
     * on each iteration.
     * <p>
     * Use the same seed for the RNGs when reproducible results are required.
     *
     * @param input {@link List} of elements to be shuffled
     * @param seed  {@link Long} number to use as seed of the RNG
     * @param <T>   Type parameter of the input {@link List} of elements
     * @return A new {@link List} of elements, containing the same elements
     *     as in the input {@link List}, but in a different order.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> shuffle(List<T> input, long seed) {
        return shuffle(input, new ParkMiller(seed));
    }

    /**
     * Shuffle the input list of elements using a {@link Random} Random
     * Number Generator instance to decide which positions get swapped
     * on each iteration.
     * <p>
     * Use the same seed for the RNGs when reproducible results are required.
     *
     * <b>Warning:</b> It's recommended to use {@link #shuffle(List, long)}
     * instead, to ensure you are using a cross-platform RNG like {@link ParkMiller}.
     *
     * @param input  {@link List} of elements to be shuffled
     * @param random {@link Random} instance with the RNG to be used
     * @param <T>    Type parameter of the input {@link List} of elements
     * @return A new {@link List} of elements, containing the same elements
     *     as in the input {@link List}, but in a different order.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> shuffle(List<T> input, Random random) {
        // The algorithm creates an output array with the shuffled elements
        int size = input.size();
        Object[] inputArr = input.toArray();
        Object[] outputArr = new Object[size];

        for (int i = 0; i < size; ++i) {
            // This is the main difference with Collections.shuffle()
            // To make this version cross-platform, we need to decide the
            // target index using the next double in our PRNG to let
            // languages like JavaScript, where numbers are always represented
            // with IEE754 double precission floating point decimals.
            int j = Double.valueOf(random.nextDouble() * (i + 1)).intValue();
            if (j != i)
                // Make room for input[i] at output[j]
                outputArr[i] = outputArr[j];
            // Take input[i]
            outputArr[j] = inputArr[i];
        }

        // Once the output array is created, we set the output list
        // iterator with the elements in the order they appear
        List<T> output = new ArrayList<>(input);
        ListIterator it = output.listIterator();
        for (Object e : outputArr) {
            it.next();
            it.set(e);
        }

        return output;
    }
}
