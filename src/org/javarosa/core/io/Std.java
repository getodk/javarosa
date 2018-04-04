package org.javarosa.core.io;

import java.io.PrintStream;

/**
 * <b>Warning:</b> This class is unused and should remain that way. It will be removed in a future release.
 * Used to break dependency directly on System.out and System.err.
 *
 * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
 */
@Deprecated
public class Std {

    @Deprecated
    public static PrintStream out = System.out;
    @Deprecated
    public static PrintStream err = System.err;

    /**
     * Use this to replace stdout in case anything more than assignment is necessary in the future.
     *
     * @param out
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    public static void setOut(PrintStream out) {
        Std.out = out;
    }

    /**
     * Use this to replace stderr in case anything more than assignment is necessary in the future.
     *
     * @param err
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    public static void setErr(PrintStream err) {
        Std.err = err;
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#error(String, Throwable)} instead
     */
    public static void printStack(Throwable t) {
        t.printStackTrace(Std.err);
    }
}