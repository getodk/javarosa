/*
 * Copyright 2017 Nafundi
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

package org.javarosa.core.util;

import java.io.PrintStream;
import org.javarosa.core.io.Std;

/**
 * <b>Warning:</b> This class is unused and should remain that way. It will be removed in a future release.
 *
 * Helps with timing an operation and logging the time
 *
 * @deprecated Use {@link StopWatch} instead
 */
@Deprecated
public class CodeTimer {
    private final long startTime = System.nanoTime();
    private final String operation;
    private static boolean enabled = false;

    /**
     * Creates a CodeTimer for the specified operation
     *
     * @param operation the name of the operation, such as “parsing form”
     * @deprecated Use {@link StopWatch} instead
     */
    @Deprecated
    public CodeTimer(String operation) {
        this.operation = operation;
    }

    /**
     * Calculates and logs the time since this object was constructed.
     *
     * @deprecated Use {@link StopWatch} instead
     */
    @Deprecated
    public void logDone() {
        logDone(Std.out);
    }

    /**
     * Calculates and logs the time since this object was constructed.
     *
     * @param stream the PrintStream onto which to log the message
     * @deprecated Use {@link StopWatch} instead
     */
    @Deprecated
    public void logDone(PrintStream stream) {
        if (enabled) {
            stream.printf("%s finished in %.3f ms\n", operation, (System.nanoTime() - startTime) / 1e6);
        }
    }

    /**
     * @deprecated Use {@link StopWatch} instead
     */
    @Deprecated
    public static void setEnabled(boolean enabled) {
        CodeTimer.enabled = enabled;
    }
}