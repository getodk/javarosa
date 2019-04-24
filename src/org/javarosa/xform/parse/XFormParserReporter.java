/**
 *
 */
package org.javarosa.xform.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(XFormParseException.class);
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
        LOGGER.warn("XForm Parse Warning: {} {}", message, xmlLocation == null ? "" : xmlLocation);
    }

    /**
     * @deprecated Use {@link org.slf4j.Logger#error(String, Throwable)} instead
     */
    @Deprecated
    public void error(String message) {
        LOGGER.error("XForm Parse Error: {}", message);
    }
}
