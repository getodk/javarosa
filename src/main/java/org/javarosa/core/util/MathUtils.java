/**
 *
 */
package org.javarosa.core.util;

import java.util.Random;

/**
 * Static utility functions for mathematical operations
 *
 * @author ctsims
 *
 */
public class MathUtils {
    private static Random r;

    public static Random getRand () {
        if (r == null) {
            r = new Random();
        }
        return r;
    }
}
