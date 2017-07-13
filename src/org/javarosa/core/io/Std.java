package org.javarosa.core.io;

import java.io.PrintStream;

/**
 * Used to break dependency directly on System.out and System.err.
 */
public class Std {

    public static PrintStream out = System.out;
    public static PrintStream err = System.err;

    /**
     * Use this to replace stdout in case anything more than assignment is necessary in the future.
     * @param out
     */
    public static void setOut(PrintStream out) {
        Std.out = out;
    }

    /**
     * Use this to replace stderr in case anything more than assignment is necessary in the future.
     * @param err
     */
    public static void setErr(PrintStream err) {
        Std.err = err;
    }

    public static void printStack(Throwable t) {
        t.printStackTrace(Std.err);
    }
}