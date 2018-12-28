package org.javarosa.xform.util;

import org.javarosa.xform.parse.XFormParser;

/**
 * Provides a hook to allow customizing a parser before its parse method is called, allowing the tests
 * to use the same {@link org.javarosa.xform.util.XFormUtils#getFormFromInputStream(java.io.InputStream)}
 * call that OpenDataKit Collect uses.
 */
public interface ParserCustomizer {
    /**
     * Allows the parser user to customize the parser.
     *
     * @param parser the parser to be customized
     */
    void customize(XFormParser parser);
}
