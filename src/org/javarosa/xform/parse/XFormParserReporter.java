/**
 *
 */
package org.javarosa.xform.parse;

import java.io.PrintStream;
import org.javarosa.core.io.Std;

/**
 * <b>Warning:</b> This class is unused and should remain that way. It will be removed in a future release.
 *
 * A Parser Reporter is provided to the XFormParser to receive
 * warnings and errors from the parser.
 *
 * @author ctsims
 * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
 */
@Deprecated
public class XFormParserReporter {
    @Deprecated
    public static final String TYPE_UNKNOWN_MARKUP = "markup";
    @Deprecated
    public static final String TYPE_INVALID_STRUCTURE = "invalid-structure";
    @Deprecated
    public static final String TYPE_ERROR_PRONE = "dangerous";
    @Deprecated
    public static final String TYPE_TECHNICAL = "technical";
    @Deprecated
    protected static final String TYPE_ERROR = "error";

    @Deprecated
    PrintStream errorStream;

    /**
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    @Deprecated
    public XFormParserReporter() {
        this(Std.err);
    }

    /**
     * @deprecated Use {@link org.slf4j.LoggerFactory#getLogger(Class)} instead
     */
    @Deprecated
    public XFormParserReporter(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#warn(String)} instead
     */
    @Deprecated
    public void warning(String type, String message, String xmlLocation) {
        errorStream.println("XForm Parse Warning: " + message + (xmlLocation == null ? "" : xmlLocation));
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#error(String, Throwable)} instead
     */
    @Deprecated
    public void error(String message) {
        errorStream.println("XForm Parse Error: " + message);
    }
}
